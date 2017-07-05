package org.twisterfx

import java.util.ResourceBundle
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage, StageStyle, Window}
import javax.inject.{Inject, Named}

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._
import scala.util.Try

/**
  * Base for all views in the application
  */
@Named
trait View extends LazyLogging { //TODO logging framework should be chosen by the app

    // root node
    val root: Parent

    // title property
    lazy val titleProperty: StringProperty = new SimpleStringProperty(this, "title", getClass.getSimpleName )
    def title: String = titleProperty.get
    def title_=( value: String ): Unit = titleProperty.set(value)

    // resizable property
    lazy val resizableProperty: BooleanProperty = new SimpleBooleanProperty(this, "resizable", true )
    def resizable: Boolean = resizableProperty.get
    def resizable_=( value: Boolean ): Unit = resizableProperty.set(value)

    def sceneProperty: ReadOnlyObjectProperty[Scene] = root.sceneProperty()

    /**
      * Retrieves the window view belongs to
      * @return
      */
    def getWindow: Option[Window] = Option(sceneProperty.get).map(_.getWindow)

    final def assignTo( stage: Stage = new Stage(),
                        owner: Window = getActiveStage.orNull,
                        modality: Modality = null,
                        style: StageStyle = null,
                        sizeToScene: Boolean = true,
                        centerOnScreen: Boolean = true): Stage = {

        val scene = new Scene(root)
        stage.setScene(scene)
        stage.titleProperty.bind(titleProperty)
        stage.resizableProperty().bindBidirectional(resizableProperty)
        Option(owner).foreach( stage.initOwner )
        Option(modality).foreach(stage.initModality)
        Option(style).foreach(stage.initStyle)
        if (sizeToScene) stage.sizeToScene()
        if (centerOnScreen) stage.centerOnScreen()

        stage.sceneProperty().addListener{ scene: Scene => Option(scene).foreach(_ => beforeShow())}

        stage
    }

    /**
      * Invoked just before the view is shown
      * // TODO replace with property?
      */
    def beforeShow(): Unit = {}

    def show(owner: Window = getActiveStage.orNull,
             modality: Modality = null,
             style: StageStyle = null,
             sizeToScene: Boolean = true,
             centerOnScreen: Boolean = true): Unit = {
        assignTo(owner=owner, modality=modality, style=style, sizeToScene=sizeToScene, centerOnScreen=centerOnScreen).show()
    }

    def showWindow(owner: Window = getActiveStage.orNull, style: StageStyle = StageStyle.DECORATED): Unit = {
        show(owner = owner, style = style)
    }

    def showModal(owner: Window = getActiveStage.orNull, modality: Modality = Modality.WINDOW_MODAL, style: StageStyle = StageStyle.DECORATED): Unit = {
        show( owner = owner , modality = modality, style = style)
    }

    // TODO graphic for dialog
    // TODO header for dialog
    def showDialog[_ <: DialogCommand](
         owner: Window = getActiveStage.orNull,
         modality: Modality = Modality.WINDOW_MODAL,
         style: StageStyle = StageStyle.DECORATED,
         commands: Set[DialogCommand] = Set(DialogOKCommand, DialogCancelCommand ) ): Option[_ <: DialogCommand] = {

        val dialog = new javafx.scene.control.Dialog[DialogCommand]
        dialog.initOwner(owner)
        dialog.initStyle(style)
        dialog.initModality(modality)
        dialog.resizableProperty.bindBidirectional(resizableProperty)
        dialog.setTitle(title)
   
        val dialogPane = dialog.getDialogPane
        dialogPane.setContent(root)
        //        dialogPane.setHeaderText("HEADER HEADER HEADER")
        dialogPane.getButtonTypes.addAll(commands.map( _.buttonType).asJava)

        dialog.setResultConverter{ buttonType =>
            commands.find( _.buttonType == buttonType ).foreach( _.perform(dialog))
            null.asInstanceOf[DialogCommand]
        }
        dialog.showAndWait.asScala
    }

}

/**
  * View backed by fxml resource. Root node is created automatically by loading fxml
  * Tries to find a resource with same name as view (lowercase) in the same package
  * Resource bundle with the same name is used if found in the same package.
  */
@Named
class FXMLView extends View {

    @Inject protected var loader: FXMLLoader = _

    lazy val root: Parent = loadRoot()

    final def getController[T]: T = loader.getController[T]

    private def loadRoot(): Parent = {

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

        loader.setRoot(null)
        loader.setLocation(getClass.getResource(fxml).toURI.toURL)
        loader.setResources(resourceBundle)

        loader.load()
    }

}




