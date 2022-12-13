//
//  AppDelegate.swift
//  app-ios
//
//  Created by Arkadii Ivanov on 20/04/2021.
//

import UIKit
import ParcelizeSample

class AppDelegate: UIResponder, UIApplicationDelegate {
    private var restoredSomeLogic: SomeLogic? = nil
    lazy var someLogic: SomeLogic = { restoredSomeLogic ?? SomeLogic(savedState: nil) }()
    
    func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
        CodingKt.encodeParcelable(coder, value: someLogic.saveState(), key: "some_state")
        return true
    }
    
    func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
        do {
            let state: Parcelable? = try CodingKt.decodeParcelable(coder, key: "some_state")
            restoredSomeLogic = SomeLogic(savedState: state as? SomeLogic.SavedState)
            return true
        } catch {
            return false
        }
    }
}
