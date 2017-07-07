package org.twisterfx

import javafx.event.ActionEvent
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{ButtonType, Dialog}

class DialogCommand(text: String, buttonData: ButtonData, isClosing: Boolean = false )
                   (action: ActionEvent => Unit) extends Command {
    val buttonType = new ButtonType( text, buttonData )
    final override def perform( e: ActionEvent): Unit = {
        action(e)
        if (!isClosing) e.consume() // don't close the dialog
    }
}

object DialogCommand {
    case object OK extends DialogCommand("OK", ButtonData.OK_DONE, true )(_=>())
    case object Cancel extends DialogCommand("Cancel", ButtonData.CANCEL_CLOSE, true)(_=>())
    case object Close extends DialogCommand("Close", ButtonData.CANCEL_CLOSE, true)(_=>())
}


