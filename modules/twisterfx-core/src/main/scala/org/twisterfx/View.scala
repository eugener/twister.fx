package org.twisterfx

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import FXImplicits._

//TODO View loaded from FXML

class View[+T <: Parent](titleText: String = null, val root: T) {

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


