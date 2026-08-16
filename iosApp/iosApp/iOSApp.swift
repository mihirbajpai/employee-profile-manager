import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        Koin_iosKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
