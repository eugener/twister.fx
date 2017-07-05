package org.twisterfx

abstract class ViewController[VIEW <:View, MODEL <: ViewModel[_]] {

    val view : VIEW

    protected def dialogCommands: Set[DialogCommand] = Set(DialogOKCommand, DialogCancelCommand)

    protected def bind( model: MODEL ): Unit

    final def execute( model: MODEL ) : Unit = {
        bind(model)
        try {
            view.showDialog( commands = dialogCommands )
        } finally {
            model.unbind()
        }
    }

}