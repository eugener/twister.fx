package org.twisterfx

import javafx.scene.control.Alert


object Alert {

    import java.io.{PrintWriter, StringWriter}
    import javafx.scene.control.Alert.AlertType
    import javafx.scene.control.{ButtonType, Label, TextArea}
    import javafx.scene.layout.{GridPane, Priority}
    import scala.compat.java8.OptionConverters._



    private def custom( alertType: AlertType, title: String, content: String, header: String = null): Alert = {
        val alert = new javafx.scene.control.Alert(alertType)
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

    def exception( title: String = "Exception", ex: Throwable,  header: String = null ): Unit = {
        val alert = new javafx.scene.control.Alert(AlertType.ERROR)
        alert.setTitle("Exception")
        alert.setHeaderText(header)

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
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)


        val expContent = new GridPane
        expContent.setMaxWidth(Double.MaxValue)
        expContent.add(label, 0, 0)
        expContent.add(textArea, 0, 1)

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane.setExpandableContent(expContent)

        alert.showAndWait()
    }

    def confirmation( title: String, content: String, header: String = null): Boolean = {
        custom(AlertType.CONFIRMATION, title, content, header)
             .showAndWait().asScala
             .map( _ == ButtonType.OK).get
    }



}
