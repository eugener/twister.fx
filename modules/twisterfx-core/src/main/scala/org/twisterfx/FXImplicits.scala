package org.twisterfx

import javafx.beans.value.WritableObjectValue

object FXImplicits {

    // Simpler API for properties
    implicit class PropertyImplicits[T]( objectValue: WritableObjectValue[T]  ) {

        def value: T = objectValue.get()
        def value_=( value: T ) : Unit = objectValue.set(value)

        def apply(): T = value
    }

}
