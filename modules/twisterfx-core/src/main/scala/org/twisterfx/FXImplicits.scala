package org.twisterfx

import javafx.beans.value.WritableObjectValue

/**
  * Created by eryzhikov on 1/13/17.
  */
object FXImplicits {

    implicit class PropertyImplicits[T]( objectValue: WritableObjectValue[T]  ) {

        def value: T = objectValue.get()
        def value_=( value: T ) : Unit = objectValue.set(value)

        def apply(): T = value
    }

}
