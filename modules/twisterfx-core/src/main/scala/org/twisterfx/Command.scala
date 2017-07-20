package org.twisterfx

import java.util
import java.util.Collections
import javafx.beans.binding.Bindings
import javafx.beans.property._
import javafx.collections.ListChangeListener.Change
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.KeyCombination

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Basic command trait with all relevant properties
  */
trait Command {

    // text which usually shows up on the button ot menu item
    lazy val textProperty: StringProperty = new SimpleStringProperty
    def text: String = textProperty.get
    def text_=(value: String): Unit = textProperty.set(value)

    // long text is used mostly on tooltips
    lazy val longTextProperty: StringProperty = new SimpleStringProperty(null)
    def longText: String = longTextProperty.get
    def longText_=(value: String): Unit = longTextProperty.set(value)

    // graphic shown on the buttons or menu items
    lazy val graphicProperty: ObjectProperty[Node] = new SimpleObjectProperty[Node]
    def graphic: Node = graphicProperty.get
    def graphic_=(value: Node): Unit = graphicProperty.set(value)

    // disabled status propagated to associated controls
    lazy val disabledProperty: BooleanProperty = new SimpleBooleanProperty
    def disabled: Boolean = disabledProperty.get()
    def disabled_=(value: Boolean): Unit = disabledProperty.set(value)

    // accelerator assigned to the action
    lazy val acceleratorProperty: ObjectProperty[KeyCombination] = new SimpleObjectProperty[KeyCombination]
    def accelerator: KeyCombination = acceleratorProperty.get
    def accelerator_=(value: KeyCombination): Unit = acceleratorProperty.set(value)

    // style classes propagated to related controls
    lazy val styleClass: ObservableList[String] = FXCollections.observableArrayList[String]

    /**
      * Called on command execution
      *
      * @param e action event
      */
    def perform(e: ActionEvent): Unit = {}

}

/**
  * Command creation tools
  */
object Command {

    def apply( text: String,
               longText: String = null,
               graphic: Node = null,
               disabled: Boolean = false,
               accelerator: KeyCombination = null,
               styleClasses: Iterable[String] = List())(action: ActionEvent => Unit): Command = {

        val cmd = new Command { override def perform(e: ActionEvent): Unit = action(e) }
        cmd.text = text
        cmd.longText = longText
        cmd.graphic = graphic
        cmd.disabled = disabled
        cmd.accelerator = accelerator
        cmd.styleClass.setAll(styleClasses.asJavaCollection)
        cmd

    }

}

/**
  * Command which performs no action
  */
trait MutedCommand extends Command {
    final override def perform(e: ActionEvent): Unit = {} // no-op
}

/**
  * Trait to add a selectable property to commands
  */
trait Selectable {

    // selected used mostly on check and radio buttons and menu items
    lazy val selectedProperty: BooleanProperty = new SimpleBooleanProperty

    def selected: Boolean = selectedProperty.get()

    def selected_=(value: Boolean): Unit = selectedProperty.set(value)

}

/**
  * Group of commands with common name.
  * Represented by dropdown button or submenu item
  *
  * @param text        group text
  * @param subcommands commands in the group
  */
class CommandGroup(text: String, graphic: Node = null)(subcommands: Command*) extends MutedCommand {

    textProperty.set(text)
    graphicProperty.set(graphic)

    val commands: ObservableList[Command] = FXCollections.observableArrayList[Command](
        Option(subcommands).map(_.asJavaCollection).getOrElse(Collections.emptyList())
    )

}

/**
  * Command represented by check menu item or toggle check button
  */
class CommandCheck(text: String, graphic: Node = null) extends MutedCommand with Selectable {
    textProperty.set(text)
    graphicProperty.set(graphic)

}

/**
  * Command represented by radio menu item or toggle radio button
  *
  * @param groupId allows for grouping of radio items using toggle groups
  */
class CommandRadio(text: String, graphic: Node = null)(val groupId: String) extends MutedCommand with Selectable {
    textProperty.set(text)
    graphicProperty.set(graphic)
}

/**
  * Represents separator either for toolbar or menu
  */
object CommandSeparator extends MutedCommand


object CommandTools {

    implicit class CommandImplicits(cmd: Command) {

        /**
          * Create a button bound to the command properties.
          * MenuButton is created for CommandGroup
          *
          * @param graphicOnly show only button graphics
          * @return newly created button
          */
        def toButton(graphicOnly: Boolean = false)(implicit groupCache: mutable.Map[String, ToggleGroup] = mutable.Map()): ButtonBase = {

            val button = cmd match {

                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new MenuButton) { (btn, c) =>
                        btn.getItems.add(c.toMenuItem)
                        btn
                    }
                case c: CommandCheck =>
                    val btn = new ToggleButton
                    c.selectedProperty.bindBidirectional(btn.selectedProperty)
                    btn
                case c: CommandRadio =>
                    val btn = new ToggleButton
                    btn.setToggleGroup(groupCache.getOrElseUpdate(c.groupId, new ToggleGroup))
                    c.selectedProperty.bindBidirectional(btn.selectedProperty)
                    btn
                case c: Command =>
                    val btn = new Button
                    btn.setOnAction(c.perform)
                    btn
            }

            if (graphicOnly) {
                Bindings.when(button.graphicProperty().isNotNull)
                    .`then`(ContentDisplay.GRAPHIC_ONLY)
                    .otherwise(ContentDisplay.CENTER)
            }

            button.textProperty.bind(cmd.textProperty)
            button.graphicProperty.bind(cmd.graphicProperty)
            button.disableProperty.bindBidirectional(cmd.disabledProperty)
            bindStyleClass(cmd.styleClass, button.getStyleClass)

            def resetAccelerator() = {
                Option(button.getScene).foreach {
                    _.getAccelerators.put(cmd.accelerator, () => button.fire())
                }
            }

            button.sceneProperty().addListener(( _, _, _) => resetAccelerator())
            cmd.acceleratorProperty.addListener(( _, _, _) => resetAccelerator())
            resetAccelerator()

            cmd.longTextProperty.addListener { (_, _, txt) =>
                button.setTooltip(Option(txt).map(new Tooltip(_)).orNull)
            }

            button

        }

        /**
          * Creates a menu item bound to the command properties
          * Menu is created for CommandGroup
          *
          * @return newly created menu item
          */
        def toMenuItem(implicit groupCache: mutable.Map[String, ToggleGroup] = mutable.Map()): MenuItem = {

            val menuItem = cmd match {
                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new Menu) { (menu, c) =>
                        menu.getItems.add(c.toMenuItem)
                        menu
                    }
                case CommandSeparator => new SeparatorMenuItem
                case c: CommandCheck =>
                    val mi = new CheckMenuItem
                    mi.selectedProperty().bindBidirectional(c.selectedProperty)
                    mi
                case c: CommandRadio =>
                    val mi = new RadioMenuItem
                    mi.setToggleGroup(groupCache.getOrElseUpdate(c.groupId, new ToggleGroup))
                    mi.selectedProperty().bindBidirectional(c.selectedProperty)
                    mi
                case c: Command =>
                    val mi = new MenuItem
                    mi.setOnAction(c.perform)
                    mi
            }

            menuItem.textProperty.bind(cmd.textProperty)
            menuItem.graphicProperty.bind(cmd.graphicProperty)
            menuItem.disableProperty.bindBidirectional(cmd.disabledProperty)
            menuItem.acceleratorProperty.bind(cmd.acceleratorProperty)
            bindStyleClass(cmd.styleClass, menuItem.getStyleClass)

            menuItem

        }

        private def bindStyleClass(source: ObservableList[String], dest: ObservableList[String]): Unit = {
            source.addListener { change: Change[_ <: String] =>
                val toAdd = new util.ArrayList[String]
                val toRemove = new util.ArrayList[String]
                while (change.next) {
                    if (change.wasAdded()) toAdd.addAll(change.getAddedSubList)
                    if (change.wasRemoved()) toRemove.addAll(change.getRemoved)
                }
                dest.removeAll(toRemove)
                dest.addAll(toAdd)
                ()
            }


        }

    }


    // implicit methods for command collections

    implicit class CommandsImplicits(commands: Iterable[Command]) {

        /**
          * Converts a set of commands to a toolbar
          *
          * @param toolbar toolbar which has to be used. new one is created by default
          * @return updated toolbar
          */
        def toToolBar(toolbar: => ToolBar = new ToolBar): ToolBar = {

            implicit val groupCache = mutable.Map[String, ToggleGroup]()

            commands.foldLeft(toolbar) { (tb, cmd) =>
                val item = cmd match {
                    case CommandSeparator =>
                        val separator = new Separator
                        separator.orientationProperty().bind(
                            Bindings.when(toolbar.orientationProperty().isEqualTo(Orientation.HORIZONTAL))
                                .`then`(Orientation.VERTICAL)
                                .otherwise(Orientation.HORIZONTAL)
                        )
                        separator
                    case _: Command => cmd.toButton(graphicOnly = true)
                }

                tb.getItems.add(item)
                tb

            }
        }

        /**
          * Converts a set of commands to a menu bar
          *
          * @param menubar menu bar which has to be used. new one is created by default
          * @return updated menu bar
          */
        def toMenu(menubar: => MenuBar = new MenuBar): MenuBar = {
            implicit val groupCache = mutable.Map[String, ToggleGroup]()

            commands.foldLeft(menubar) { (mb, cmd) =>
                cmd.toMenuItem match {
                    case m: Menu => menubar.getMenus.add(m)
                    case _ =>
                }
                mb
            }
        }

        /**
          * Converts a set of commands to a context menu
          *
          * @param menu context menu which has to be used. new one is created by default
          * @return updated context menu
          */
        def toContextMenu(menu: => ContextMenu = new ContextMenu): ContextMenu = {
            implicit val groupCache = mutable.Map[String, ToggleGroup]()

            commands.foldLeft(menu) { (mn, cmd) =>
                mn.getItems.add(cmd.toMenuItem)
                mn
            }
        }
    }

}
