package org.twisterfx

import java.util.ResourceBundle
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage, StageStyle, Window}

abstract class View( viewTitle: String, rootNode: Parent ) {

    val root: Parent = rootNode

    def this(viewTitle: String, fxmlResource: String, resources: ResourceBundle = null ) = {
        this( viewTitle, FXMLLoader.load( getClass.getResource(fxmlResource).toURI.toURL, resources ) )
    }

    // title property
    lazy val titleProperty: StringProperty = new SimpleStringProperty(this, "title", viewTitle)
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


