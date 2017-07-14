package org.twisterfx

import javafx.scene.control.Alert
import javafx.scene.layout.BorderPane
import javafx.stage.{Modality, Stage}

import com.sun.javafx.stage.StageHelper


object Alerts {

    import java.io.{PrintWriter, StringWriter}
    import javafx.scene.control.Alert.AlertType
    import javafx.scene.control.{ButtonType, Label, TextArea}

    import scala.compat.java8.OptionConverters._


    private def custom( alertType: AlertType, title: String, content: String, header: String = null): Alert = {
        val alert = new javafx.scene.control.Alert(alertType)

        alert.initModality( Modality.WINDOW_MODAL)
        alert.setTitle(title)
        alert.setHeaderText(header)
        alert.setContentText(content)

        getActiveStage.foreach{ stage =>

            // assign active stage
            alert.initOwner(stage)

            // center alert on parent stage
            val stageCenterX = stage.getX + stage.getWidth / 2d
            val stageCenterY = stage.getY + stage.getHeight / 2d
            alert.setX(stageCenterX - alert.getWidth / 2d)
            alert.setY(stageCenterY - alert.getHeight / 2d)

        }

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
        val alert = custom(AlertType.ERROR, title, ex.getLocalizedMessage, header)

        val sw = new StringWriter
        val pw = new PrintWriter(sw)
        ex.printStackTrace(pw)
        val exceptionText = sw.toString

        val textArea = new TextArea(exceptionText)
        textArea.setEditable(false)
        textArea.setWrapText(true)
        textArea.setMaxWidth(Double.MaxValue)
        textArea.setMaxHeight(Double.MaxValue)

        val expContent = new BorderPane(textArea)
        expContent.setTop(new Label("The exception stacktrace was:")) // TODO externalize
        expContent.setMaxWidth(Double.MaxValue)
        alert.getDialogPane.setExpandableContent(expContent)

        alert.showAndWait()
    }

    //TODO cutomize button text (YES/NO for example)
    def confirmation( title: String, content: String, header: String = null): Boolean = {
        custom(AlertType.CONFIRMATION, title, content, header)
             .showAndWait().asScala
             .map( _ == ButtonType.OK).get
    }



}
