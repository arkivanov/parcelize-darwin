//
//  app_iosApp.swift
//  app-ios
//
//  Created by Arkadii Ivanov on 20/04/2021.
//

import SwiftUI
import ParcelizeSample

@main
struct app_iosApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView(appDelegate.someLogic)
        }
    }
}
