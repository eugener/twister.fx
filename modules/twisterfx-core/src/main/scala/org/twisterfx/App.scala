package org.twisterfx

import javafx.application.Application
import javafx.stage.Stage

import com.gluonhq.ignite.DIContext
import javax.inject.{Inject, Named}


/**
  * A base from which application object is created. Since it implements [[scala.App]],
  * it automatically becomes and executable object
  * @param view function to produce a main view
  */
abstract class App() extends scala.App {

    // main method initialization
    App.activeApp = this
    Application.launch(classOf[JavaFXAppAdapter], args: _*)

    protected def diContext: DIContext

    @Inject @Named("MainView")
    protected var view: View = _

    protected[twisterfx] final def start(primaryStage: Stage): Unit = {

        diContext.init()

        //TODO apply stylesheets
        Option(view).foreach{ v =>
            //TODO configure stage
            v.withStage(primaryStage)
            beforeShow()
            primaryStage.show()
        }

    }

    def beforeShow() {}
    def stop() {}

}

private object App {
    var activeApp: App = _
}

private class JavaFXAppAdapter extends Application {
    override def start(primaryStage: Stage): Unit = App.activeApp.start(primaryStage)
    override def stop(): Unit = App.activeApp.stop()
}
