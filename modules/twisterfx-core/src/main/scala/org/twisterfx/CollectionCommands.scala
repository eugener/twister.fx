package org.twisterfx

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.{ListView, MultipleSelectionModel, TableView}
import javafx.scene.input.KeyCombination

import scala.collection.JavaConverters._


object CollectionCommands {

    private implicit class CommandImplicits[T <: Command](cmd: T) {
        def setup(text: String = "Insert",
                  longText: String = null,
                  graphic: Node = null,
                  accelerator: KeyCombination = null,
                  styleClasses: Iterable[String]): T = {
            cmd.text = text
            cmd.longText = longText
            cmd.graphic = graphic
            cmd.accelerator = accelerator
            cmd.styleClass.setAll(styleClasses.asJavaCollection)
            cmd
        }
    }

    implicit class TableViewImplicits[T]( val table: TableView[T] ) {

        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: T => T): CommandTableViewInsert[T] = {
            new CommandTableViewInsert[T](table) {
                override protected def insert(item: T): T = handle(item)
            }.setup(text, longText, graphic, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: T => T): CommandTableViewUpdate[T] = {
            new CommandTableViewUpdate[T](table) {
                override protected def update(item: T): T = handle(item)
            }.setup(text, longText, graphic, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: Iterable[T] => Boolean): CommandTableViewRemove[T] = {
            new CommandTableViewRemove[T](table) {
                protected def remove(items: Iterable[T]): Boolean = handle(items)
            }.setup(text, longText, graphic, accelerator, styleClasses)
        }

    }


    implicit class ListViewImplicits[T]( val table: ListView[T] ) {

        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: T => T): CommandListViewInsert[T] = {
            new CommandListViewInsert[T](table) {
                override protected def insert(item: T): T = handle(item)
            }.setup(text, longText, graphic, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: T => T): CommandListViewUpdate[T] = {
            new CommandListViewUpdate[T](table) {
                override protected def update(item: T): T = handle(item)
            }.setup(text, longText, graphic, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(handle: Iterable[T] => Boolean): CommandListViewRemove[T] = {
            new CommandListViewRemove[T](table) {
                protected def remove(items: Iterable[T]): Boolean = handle(items)
            }.setup(text, longText, graphic, accelerator, styleClasses)
        }

    }
    
    /**
      * Generic trait for a command operating on control with item collection
      * @tparam T item type
      */
    trait CollectionCommand[T] extends Command {

        protected def getSelectionModel: MultipleSelectionModel[T]
        protected def getItems: ObservableList[T]

        protected def initialEnabledCondition: Boolean = getSelectionModel.getSelectedItems.isEmpty

        // override to provide condition enabling the command
        protected def enabledCondition: Boolean = false

    }

    /**
      * Generic implementatoin
      * @tparam T item type
      */
    abstract class CommandCollectionInsert[T] extends CollectionCommand[T] {

        // for insert command selection does not have to be there
        override def initialEnabledCondition: Boolean = false

        protected def insert(item: T): T

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem = getSelectionModel.getSelectedItem
            val newItem      = insert( selectedItem )
            getItems.add(newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionUpdate[T] extends CollectionCommand[T] {

        protected def update(item: T): T

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem  = getSelectionModel.getSelectedItem
            val selectedIndex = getSelectionModel.getSelectedIndex
            val newItem       = update( selectedItem )
            getItems.add( math.max(selectedIndex,0), newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionRemove[T] extends CollectionCommand[T] {

        protected def remove(items: Iterable[T]): Boolean

        final override def perform(e: ActionEvent): Unit = {
            val selectedItems    = getSelectionModel.getSelectedItems
            val minSelectedIndex = getSelectionModel.getSelectedIndices.asScala.min
            if ( remove(selectedItems.asScala) ) {
                getItems.removeAll(selectedItems)
                val newSelectionIndex: Int = minSelectedIndex match {
                    case idx if idx < 0 || getItems.isEmpty => 0
                    case idx if idx > getItems.size-1       => getItems.size-1
                    case idx                                => idx
                }
                getSelectionModel.select(newSelectionIndex)
            }
        }

    }

    trait TableViewCommand[T] extends CollectionCommand[T] {

        protected val tableView: TableView[T]

        protected def getSelectionModel: MultipleSelectionModel[T] = tableView.getSelectionModel
        protected def getItems: ObservableList[T] = tableView.getItems

        tableView.disableProperty().bind {
            Bindings.createBooleanBinding(
                () => initialEnabledCondition && !enabledCondition,
                tableView.selectionModelProperty
            )
        }

    }


    trait ListViewCommand[T] extends CollectionCommand[T] {

        protected val listView: ListView[T]

        protected def getSelectionModel: MultipleSelectionModel[T] = listView.getSelectionModel
        protected def getItems: ObservableList[T] = listView.getItems

        listView.disableProperty().bind {
            Bindings.createBooleanBinding(
                () => initialEnabledCondition && !enabledCondition,
                listView.selectionModelProperty
            )
        }

    }


    abstract class CommandListViewInsert[T](override val listView: ListView[T]) extends CommandCollectionInsert[T] with ListViewCommand[T]
    abstract class CommandListViewUpdate[T](override val listView: ListView[T]) extends CommandCollectionUpdate[T] with ListViewCommand[T]
    abstract class CommandListViewRemove[T](override val listView: ListView[T]) extends CommandCollectionRemove[T] with ListViewCommand[T]

    abstract class CommandTableViewInsert[T](override val tableView: TableView[T]) extends CommandCollectionInsert[T] with TableViewCommand[T]
    abstract class CommandTableViewUpdate[T](override val tableView: TableView[T]) extends CommandCollectionUpdate[T] with TableViewCommand[T]
    abstract class CommandTableViewRemove[T](override val tableView: TableView[T]) extends CommandCollectionRemove[T] with TableViewCommand[T]
    
    
}
