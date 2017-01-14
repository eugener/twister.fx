package org.twisterfx

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import FXImplicits._

//TODO View loaded from FXML

case class View( root: Parent, titleText: String = null ) {

    lazy val title: StringProperty = new SimpleStringProperty(this, "title", titleText)
    def title_=( value: String ): Unit = title.value = value

    def prepareForStage( stage: Stage ): Stage = {
        stage.setScene(new Scene(root))
        stage.titleProperty.bind(title)
        stage.sizeToScene()
        stage.centerOnScreen()
        stage
    }

    def show(): Unit = prepareForStage(new Stage()).show()

    //TODO Show as dialog?


}


