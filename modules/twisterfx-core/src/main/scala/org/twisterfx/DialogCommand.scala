package org.twisterfx

import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType


trait DialogCommand extends Command {

    protected val buttonData: ButtonData

    def getButtonType = new ButtonType( text, buttonData)
}

case object DialogOKCommand extends DialogCommand {
    val buttonData = ButtonData.OK_DONE
    text = "OK"
}

case object DialogCancelCommand extends DialogCommand {
    val buttonData = ButtonData.CANCEL_CLOSE
    text = "Cancel"
}

case object DialogCloseCommand extends DialogCommand {
    val buttonData = ButtonData.CANCEL_CLOSE
    text = "Close"
}
