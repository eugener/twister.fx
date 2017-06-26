package org.twisterfx

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.{ListView, MultipleSelectionModel, TableView}

import scala.collection.JavaConverters._


object
ListCommands {

    implicit class TableViewImplicits[T]( val table: TableView[T] ) {

//         def createInsertCommand( text: String = "Insert" ): Unit = {
//             val cmd = new CommandInsert[T](table)
//             cmd.text = text
//         }


    }


    /**
      * Generic trait for a command operating on control with item collection
      * @tparam T item type
      */
    trait CollectionCommand[T] extends Command {

        protected def getSelectionModel: MultipleSelectionModel[T]
        protected def getItems: ObservableList[T]
        protected def getDisabledProperty: BooleanProperty

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

        protected abstract def insert(item: T): T

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem = getSelectionModel.getSelectedItem
            val newItem      = insert( selectedItem )
            getItems.add(newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionUpdate[T] extends CollectionCommand[T] {

        protected abstract def update(item: T): T

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem  = getSelectionModel.getSelectedItem
            val selectedIndex = getSelectionModel.getSelectedIndex
            val newItem       = update( selectedItem )
            getItems.add( math.max(selectedIndex,0), newItem)
            getSelectionModel.select(newItem)
        }

    }

    abstract class CommandCollectionRemove[T] extends CollectionCommand[T] {

        protected abstract def remove(item: Iterable[T]): Boolean

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
