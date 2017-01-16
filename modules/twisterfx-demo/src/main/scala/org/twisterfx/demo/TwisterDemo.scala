package org.twisterfx.demo

import org.twisterfx.{App, View}

object TwisterDemo extends App( View("TwisterFX Demo Application", "/demo.fxml") ) {

    override def stop(): Unit = println("stopping")
}
