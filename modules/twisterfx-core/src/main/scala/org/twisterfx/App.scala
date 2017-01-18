package org.twisterfx

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.stage.Stage


/**
  * A base from which application object is created. Since it implements [[scala.App]],
  * it automatically becomes and executable object
  * @param view function to produce a main view
  */
abstract class App( view: => View[_<:Parent] = App.defaultView ) extends scala.App {

    //TODO inject view

    // main method initialization
    App.activeApp = this
    Application.launch(classOf[JavaFXAppAdapter], args: _*)

    private lazy val primaryView: View[_<:Parent] = view

    def start(primaryStage: Stage): Unit = {

        //TODO apply stylesheets
        Option(primaryView).foreach{
            //TODO configure stage
            _.withStage(primaryStage).show()
        }

    }

    def stop() {}

}

private object App {
    var activeApp: App = _
    def defaultView: View[Label] = {
        val label = new Label("Application")
        label.setStyle("-fx-padding: 25;")
        View("Application Title", label)
    }
}

private class JavaFXAppAdapter extends Application {
    override def start(primaryStage: Stage): Unit = App.activeApp.start(primaryStage)
    override def stop(): Unit = App.activeApp.stop()
}
