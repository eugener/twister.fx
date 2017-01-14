package org.twisterfx

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.scene.Parent
import FXImplicits._

case class View( root: Parent, titleText: String = null ) {

    lazy val title: StringProperty = new SimpleStringProperty(this, "title", titleText)
    def title_=( value: String ): Unit = title.value = value


}


