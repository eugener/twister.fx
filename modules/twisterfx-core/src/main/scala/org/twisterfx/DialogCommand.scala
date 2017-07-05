package org.twisterfx

import javafx.event.ActionEvent
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{ButtonType, Dialog}

class DialogCommand( text: String, buttonData: ButtonData)(action: Dialog[_] => Unit) extends Command {
    val buttonType = new ButtonType( text, buttonData)
    final override def perform( e: ActionEvent): Unit = action(e.getSource.asInstanceOf[Dialog[_]])
    final def perform( dialog: Dialog[_]): Unit  = action(dialog)
}

case object DialogOKCommand extends DialogCommand("OK", ButtonData.OK_DONE )( _.close)
case object DialogCancelCommand extends DialogCommand("Cancel", ButtonData.CANCEL_CLOSE)( _.close)
case object DialogCloseCommand extends DialogCommand("Close", ButtonData.CANCEL_CLOSE)( _.close)
