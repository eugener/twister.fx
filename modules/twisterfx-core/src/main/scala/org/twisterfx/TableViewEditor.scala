package org.twisterfx
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.beans.{InvalidationListener, Observable}
import javafx.collections.{FXCollections, ObservableList}
import javafx.geometry.Orientation
import javafx.scene.control.{TableView, ToolBar}
import javafx.scene.layout.BorderPane

import org.twisterfx.CommandTools._

import scala.collection.JavaConverters._

class TableViewEditor[T] extends View {

    override val root: BorderPane = new BorderPane()

    protected val toolbar = new ToolBar()
    protected val tableView = new TableView[T]()

    // toolbarOrientation property
    val toolBarOrientationProperty: ObjectProperty[Orientation] = new SimpleObjectProperty[Orientation](this, "Toolbar Orientation", null) {
        override def set(newOrientation: Orientation): Unit = {
            if (this.getValue != newOrientation) {
                root.getChildren.remove(toolbar)
                if ( Orientation.VERTICAL == newOrientation ) root.setRight(toolbar) else root.setTop(toolbar)
                toolbar.setOrientation(newOrientation)
            }
            super.set(newOrientation)
        }
    }
    def toolBarOrientation: Orientation = toolBarOrientationProperty.get
    def toolBarOrientation_=(value: Orientation): Unit = toolBarOrientationProperty.set(value)

    toolBarOrientation = Orientation.HORIZONTAL
    root.setCenter(tableView)


    val commands: ObservableList[Command] = FXCollections.observableArrayList()

    commands.addListener( new InvalidationListener {
        override def invalidated(observable: Observable): Unit = {
            toolbar.getItems.clear()
            val newCommands = commands.asScala
            newCommands.toToolBar(toolbar)
            tableView.setContextMenu(newCommands.toContextMenu())
        }
    })


}
