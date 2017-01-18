package org.twisterfx

import java.util.ResourceBundle
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage, StageStyle, Window}

object View {

    //TODO allow injection in the controller

    /**
      * Creates a view based on the root node
      * @param viewTitle view title
      * @param rootNode root node of the view
      * @tparam T root node type
      * @return a view
      */
    def apply[T <: Parent]( viewTitle: String, rootNode: T ): View[T] = new View[T] {
        require(rootNode != null, "Root component of the View cannot be null")
        override val root: T = rootNode
        title = viewTitle
    }

    /**
      * Creates a view by creating root node from fxml resource
      * @param title view title
      * @param fxmlResource fxml resource
      * @param resources resource bundle for i18n
      * @tparam T root node type
      * @return a view
      */
    def apply[T <: Parent]( title: String, fxmlResource: String, resources: ResourceBundle = null ): View[T] = {
        require(fxmlResource != null, "fxml resource cannot be null")
        apply[T]( title, FXMLLoader.load( getClass.getResource(fxmlResource).toURI.toURL, resources ))
    }

}


trait View[+T <: Parent] {

    val root: T

    // title property
    lazy val titleProperty: StringProperty = new SimpleStringProperty(this, "title")
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

    def beforeShow(): Unit = {}

    def showWindow(owner: Window = null, style: StageStyle = StageStyle.DECORATED): Unit = {
        withStage(owner = owner, style = style).show()
    }

    def showModal(owner: Window = null, modality: Modality = Modality.WINDOW_MODAL, style: StageStyle = StageStyle.DECORATED): Unit = {
        withStage( owner = owner , modality = modality, style = style).show()
    }

    //TODO Show as dialog?


}


