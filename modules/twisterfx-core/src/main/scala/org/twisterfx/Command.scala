package org.twisterfx

import javafx.beans.binding.Bindings
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.KeyCombination

import collection.JavaConverters._

trait Command {

    lazy val textProperty: StringProperty = new SimpleStringProperty
    def text: String = textProperty.get
    def text_=(value: String): Unit = textProperty.set(value)

    lazy val longTextProperty: StringProperty = new SimpleStringProperty
    def longText: String = longTextProperty.get
    def longText_=(value: String): Unit = longTextProperty.set(value)

    lazy val graphicProperty: ObjectProperty[Node] = new SimpleObjectProperty[Node]
    def graphic: Node = graphicProperty.get
    def graphic_=(value: Node): Unit = graphicProperty.set(value)

    lazy val disabledProperty: BooleanProperty = new SimpleBooleanProperty
    def disabled: Boolean = disabledProperty.get()
    def disabled_=(value: Boolean): Unit = disabledProperty.set(value)

    lazy val acceleratorProperty: ObjectProperty[KeyCombination] = new SimpleObjectProperty[KeyCombination]
    def accelerator: KeyCombination = acceleratorProperty.get
    def accelerator_=(value: KeyCombination): Unit = acceleratorProperty.set(value)

    lazy val selectedProperty: BooleanProperty = new SimpleBooleanProperty
    def selected: Boolean = selectedProperty.get()
    def selected_=(value: Boolean): Unit = selectedProperty.set(value)

//    lazy val styleProperty: StringProperty = new SimpleStringProperty
//    def style: String = styleProperty.get
//    def style_=(value: String): Unit = styleProperty.set(value)

    def perform( e: ActionEvent ): Unit = {}

}

object Command {

    def apply( text: String, graphic: Node = null , longText: String = null )( action: ActionEvent => Unit): Command =  {
        val cmd = new Command {
            override def perform(e: ActionEvent): Unit = action(e)
        }
        cmd.text = text
        cmd.graphic = graphic
        cmd
    }

    def group(text: String)(subCommands: Command* ): Command = {
        new CommandGroup(text)(subCommands:_*)
    }
}

class CommandGroup( groupText: String )( subcommands: Command* ) extends Command {
    text = groupText
    val commands: ObservableList[Command] = FXCollections.observableArrayList[Command]()
    Option(subcommands).foreach( cmds => commands.addAll(cmds.asJava))
    final override def perform( e: ActionEvent ): Unit = {} // no-op
}

object CommandSeparator extends Command


object CommandTools {

    implicit class CommandImplicits( cmd: Command ) {

        /**
          * Create a button bound the command properties.
          * MenuButton is created for CommandGroup
          * @param contentDisplay buttons content display type
          * @return newly created button
          */
        def toButton( contentDisplay: ContentDisplay = null ): ButtonBase = {

            val button = cmd match {

                //TODO Check and Radio buttons
                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new MenuButton){ (btn,c) =>
                        btn.getItems.add(c.toMenuItem)
                        btn
                    }
                case c: Command =>
                    val btn = new Button
                    btn.setOnAction(c.perform)
                    btn
            }

            Option(contentDisplay).foreach(button.setContentDisplay)

            button.textProperty.bind(cmd.textProperty)
            button.graphicProperty().bind(cmd.graphicProperty)
            button.disableProperty.bind(cmd.disabledProperty)

            //TODO bind style without losing existing control styles
//            button.styleProperty().bind(cmd.styleProperty)
            //TODO set accelerator

            val tooltip = new Tooltip
            tooltip.textProperty().bind(cmd.longTextProperty)
            button.setTooltip(tooltip)

            button

        }

        /**
          * Creates a menu item bound to the command properties
          * Menu is created for CommandGroup
          * @return newly created menu item
          */
        def toMenuItem: MenuItem = {

            val menuItem = cmd match {
                //TODO Check and Radio menu items
                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new Menu){ (menu,c) =>
                        menu.getItems.add(c.toMenuItem)
                        menu
                    }
                case CommandSeparator => new SeparatorMenuItem
                case c : Command =>
                    val mi = new MenuItem
                    mi.setOnAction(c.perform)
                    mi
            }

            menuItem.textProperty.bind(cmd.textProperty)
            menuItem.graphicProperty.bind(cmd.graphicProperty)
            menuItem.disableProperty.bind(cmd.disabledProperty)
            //TODO bind style without losing existing control styles
//            menuItem.styleProperty.bind(cmd.styleProperty)
            menuItem.acceleratorProperty.bind(cmd.acceleratorProperty)

            //TODO tooltip

            menuItem

        }

    }


    // implicit methods for command collections

    implicit class CommandsImplicits(commands: Iterable[Command]) {

        /**
          * Converts a set of commands to a toolbar
          * @param toolbar toolbar which has to be used. new one is created by default
          * @return updated toolbar
          */
        def toToolBar( toolbar: => ToolBar = new ToolBar ): ToolBar = {

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
                    case _: Command => cmd.toButton(ContentDisplay.GRAPHIC_ONLY)
                }

                tb.getItems.add(item)
                tb

            }
        }

        /**
          * Converts a set of commands to a menubar
          * @param menubar menubar which has to be used. new one is created by default
          * @return updated menubar
          */
        def toMenu(menubar: => MenuBar = new MenuBar): MenuBar = {
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
          * @param menu context menu which has to be used. new one is created by default
          * @return updated context menu
          */
        def toContextMenu(menu: => ContextMenu = new ContextMenu): ContextMenu = {
            commands.foldLeft(menu) { (mn, cmd) =>
                mn.getItems.add(cmd.toMenuItem)
                mn
            }
        }
    }

}
