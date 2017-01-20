package org.twisterfx

import java.util.ResourceBundle
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage, StageStyle, Window}

/**
  * Base for all views in the application
  */
trait View {

    // root node
    protected val root: Parent

    // title property
    lazy val titleProperty: StringProperty = new SimpleStringProperty(this, "title", getClass.getSimpleName )
    def title: String = titleProperty.value
    def title_=( value: String ): Unit = titleProperty.value = value

    // scene property
    private lazy val sceneProperty = new ReadOnlyObjectWrapper[Scene](this, "scene", null)
    lazy val scene: ReadOnlyObjectProperty[Scene] = sceneProperty.getReadOnlyProperty

    final def withStage(
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
        withStage(owner = owner, style = style).show()
    }

    def showModal(owner: Window = null, modality: Modality = Modality.WINDOW_MODAL, style: StageStyle = StageStyle.DECORATED): Unit = {
        withStage( owner = owner , modality = modality, style = style).show()
    }

    //TODO Show as dialog?


}

/**
 * View backed by fxml.
  * Root node is created automatically by loading fxml
 */


/**
  * View backed by fxml resource. Root node is created automatically by loading fxml
  * @param fxmlResource fxml resource
  * @param resourceBundle related resource bundle
  */
abstract class FXMLView( fxmlResource: String, resourceBundle: ResourceBundle = null) extends View {

    protected lazy val root: Parent = FXMLLoader.load( getClass.getResource(fxmlResource).toURI.toURL, resourceBundle )

}




