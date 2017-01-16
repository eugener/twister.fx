package org.twisterfx

import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import org.twisterfx._

object View {

    //TODO add constructor with resource bundle
    //TODO allow injection in the controller

    def apply[T <: Parent]( viewTitle: String, rootNode: T ): View[T] = new View[T] {
        require(rootNode != null, "Root component of the View cannot be null")
        override val root: T = rootNode
        title = viewTitle
    }

    def apply[T <: Parent]( viewTitle: String, fxmlResource: String ): View[T] = new View[T] {
        require(fxmlResource != null, "fxml resource cannot be null")
        override val root: T = FXMLLoader.load( getClass.getResource(fxmlResource).toURI.toURL )
        title = viewTitle
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

    final def prepareForStage( stage: Stage = new Stage() ): Stage = {
        sceneProperty.value = new Scene(root)
        stage.setScene(scene.get())
        stage.titleProperty.bind(titleProperty)
        stage.sizeToScene()
        stage.centerOnScreen()
        stage
    }

    def show(): Unit = prepareForStage().show()

    //TODO Show as dialog?


}


