import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    let secured = url.startAccessingSecurityScopedResource()
                    defer {
                        if secured {
                            url.stopAccessingSecurityScopedResource()
                        }
                    }
                    if let content = try? String(contentsOf: url, encoding: .utf8), !content.isEmpty {
                        MainViewControllerKt.handleIncomingFileContent(content: content)
                    }
                }
        }
    }
}
