# Employee Profile Manager

A Kotlin Multiplatform app for keeping a company roster, with one Compose UI shared by Android and
iOS. You can add employees with a photo and a resume, search and filter the list, see who the top
earners are and how the departments break down, and undo a delete you didn't mean. Everything is
stored on the device, so it works with no network at all, and it follows the system light and dark
setting or whichever you pick.

## Demo

Video walkthrough, Android and iOS:
https://drive.google.com/drive/folders/1oKQdzGLTKa1f5N3ppZwLRPFo0z2RUrrx?usp=sharing

## What it does

- Keeps a roster of employees in a local database, with name, contact details, department, role,
  salary, joining date, gender, employment type, skills, and whether they're still active.
- Validates the form as you fill it in, and refuses to save someone whose email or phone number
  already belongs to another employee — naming who has it rather than just rejecting the entry.
- Searches by name, email, or phone as you type, highlighting the part that matched, and suggests
  names from a prefix of two characters or more.
- Filters by department, employment type, and status, and sorts by name, salary, or joining date.
  Filters apply as you tap them rather than on a Save button.
- Loads the list twenty at a time, so a long roster doesn't build every row up front.
- Attaches a profile photo from the gallery or the camera, and a resume from the device's files.
- Ranks the top earners — between one and ten of them — and breaks the roster down by department
  with headcount, total, average, and highest salary.
- Remembers the last five employees you opened and shows them above the list.
- Undoes a delete, up to ten of them, from the message that appears after each one.
- Exports the whole filtered roster to a PDF and hands it to the system share sheet.

## Tech

- Kotlin Multiplatform with Compose Multiplatform — all UI in `commonMain`, no per-platform screen
- Material 3, with colour, type, and spacing tokens shared across both platforms
- MVVM; UI state exposed as `StateFlow` from the JetBrains lifecycle ViewModel
- Room with the bundled SQLite driver, running the same schema and DAO on both platforms
- Koin for dependency injection, with an `expect val platformModule` per target
- Navigation Compose (JetBrains multiplatform port) with shared-element transitions
- DataStore preferences for the theme choice
- Coil for loading images off the filesystem
- Coroutines and Flow for async work
- Android min SDK 29, compile SDK 37; iOS 14.1 and newer, Apple silicon simulator

## How it's put together

The code is split into a data layer, a small domain layer, and a view layer. The data layer owns the
database and maps entities to a domain model; the domain layer holds the data structures the
features are built on; the view layer holds the screens and the ViewModels that own their state.
State moves in one direction: the ViewModel exposes `StateFlow`s, the UI reads them, and the UI calls
back into the ViewModel when something happens.

```
Room (SQLite) -> EmployeeDao -> EmployeeEntity -> toDomain() -> Employee
                                                                   |
                                             EmployeeRepository (Flow + duplicate index)
                                                                   |
                                                  ViewModel  --StateFlow-->  Compose UI
                                                                   ^              |
                                                                   +--- events ---+
```

Anything a platform has to answer for itself — picking an image, writing a PDF, building the
database, the IO dispatcher — is an `expect` declaration in `commonMain` with an `actual` on each
side. That's the only place platform code appears; every screen is written once.

A few decisions worth calling out:

- The duplicate check is a pair of hash maps from email and normalised phone to the id that owns
  them, kept in the repository rather than a ViewModel so every screen sees the same answer. It
  stores the owner rather than just the value, so editing your own record isn't a clash with
  yourself.
- Phone numbers are normalised to their last ten digits before comparing, so the same number written
  with a country code, spaces, or dashes is still recognised as taken.
- Top earners uses a hand-rolled min heap of size n rather than sorting the roster, so finding five
  out of a thousand stays O(n log k) instead of O(n log n).
- Name suggestions come from a trie rebuilt from the same database flow the list uses, so there's
  one query behind the list, the suggestions, and the recently-viewed row instead of three.
- Undo is a stack of deleted employees, capped at ten so it can't grow without bound; recently
  viewed is a bounded queue that moves a repeat visit to the front rather than adding it twice.
- Recently viewed publishes its own flow. Opening someone writes nothing to the database, so a
  screen watching only the employee table would never hear about it.
- Paging is a visible count in the ViewModel over the full flow rather than a `LIMIT` in the DAO.
  The roster is small enough that the query cost isn't the thing worth optimising, and it keeps
  search, filter, and sort composing over one list.
- The list screen owns filters and sort as separate flows that combine into the visible list, so
  changing one doesn't rebuild the others.

```
shared/src/
├── commonMain/kotlin/com/example/employeeprofile/
│   ├── data/
│   │   ├── local/                          # Room database, DAO, entity, converters
│   │   ├── model/                          # domain models and enums
│   │   └── repository/EmployeeRepository.kt
│   ├── domain/algo/                        # the data structures the features are built on
│   │   ├── DuplicateIndex.kt               # hash maps: email/phone -> owning id
│   │   ├── MinHeap.kt, TopEarners.kt       # top n by salary
│   │   ├── NameTrie.kt                     # prefix suggestions
│   │   ├── UndoStack.kt                    # bounded stack of deletes
│   │   ├── RecentlyViewed.kt               # bounded queue, newest first
│   │   ├── DepartmentSummary.kt            # grouping and totals
│   │   └── PhoneNormalizer.kt
│   ├── di/Koin.kt                          # modules, with expect val platformModule
│   ├── platform/                           # expect: media picker, PDF export, dispatcher
│   └── view/
│       ├── EmployeeApp.kt                  # navigation host
│       ├── component/                      # reusable UI pieces
│       ├── screen/
│       │   ├── list/                       # list, filters, sort, search
│       │   ├── detail/
│       │   ├── form/                       # add and edit, with validation
│       │   ├── topearners/
│       │   └── summary/
│       └── theme/                          # colour, type, spacing
├── androidMain/                            # actual: PickVisualMedia, PdfDocument, Room
├── iosMain/                                # actual: PHPicker, UIGraphics PDF, Room builder
├── commonTest/                             # runs on both the JVM and iOS
└── iosTest/                                # iOS-only: PDF bytes, document picker

androidApp/                                 # Activity and Application, Koin start
iosApp/                                     # SwiftUI host around the shared UI
```

## Running it

You'll need Android Studio, and Xcode for the iOS side.

```bash
git clone https://github.com/mihirbajpai/employee-profile-manager.git
cd employee-profile-manager
```

For Android, open the project in Android Studio and press Run, or build from the terminal:

```bash
./gradlew installDebug
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run it on an Apple silicon simulator or a
device. Gradle builds the shared framework as part of the Xcode build, so there's no separate step.

There's nothing to configure — the database is created on first launch and everything stays on the
device.

## Running the tests

The shared code is tested once and run twice: `commonTest` compiles for both the JVM and iOS
native, so the same assertions execute against both compilers. `iosTest` adds the cases that only
mean something on a device — that the PDF writer really produces PDF bytes, and that a picked
document lands in app storage.

```bash
./gradlew allTests
```

That gives 110 tests on the JVM and 119 on the iOS simulator — the same 110, plus 9 iOS-only ones.
To run one side on its own:

```bash
./gradlew :shared:testAndroidHostTest        # JVM only, no simulator needed
./gradlew :shared:iosSimulatorArm64Test      # iOS simulator
```

HTML reports land in `shared/build/reports/tests/`.

The iOS half needs a full Xcode rather than only the Command Line Tools, because it asks `simctl`
which simulator to use. If `xcrun --find simctl` comes up empty, that task reports itself skipped
instead of failing the build, so the JVM suite still runs and reports. To bring the iOS tests back:

```bash
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```

## Notes for the reviewer

A few places where this departs from the obvious reading of the brief, and why:

- **The duplicate check lives in the repository, not a ViewModel.** The brief describes it as a
  ViewModel concern, but two screens need the same answer, and a per-ViewModel copy would let them
  disagree after an edit. It's built lazily on first use rather than at startup, so launching the
  app doesn't pay for an index nobody has asked for yet.
- **Paging is done in the ViewModel, not with `LIMIT` in the DAO.** Keeping one flow of the whole
  roster lets search, filter, and sort compose over the same list; a paged query would need each of
  those pushed into SQL to stay correct. At this size the query isn't the cost worth cutting.
- **Android asks for `CAMERA` but not `READ_MEDIA_IMAGES`.** Taking a photo needs the camera
  permission, so it's declared; choosing an existing one doesn't, because the gallery path uses the
  Photo Picker (`PickVisualMedia`), which hands back the single image the user chose. Asking for
  library access on top of that would be asking for more than the feature needs.
- **iosX64 isn't a target.** Compose Multiplatform no longer publishes artifacts for it, so the iOS
  side builds for `iosArm64` and `iosSimulatorArm64` — devices and Apple silicon simulators.
- **Coil is pinned to 3.4.0.** 3.5.0 ships klibs built with Kotlin 2.4, which this toolchain can't
  read. The pin produces a Skiko version notice during the iOS build; it's a compatibility warning,
  not a defect, and both platforms build and pass.

The brief's numbering skips from section 4 to section 6 — there is no section 5 in the document, so
nothing has been left out here.
