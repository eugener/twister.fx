package org.twisterfx

import javafx.scene.control.Alert
import javafx.scene.layout.BorderPane
import javafx.stage.{Modality, Stage}

import com.sun.javafx.stage.StageHelper


object Alerts {

    import java.io.{PrintWriter, StringWriter}
    import javafx.scene.control.Alert.AlertType
    import javafx.scene.control.{ButtonType, Label, TextArea}

    import scala.collection.JavaConverters._
    import scala.compat.java8.OptionConverters._


    private def getActiveStage: Option[Stage] =  StageHelper.getStages.asScala.find( _.isFocused)

    private def custom( alertType: AlertType, title: String, content: String, header: String = null): Alert = {
        val alert = new javafx.scene.control.Alert(alertType)
        getActiveStage.foreach(alert.initOwner)
        alert.initModality( Modality.WINDOW_MODAL)
        alert.setTitle(title)
        alert.setHeaderText(header)
        alert.setContentText(content)
        alert
    }

    def info( title: String, content: String, header: String = null): Unit = {
        custom(AlertType.INFORMATION, title, content, header).showAndWait()
    }

    def warning( title: String, content: String, header: String = null): Unit = {
        custom(AlertType.WARNING, title, content, header).showAndWait()
    }


    def error( title: String, content: String, header: String = null): Unit = {
        custom(AlertType.ERROR, title, content, header).showAndWait()
    }

    def exception( ex: Throwable, title: String = "Exception", header: String = null ): Unit = {
        val alert = new javafx.scene.control.Alert(AlertType.ERROR)
        alert.setTitle(title)
        alert.setHeaderText(header)
        alert.setContentText(ex.getLocalizedMessage)

        val sw = new StringWriter
        val pw = new PrintWriter(sw)
        ex.printStackTrace(pw)
        val exceptionText = sw.toString

        val label = new Label("The exception stacktrace was:")

        val textArea = new TextArea(exceptionText)
        textArea.setEditable(false)
        textArea.setWrapText(true)
        textArea.setMaxWidth(Double.MaxValue)
        textArea.setMaxHeight(Double.MaxValue)

        val expContent = new BorderPane(textArea)
        expContent.setTop(label)
        expContent.setMaxWidth(Double.MaxValue)
        alert.getDialogPane.setExpandableContent(expContent)

        alert.showAndWait()
    }

    def confirmation( title: String, content: String, header: String = null): Boolean = {
        custom(AlertType.CONFIRMATION, title, content, header)
             .showAndWait().asScala
             .map( _ == ButtonType.OK).get
    }



}
