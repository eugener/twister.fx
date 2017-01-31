package org.twisterfx

import java.util.ResourceBundle
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage, StageStyle, Window}

import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Base for all views in the application
  */
trait View extends LazyLogging {

    // root node
    protected val root: Parent

    // title property
    lazy val titleProperty: StringProperty = new SimpleStringProperty(this, "title", getClass.getSimpleName )
    def title: String = titleProperty.value
    def title_=( value: String ): Unit = titleProperty.value = value

    // scene property
    private lazy val sceneProperty = new ReadOnlyObjectWrapper[Scene](this, "scene", null)
    lazy val scene: ReadOnlyObjectProperty[Scene] = sceneProperty.getReadOnlyProperty

    final def assignTo(
                 stage: Stage = new Stage(),
                 owner: Window = null,
                 modality: Modality = null,
                 style: StageStyle = null ): Stage = {

        sceneProperty.value = new Scene(root)
        stage.setScene(scene.get())
        stage.titleProperty.bind(titleProperty)
        Option(owner).foreach( stage.initOwner )
        Option(modality).foreach(stage.initModality)
        Option(style).foreach(stage.initStyle)
        stage.sizeToScene()
        stage.centerOnScreen()

        scene.addListener{ scene: Scene => Option(scene).foreach(_ => beforeShow())}

        stage
    }

    /**
      * Invoked just before the view is shown
      */
    def beforeShow(): Unit = {}

    def showWindow(owner: Window = null, style: StageStyle = StageStyle.DECORATED): Unit = {
        assignTo(owner = owner, style = style).show()
    }

    def showModal(owner: Window = null, modality: Modality = Modality.WINDOW_MODAL, style: StageStyle = StageStyle.DECORATED): Unit = {
        assignTo( owner = owner , modality = modality, style = style).show()
    }

    //TODO Show as dialog?

}

/**
  * View backed by fxml resource. Root node is created automatically by loading fxml
  * Tries to find a resource with same name as view (lowercase) in the same package
  * Resource bundle with the same name is used if found in the same package.
  */
class FXMLView extends View {

    protected lazy val loader: FXMLLoader = {

        // load resource bundle, null if not found
        val resourceBundle = Try{
            val bundleName = getClass.getName.toLowerCase
            ResourceBundle.getBundle(bundleName, AppContext.locale)
        }.getOrElse{
            logger.info("Resource bundle is not found - localization is not available.")
            null
        }

        val fxml = s"${getClass.getSimpleName.toLowerCase}.fxml"
        logger.info(s"Assuming fxml location as '$fxml'")

        new FXMLLoader(getClass.getResource(fxml).toURI.toURL, resourceBundle)
    }

    protected lazy val root: Parent = loader.load()

    final def getController[T]: T = loader.getController[T]

}




