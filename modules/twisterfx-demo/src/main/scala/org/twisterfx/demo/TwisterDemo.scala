package org.twisterfx.demo

import javafx.application.Application
import javafx.scene.control.{Button, Label}

import org.twisterfx.{App, View}

object TwisterDemo extends App( View( new  Button("Press me!"), "Twister Demo Application") ) {
    override def stop(): Unit = println("stopping")
}
