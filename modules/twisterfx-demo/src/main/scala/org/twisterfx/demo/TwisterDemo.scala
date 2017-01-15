package org.twisterfx.demo

import javafx.application.Application
import javafx.scene.control.{Button, Label}

import org.twisterfx.{App, View}

object TwisterDemo extends App( new DemoView() ) {
    override def stop(): Unit = println("stopping")
}

class DemoView() extends View[Button]("TwisterFX Demo Application", new  Button("Press me!")) {

    root.setOnAction( e => println("action"))

}
