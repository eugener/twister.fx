package org.twisterfx

import javafx.application.{Application, Platform}
import javafx.embed.swing.JFXPanel
import javafx.scene.control.Label
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

abstract class App( view: => View[Parent] = App.defaultView ) extends scala.App {

    // main method initialization
    App.activeApp = this
    Application.launch(classOf[JavaFXAppAdapter], args: _*)

    lazy val primaryView: View[Parent] = view

    def start(primaryStage: Stage): Unit = {

        //TODO apply stylesheets
        Option(primaryView).foreach( _.prepareForStage(primaryStage))
        //TODO configure stage

    }

    def stop() {}

}

private object App {
    var activeApp: App = _
    def defaultView = new View("Application Title", new Label("Application"))
}

private class JavaFXAppAdapter extends Application {

    override def start(primaryStage: Stage): Unit = {
        App.activeApp.start(primaryStage)
        primaryStage.show()
    }

    override def stop(): Unit = {
        App.activeApp.stop()
    }
}
