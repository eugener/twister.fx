package org.twisterfx

import javafx.application.{Application, Platform}
import javafx.embed.swing.JFXPanel
import javafx.scene.control.Label
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

abstract class App( view: => View = App.defaultView ) extends scala.App {

    // main method initialization
    App.activeApp = Some(this)
    Application.launch(classOf[AppHelper], args: _*)

    lazy val primaryView: View = view

    def start(primaryStage: Stage): Unit = {

        //TODO apply stylesheets
        Option(primaryView).foreach( v => v.prepareForStage(primaryStage))
        //TODO configure stage

    }

    def stop() {}

}

private object App {
    var activeApp: Option[App] = None
    def defaultView = View( new Label("Application"), "Application Title")
}

private class AppHelper extends Application {

    override def start(primaryStage: Stage): Unit = {
        App.activeApp.foreach(_.start(primaryStage))
        primaryStage.show()
    }

    override def stop(): Unit = {
        App.activeApp.foreach(_.stop())
    }
}
