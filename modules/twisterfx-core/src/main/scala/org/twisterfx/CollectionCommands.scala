package org.twisterfx

import javafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.{ListView, MultipleSelectionModel, TableView}
import javafx.scene.input.KeyCombination

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, reflectiveCalls}


object CollectionCommands {

    import Command._

    private implicit class CommandImplicits[T <: Command](cmd: T) {
        def setup(text: String = "Insert",
                  longText: String = null,
                  graphicBuilder: NodeBuilder = null,
                  accelerator: KeyCombination = null,
                  styleClasses: Iterable[String]): T = {
            cmd.text = text
            cmd.longText = longText
            cmd.graphicBuilder = graphicBuilder
            cmd.accelerator = accelerator
            cmd.styleClass.setAll(styleClasses.asJavaCollection)
            cmd
        }
    }

    type InsertAction[T] = T => Option[T]
    type UpdateAction[T] = T => Option[T]
    type RemoveAction[T] = Iterable[T] => Boolean


    protected trait CommandTarget[T] <: {
        def getSelectionModel: MultipleSelectionModel[T]
        def getItems: ObservableList[T]
        def scrollTo( index: Int ): Unit
        def scrollTo( index: T ): Unit
    }


    private implicit def toTarget[T](tableView: TableView[T]): CommandTarget[T] = new CommandTarget[T] {
        override def getSelectionModel: MultipleSelectionModel[T] = tableView.getSelectionModel
        override def getItems: ObservableList[T] = tableView.getItems
        override def scrollTo(item: Int): Unit = tableView.scrollTo(item)
        override def scrollTo(index: T): Unit = tableView.scrollTo(index)
    }

    private implicit def toTarget[T](listView: ListView[T]): CommandTarget[T] = new CommandTarget[T] {
        override def getSelectionModel: MultipleSelectionModel[T] = listView.getSelectionModel
        override def getItems: ObservableList[T] = listView.getItems
        override def scrollTo(item: Int): Unit = listView.scrollTo(item)
        override def scrollTo(index: T): Unit = listView.scrollTo(index)
    }

    implicit class TableViewImplicits[T]( val tableView: TableView[T] ) {


        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(insertAction: InsertAction[T]): CommandCollectionInsert[T] = {
            new CommandCollectionInsert[T](tableView, insertAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(updateAction: UpdateAction[T]): CommandCollectionUpdate[T] = {
            new CommandCollectionUpdate[T](tableView, updateAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(removeAction: RemoveAction[T]): CommandCollectionRemove[T] = {
            new CommandCollectionRemove[T](tableView, removeAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)
        }

    }


    implicit class ListViewImplicits[T]( val listView: ListView[T] ) {

        def insertCommand(text: String = "Insert",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(insertAction: InsertAction[T]): CommandCollectionInsert[T] = {
            new CommandCollectionInsert[T](listView, insertAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)

        }

        def updateCommand(text: String = "Update",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(updateAction: UpdateAction[T]): CommandCollectionUpdate[T] = {
            new CommandCollectionUpdate[T](listView, updateAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)

        }

        def removeCommand(text: String = "Remove",
                          longText: String = null,
                          graphicBuilder: NodeBuilder = null,
                          accelerator: KeyCombination = null,
                          styleClasses: Iterable[String] = List())(removeAction: RemoveAction[T]): CommandCollectionRemove[T] = {
            new CommandCollectionRemove[T](listView, removeAction).setup(text, longText, graphicBuilder, accelerator, styleClasses)
        }

    }



    /**
      * Generic trait for a command operating on control with item collection
      * @tparam T item type
      */
    protected trait CollectionCommand[T] extends Command {

        protected val target: CommandTarget[T]

        protected def initialEnabledCondition: Boolean = target.getSelectionModel.getSelectedItems.isEmpty

        // override to provide condition enabling the command
        protected def enabledCondition: Boolean = false

        // bind disabled state of the command
        disabledProperty.bind {
            Bindings.createBooleanBinding(
                () => initialEnabledCondition && !enabledCondition,
                target.getSelectionModel.selectedIndexProperty
            )
        }

        protected def select(item: Int): Unit = {
            target.getSelectionModel.select(item)
            target.scrollTo(item)
        }

        protected def select(index: T): Unit = {
            target.getSelectionModel.select(index)
            target.scrollTo(index)
        }

    }

    /**
      * Generic implementatoin
      * @tparam T item type
      */
    class CommandCollectionInsert[T]( override val target: CommandTarget[T], private val insertAction: InsertAction[T] ) extends CollectionCommand[T] {

        // for insert command selection does not have to be there
        override def initialEnabledCondition: Boolean = false

        final override def perform(e: ActionEvent): Unit = {
            insertAction( target.getSelectionModel.getSelectedItem ).foreach{ newItem =>
                target.getItems.add(newItem)
                target.getSelectionModel.clearSelection()
                select(newItem)
            }

        }

    }

    class CommandCollectionUpdate[T]( override val target: CommandTarget[T], private val updateAction:UpdateAction[T]) extends CollectionCommand[T] {

        final override def perform(e: ActionEvent): Unit = {
            val selectedItem  = target.getSelectionModel.getSelectedItem
            val selectedIndex = target.getSelectionModel.getSelectedIndex
            updateAction( selectedItem ).foreach{ newItem =>

                // check if new item is truly same object ref as selected one
                // in that case collection update is not required
                val collectionUpdateRequired = selectedItem match {
                    case ref: AnyRef => ref.ne(newItem.asInstanceOf[AnyRef])
                    case _ => true
                }

                if ( collectionUpdateRequired ) {
                    target.getItems.remove(selectedIndex)
                    target.getItems.add(math.max(selectedIndex, 0), newItem)
                    select(newItem)
                }
            }

        }

    }

    class CommandCollectionRemove[T]( override val target: CommandTarget[T], private val removeAction: RemoveAction[T] ) extends CollectionCommand[T] {

        final override def perform(e: ActionEvent): Unit = {
            val selectedItems    = target.getSelectionModel.getSelectedItems
            val minSelectedIndex = target.getSelectionModel.getSelectedIndices.asScala.min
            if ( removeAction(selectedItems.asScala) ) {
                target.getItems.removeAll(selectedItems)
                val newSelectionIndex: Int = minSelectedIndex match {
                    case idx if idx < 0 || target.getItems.isEmpty => 0
                    case idx if idx > target.getItems.size-1 => target.getItems.size-1
                    case idx                                 => idx
                }
                select(newSelectionIndex)
            }
        }

    }

}


