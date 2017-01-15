package org.twisterfx

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import FXImplicits._

//TODO View loaded from FXML

class View[+T <: Parent](titleText: String, val root: T) {

    require( root != null, "Root component of the View cannot be null" )

    //TODO add constructor with resource bundle
    //TODO allow injection in the controller
    def this( titleText: String, fxmlResource: String )  {
        this(titleText, FXMLLoader.load( getClass.getResource(fxmlResource).toURI.toURL ).asInstanceOf[T])
    }

    // title property
    lazy val title: StringProperty = new SimpleStringProperty(this, "title", titleText)
    def title_=( value: String ): Unit = title.value = value

    final def prepareForStage( stage: Stage = new Stage() ): Stage = {
        stage.setScene(new Scene(root))
        stage.titleProperty.bind(title)
        stage.sizeToScene()
        stage.centerOnScreen()
        stage
    }

    def show(): Unit = prepareForStage().show()

    //TODO Show as dialog?


}


