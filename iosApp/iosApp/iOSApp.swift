import SwiftUI
//import FirebaseAppCheck
//import FirebaseCore

@main
struct iOSApp: App {
//    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @Environment(\.scenePhase) private var scenePhase
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
//    class AppDelegate: UIResponder, UIApplicationDelegate {
//        func application(
//            _ application: UIApplication,
//            didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
//        ) -> Bool {
//            
//            let providerFactory = MainAppCheckProviderFactory()
//            AppCheck.setAppCheckProviderFactory(providerFactory)
//            
//            // 1. Configure Firebase Core
//            FirebaseApp.configure()
//            return true
//        }
//    }
}

//class MainAppCheckProviderFactory: NSObject, AppCheckProviderFactory {
//  func createProvider(with app: FirebaseApp) -> AppCheckProvider? {
//    #if targetEnvironment(simulator)
//      // Use debug provider on simulator
//      let provider = AppCheckDebugProvider(app: app)
//
//      // Print only locally generated token to avoid a valid token leak on CI.
//      print("Firebase App Check debug token: \(provider?.localDebugToken() ?? "" )")
//
//      return provider
//    #else
//    if #available(iOS 14.0, *) {
//      return AppAttestProvider(app: app)
//    } else {
//        return DeviceCheckProvider(app: app)
//    }
//    #endif
//  }
//}
