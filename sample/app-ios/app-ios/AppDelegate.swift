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
    
    func application(_ application: UIApplication, shouldSaveApplicationState coder: NSCoder) -> Bool {
        CoderUtilsKt.encodeParcelable(coder, value: someLogic.saveState(), key: "some_state")
        return true
    }
    
    func application(_ application: UIApplication, shouldRestoreApplicationState coder: NSCoder) -> Bool {
        let state: Parcelable? = CoderUtilsKt.decodeParcelable(coder, key: "some_state")
        restoredSomeLogic = SomeLogic(savedState: state as? SomeLogic.SavedState)
        return true
    }
}
