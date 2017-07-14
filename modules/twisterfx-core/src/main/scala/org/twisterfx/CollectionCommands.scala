package org.twisterfx

import javafx.beans.binding.Bindings
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

    type InsertAction[T] = T => T
    type UpdateAction[T] = T => T
    type RemoveAction[T] = Iterable[T] => Boolean


    implicit class TableViewImplicits[T]( val tableView: TableView[T] ) {

        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(insertAction: InsertAction[T]): CommandTableViewInsert[T] = {
            new CommandTableViewInsert[T](tableView, insertAction).setup(text, longText, graphic, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(updateAction: UpdateAction[T]): CommandTableViewUpdate[T] = {
            new CommandTableViewUpdate[T](tableView, updateAction).setup(text, longText, graphic, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(removeAction: RemoveAction[T]): CommandTableViewRemove[T] = {
            new CommandTableViewRemove[T](tableView, removeAction).setup(text, longText, graphic, accelerator, styleClasses)
        }

    }


    implicit class ListViewImplicits[T]( val table: ListView[T] ) {

        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(insertAction: InsertAction[T]): CommandListViewInsert[T] = {
            new CommandListViewInsert[T](table, insertAction).setup(text, longText, graphic, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(updateAction: UpdateAction[T]): CommandListViewUpdate[T] = {
            new CommandListViewUpdate[T](table, updateAction).setup(text, longText, graphic, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphic: Node = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(removeAction: RemoveAction[T]): CommandListViewRemove[T] = {
            new CommandListViewRemove[T](table, removeAction).setup(text, longText, graphic, accelerator, styleClasses)
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

        // bind disabled state of the command
        disabledProperty.bind {
            Bindings.createBooleanBinding(
                () => initialEnabledCondition && !enabledCondition,
                getSelectionModel.selectedIndexProperty
            )
        }

    }

    /**
      * Generic implementatoin
      * @tparam T item type
      */
    abstract class CommandCollectionInsert[T]( private val insertAction: InsertAction[T] ) extends CollectionCommand[T] {

        // for insert command selection does not have to be there
        override def initialEnabledCondition: Boolean = false

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem = getSelectionModel.getSelectedItem
            val newItem      = insertAction( selectedItem )
            getItems.add(newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionUpdate[T]( private val updateAction:UpdateAction[T]) extends CollectionCommand[T] {

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem  = getSelectionModel.getSelectedItem
            val selectedIndex = getSelectionModel.getSelectedIndex
            val newItem       = updateAction( selectedItem )
            getItems.add( math.max(selectedIndex,0), newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionRemove[T]( private val removeAction: RemoveAction[T] ) extends CollectionCommand[T] {

        final override def perform(e: ActionEvent): Unit = {
            val selectedItems    = getSelectionModel.getSelectedItems
            val minSelectedIndex = getSelectionModel.getSelectedIndices.asScala.min
            if ( removeAction(selectedItems.asScala) ) {
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

    }

    trait ListViewCommand[T] extends CollectionCommand[T] {

        protected val listView: ListView[T]

        protected def getSelectionModel: MultipleSelectionModel[T] = listView.getSelectionModel
        protected def getItems: ObservableList[T] = listView.getItems

    }


    class CommandListViewInsert[T](override val listView: ListView[T], insertAction: InsertAction[T])
        extends CommandCollectionInsert[T](insertAction) with ListViewCommand[T]

    class CommandListViewUpdate[T](override val listView: ListView[T], updateAction: UpdateAction[T])
        extends CommandCollectionUpdate[T](updateAction) with ListViewCommand[T]

    class CommandListViewRemove[T](override val listView: ListView[T], removeAction: RemoveAction[T])
        extends CommandCollectionRemove[T](removeAction) with ListViewCommand[T]

    class CommandTableViewInsert[T](override val tableView: TableView[T], insertAction: InsertAction[T])
        extends CommandCollectionInsert[T](insertAction) with TableViewCommand[T]

    class CommandTableViewUpdate[T](override val tableView: TableView[T], updateAction: UpdateAction[T])
        extends CommandCollectionUpdate[T](updateAction) with TableViewCommand[T]

    class CommandTableViewRemove[T](override val tableView: TableView[T], removeAction: RemoveAction[T])
        extends CommandCollectionRemove[T](removeAction) with TableViewCommand[T]


}


