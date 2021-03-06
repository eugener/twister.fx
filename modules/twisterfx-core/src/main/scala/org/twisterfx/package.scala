package org

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.beans.{InvalidationListener, Observable}
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.geometry.Orientation
import javafx.stage.Stage

import com.sun.javafx.stage.StageHelper

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.language.implicitConversions


package object twisterfx {

//    // Simpler API for properties
//    implicit class PropertyImplicits[T]( objectValue: WritableObjectValue[T]  ) {
//
//        def value: T = objectValue.get()
//        def value_=( value: T ) : Unit = objectValue.set(value)
//
//        def apply(): T = value
//    }


    /**
      * Returns a currently focused stage/window if one exists
      * @return stage in focus
      */
    def getActiveStage: Option[Stage] =  StageHelper.getStages.asScala.find( _.isFocused)

    /**
      * Ensure execution of action on FX thread
      * @param action to execute
      */
    def onFX( action: => Unit ): Unit = {
        if ( Platform.isFxApplicationThread ) action else Platform.runLater( () => action)
    }

    //TODO is better API possible?
    def runAsync[T]( asyncAction: => T )( fxAction: T => Unit = {_: T =>} ): Future[Unit] = {
        import scala.concurrent.ExecutionContext.Implicits.global
        Future(asyncAction).map( result => onFX(fxAction(result)))
    }

    // --- JavaFX implicit conversions for listeners

    // ChangeListener
    implicit def func2ChangeListener3[T, R](func: (ObservableValue[_ <: T], T, T) => R): ChangeListener[T] =
        (o: ObservableValue[_ <: T], oldValue: T, newValue: T) => func(o, oldValue, newValue)

    implicit def func2ChangeListener2[T, R](func: (T, T) => R): ChangeListener[T] =
        (_: ObservableValue[_ <: T], oldValue: T, newValue: T) => func(oldValue, newValue)

    implicit def func2ChangeListener1[T, R](func: T => R): ChangeListener[T] =
        (_: ObservableValue[_ <: T], _: T, newValue: T) => func(newValue)


    // InvalidationListener
    implicit def func2InvalidationListener[T, R](func: Observable => Unit): InvalidationListener =
        (o: Observable) => func(o)

    implicit def func2InvalidationListener[T, R](func: => Unit): InvalidationListener =
        (_: Observable) => func

    // ListChangeListener
    implicit def func2ListChangeListener[T]( func: Change[_ <: T] => Unit ): ListChangeListener[T] =
        (c: Change[_ <: T]) => func(c)

    implicit def func2ListChangeListener[T]( func: => Unit ): ListChangeListener[T] =
        (_: Change[_ <: T]) => func


    implicit class OrientationImplicits( orientation: Orientation ) {

        def reverse: Orientation = orientation match  {
            case Orientation.HORIZONTAL => Orientation.VERTICAL
            case Orientation.VERTICAL => Orientation.HORIZONTAL
        }
    }
}
