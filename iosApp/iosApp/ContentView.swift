import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose paints edge to edge and applies the safe-area insets itself, through
            // safeDrawingPadding in EmployeeApp. Letting SwiftUI inset it too would leave
            // unpainted bands behind the status bar and home indicator.
            .ignoresSafeArea()
    }
}
