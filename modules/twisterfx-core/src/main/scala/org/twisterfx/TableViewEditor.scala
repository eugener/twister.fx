package org.twisterfx
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.geometry.Orientation
import javafx.scene.control.{TableView, ToolBar}
import javafx.scene.layout.BorderPane

class TableViewEditor[T] extends View {

    override val root: BorderPane = new BorderPane()

    protected val toolbar = new ToolBar()
    protected val tableView = new TableView[T]()

    val toolBarOrientationProperty: ObjectProperty[Orientation] = new SimpleObjectProperty[Orientation](null)
    def toolBarOrientation: Orientation = toolBarOrientationProperty.get
    def toolBarOrientation_=(value: Orientation): Unit = toolBarOrientationProperty.set(value)

    toolBarOrientationProperty.addListener{ (_,oldo,newo) =>
        if ( newo != oldo ) {
            root.getChildren.remove(toolbar)
            toolbar.setOrientation( newo match {
                case Orientation.VERTICAL =>
                    root.setRight(toolbar)
                    Orientation.VERTICAL
                case _ =>
                    root.setTop(toolbar)
                    Orientation.HORIZONTAL
            })
        }
    }

    toolBarOrientation = Orientation.HORIZONTAL
    root.setCenter(tableView)

}
