package org.twisterfx.demo

import javafx.application.Application
import javafx.scene.control.{Button, Label, TabPane}

import org.twisterfx.{App, View}

object TwisterDemo extends App( new View[TabPane]("TwisterFX Demo Application", "/demo.fxml") ) {
    override def stop(): Unit = println("stopping")
}
