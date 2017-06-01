package org.twisterfx

import java.util.Locale
import javafx.application.Application
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.stage.Stage
import javax.inject.{Inject, Named}

import com.gluonhq.ignite.DIContext
import com.typesafe.scalalogging.LazyLogging


/**
  * A base from which application object is created. Since it implements [[scala.App]],
  * it automatically becomes and executable object. If the bean of type View with id "RootView"
  * is available in DI context, it will be injected and used as the root view of the application.
  *
  */
abstract class App() extends scala.App with LazyLogging {

    // main method initialization
    App.activeApp = this
    Application.launch(classOf[JavaFXAppAdapter], args: _*)

    /**
     * Application DI context. Has to be implemented in the actual application
     */
    protected def diContext: DIContext
    protected lazy val stylesheetName: String = s"/${getClass.getSimpleName.replaceAll("[$]", "").toLowerCase}.css"

    // Root view. Injected by name
    @Inject
    @Named("RootView")
    protected var view: View = _

    protected[twisterfx] final def start(primaryStage: Stage): Unit = {

        diContext.init()

        // Make sure the application stylesheet is applied
        primaryStage.sceneProperty().addListener{ (_,_,scene) =>
            try {
                Option(scene).foreach(_.getStylesheets.add(stylesheetName))
            } catch {
                case _: Throwable => logger.error(s"Stylesheet '$stylesheetName' is not found.")
            }
        }

        // Assign primary view if available
        Option(view) match {
            case None    => logger.error( "Root view was not found" )
            case Some(v) =>
                logger.info( "Root view found: " + v.getClass.getName )
                //TODO better stage configuration is needed
                v.assignTo(primaryStage)
        }


        beforeShow()
        primaryStage.show()

    }

    /**
      * Invoked just before showing the primary stage show
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

object AppContext {

    // global application wide locale
    // locale property
    lazy val localeProperty: ObjectProperty[Locale] = new SimpleObjectProperty[Locale]()
    def locale: Locale = localeProperty.value
    def locale_=( value: Locale ): Unit = localeProperty.value = value

}
