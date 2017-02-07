package org.twisterfx

import java.util
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

    lazy val styleClass: ObservableList[String] = FXCollections.observableArrayList[String]

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

    def group(text: String, graphic: Node = null)(subCommands: Command* ): Command = {
        new CommandGroup(text)(subCommands:_*)
    }

    def check(text: String, graphic: Node = null): Command = {
        val cmd = new CommandCheck
        cmd.text = text
        cmd.graphic = graphic
        cmd
    }

    def radio( text: String, graphic: Node = null, groupId: String ): Command = {
        val cmd = new CommandRadio(groupId)
        cmd.text = text
        cmd.graphic = graphic
        cmd
    }
}

trait MutedCommand extends Command {
    final override def perform( e: ActionEvent ): Unit = {} // no-op
}

class CommandGroup( groupText: String )( subcommands: Command* ) extends MutedCommand {
    text = groupText
    val commands: ObservableList[Command] = FXCollections.observableArrayList[Command]()
    Option(subcommands).foreach( cmds => commands.addAll(cmds.asJava))

}

class CommandCheck extends MutedCommand
class CommandRadio( val groupId: String )  extends MutedCommand

object CommandSeparator  extends MutedCommand



object CommandTools {

    implicit class CommandImplicits( cmd: Command ) {

        /**
          * Create a button bound the command properties.
          * MenuButton is created for CommandGroup
          * @param contentDisplay buttons content display type
          * @return newly created button
          */
        def toButton( contentDisplay: ContentDisplay = null )( implicit groupCache: mutable.Map[String, ToggleGroup] = mutable.Map() ): ButtonBase = {

            val button = cmd match {

                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new MenuButton){ (btn,c) =>
                        btn.getItems.add(c.toMenuItem)
                        btn
                    }
                case c: CommandCheck =>
                    val btn = new ToggleButton
                    c.selectedProperty.bindBidirectional(btn.selectedProperty)
                    btn
                case c: CommandRadio =>
                    val btn = new ToggleButton
                    btn.setToggleGroup( groupCache.getOrElseUpdate( c.groupId, new ToggleGroup) )
                    c.selectedProperty.bindBidirectional( btn.selectedProperty)
                    btn
                case c: Command =>
                    val btn = new Button
                    btn.setOnAction(c.perform)
                    btn
            }

            Option(contentDisplay).foreach(button.setContentDisplay)

            button.textProperty.bind(cmd.textProperty)
            button.graphicProperty().bind(cmd.graphicProperty)
            button.disableProperty.bindBidirectional(cmd.disabledProperty)
            bindStyleClass(cmd.styleClass,button.getStyleClass)

            def resetAccelerator() = {
                Option(button.getScene).foreach {
                    _.getAccelerators.put(cmd.accelerator, () => button.fire())
                }
            }
            button.sceneProperty().addListener( (_, _, scene) => resetAccelerator())
            cmd.acceleratorProperty.addListener( (_,_,_) => resetAccelerator())
            resetAccelerator()

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
        def toMenuItem( implicit groupCache: mutable.Map[String, ToggleGroup] = mutable.Map() ): MenuItem = {

            val menuItem = cmd match {
                case cg: CommandGroup =>
                    cg.commands.asScala.foldLeft(new Menu){ (menu,c) =>
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
                    mi.setToggleGroup( groupCache.getOrElseUpdate(c.groupId, new ToggleGroup) )
                    mi.selectedProperty().bindBidirectional(c.selectedProperty)
                    mi
                case c : Command =>
                    val mi = new MenuItem
                    mi.setOnAction(c.perform)
                    mi
            }

            menuItem.textProperty.bind(cmd.textProperty)
            menuItem.graphicProperty.bind(cmd.graphicProperty)
            menuItem.disableProperty.bindBidirectional(cmd.disabledProperty)
            menuItem.acceleratorProperty.bind(cmd.acceleratorProperty)
            bindStyleClass(cmd.styleClass,menuItem.getStyleClass)

            menuItem

        }

        private def bindStyleClass( source: ObservableList[String], dest: ObservableList[String]): Unit = {
            source.addListener{ change: Change[ _ <: String] =>
                val toAdd = new util.ArrayList[String]
                val toRemove = new util.ArrayList[String]
                while( change.next ){
                    if ( change.wasAdded() ) toAdd.addAll(change.getAddedSubList)
                    if ( change.wasRemoved() ) toRemove.addAll(change.getRemoved)
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
          * @param toolbar toolbar which has to be used. new one is created by default
          * @return updated toolbar
          */
        def toToolBar( toolbar: => ToolBar = new ToolBar ) : ToolBar = {

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
                    case _: Command => cmd.toButton(ContentDisplay.GRAPHIC_ONLY)
                }

                tb.getItems.add(item)
                tb

            }
        }

        /**
          * Converts a set of commands to a menu bar
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
