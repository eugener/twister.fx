package org.twisterfx

import javafx.application.Application
import javafx.stage.Stage

import com.gluonhq.ignite.DIContext
import javax.inject.{Inject, Named}


/**
  * A base from which application object is created. Since it implements [[scala.App]],
  * it automatically becomes and executable object. If the bean of type View with id "RootView"
  * is available in DI context, it will be injected and used as the root view of the application.
  *
  */
abstract class App() extends scala.App {

    // main method initialization
    App.activeApp = this
    Application.launch(classOf[JavaFXAppAdapter], args: _*)

    /**
     * Application DI context. Has to be implemented in the actual application
     */
    protected def diContext: DIContext

    // root view. Injected by name
    @Inject
    @Named("RootView")
    protected var view: View = _

    protected[twisterfx] final def start(primaryStage: Stage): Unit = {

        diContext.init()

        //TODO apply stylesheets
        Option(view).foreach{ v =>
            //TODO better stage configuration is needed
            v.withStage(primaryStage)
            beforeShow()
            primaryStage.show()
        }

    }

    /**
      * Invoked just before showing the primar ystage
      */
    def beforeShow() {}

    /**
      * Invoke on application stop
      */
    def stop() {}

}

private object App {
    var activeApp: App = _
}

private class JavaFXAppAdapter extends Application {
    override def start(primaryStage: Stage): Unit = App.activeApp.start(primaryStage)
    override def stop(): Unit = App.activeApp.stop()
}
