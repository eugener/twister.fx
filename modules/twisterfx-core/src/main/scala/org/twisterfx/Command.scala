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

class Command {

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

class CommandGroup( groupText: String )( subcommands: Command* ) extends Command {
    text = groupText
    val commands: ObservableList[Command] = FXCollections.observableArrayList[Command]()
    Option(subcommands).foreach( cmds => commands.addAll(cmds.asJava))
}

object CommandSeparator extends Command


object CommandTools {

    implicit class CommandImplicits( cmd: Command ) {

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

    def buildToolBar(commands: Iterable[Command], toolbar: => ToolBar = new ToolBar ): ToolBar = {

        commands.foldLeft(toolbar){ (tb,cmd) =>
            val item = cmd match {
                case CommandSeparator =>
                    val separator = new Separator
                    separator.orientationProperty().bind(
                        Bindings.when(toolbar.orientationProperty().isEqualTo(Orientation.HORIZONTAL))
                                .`then`(Orientation.VERTICAL)
                                .otherwise(Orientation.HORIZONTAL)
                    )
                    separator
                case _:Command => cmd.toButton(ContentDisplay.GRAPHIC_ONLY)
            }

            toolbar.getItems.add(item)
            toolbar

        }
    }

    def buildMenu(commands: Iterable[Command], menubar: => MenuBar = new MenuBar ): MenuBar = {
        commands.foldLeft(menubar) { (mb, cmd) =>
            cmd.toMenuItem match {
                case m: Menu =>  menubar.getMenus.add(m)
                case _ =>
            }
            menubar
        }
    }

}
