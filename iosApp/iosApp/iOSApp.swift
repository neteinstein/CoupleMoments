import SwiftUI
import CoupleMomentsShared

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
