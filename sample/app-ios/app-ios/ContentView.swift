//
//  ContentView.swift
//  app-ios
//
//  Created by Arkadii Ivanov on 20/04/2021.
//

import SwiftUI
import ParcelizeSample

struct ContentView: View {
    private let someLogic: SomeLogic
    
    @State
    private var value: Int32
    
    init(_ someLogic: SomeLogic) {
        self.someLogic = someLogic
        self._value = State(initialValue: someLogic.value)
    }
    
    var body: some View {
        VStack {
            Text("Value: \(value)")
                .padding()
            
            Button("Generate") {
                self.someLogic.generate()
                self.value = someLogic.value
            }
        }
    }
}
