package org.twisterfx.demo

import java.util
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory

import com.gluonhq.ignite.DIContext
import com.gluonhq.ignite.spring.SpringContext
import de.jensd.fx.glyphs.fontawesome.{FontAwesomeIcon, FontAwesomeIconView}
import de.jensd.fx.glyphs.{GlyphIcons, GlyphsBuilder}
import org.springframework.stereotype.Component
import org.twisterfx.{Alerts, App, Command, CommandCheck, CommandGroup, CommandRadio, CommandSeparator, CommandTools, FXMLView, TableViewEditor}

import scala.language.implicitConversions

object TwisterDemo extends App {
    protected def diContext: DIContext = new SpringContext(this, () => util.Arrays.asList("org.twisterfx"))
    override def stop(): Unit = println("stopping")
}

@Component("root.view")
class DemoView extends FXMLView/*("demoview.fxml")*/ {
    title = "TwisterFX Demo Application"
}

class DemoViewController {

    @FXML var tabs: TabPane = _

    @FXML implicit var toolbar: ToolBar = _
    @FXML var menuBar: MenuBar = _
    @FXML var label: Label = _

    lazy val commands = List(
        Command( "Command 1" )(_ => println("Executing command 1") ),
        new CommandGroup( "Command 2")(
            Command("Subcommand 1"){ _ => println("Executing subcommand 1") },
            Command("Subcommand 2"){ _ => println("Executing subcommand 2") },
            Command("Subcommand 3"){ _ => println("Executing subcommand 3") },
            new CommandCheck("Check 1"),
            new CommandRadio("Radio 1")("g1"),
            new CommandRadio("Radio 2")("g1"),
            new CommandRadio("Radio 3")("g1")
        ),
        Command("Command 3")(_ => println("Executing command 3") ),
        new CommandRadio("Radio 21")("g2"),
        new CommandRadio("Radio 22")("g2"),
        new CommandRadio("Radio 23")("g2")
    )

    def initialize(): Unit = {
        import CommandTools._
        commands.toToolBar(toolbar)
        commands.toMenu(menuBar)
        label.setContextMenu(commands.toContextMenu())

        tabs.getTabs.add(new Tab( "Table Commands", new TableCommandDemo().root))
    }


}

class TableCommandDemo extends TableViewEditor[Person] {

    val tcFirstName = new TableColumn[Person,String]("First Name")
    tcFirstName.setCellValueFactory( new PropertyValueFactory[Person, String]("firstName"))

    val tcLastName = new TableColumn[Person,String]("Last Name")
    tcLastName.setCellValueFactory( new PropertyValueFactory[Person, String]("lastName"))

    val tcAge = new TableColumn[Person,Int]("Age")
    tcAge.setCellValueFactory( new PropertyValueFactory[Person, Int]("age"))

    tableView.getColumns.add(tcFirstName)
    tableView.getColumns.add(tcLastName)
    tableView.getColumns.add(tcAge)

    import org.twisterfx.CollectionCommands._

    private implicit def getGlyph( kind: GlyphIcons ): Node = {
        GlyphsBuilder
            .create(classOf[FontAwesomeIconView])
            .glyph(kind)
            .size("16px")
            .styleClass("toolbar-icon")
            .build
    }

    val commandInsert: CommandTableViewInsert[Person] = tableView.insertCommand(
        graphicBuilder = () => FontAwesomeIcon.PLUS) { person =>
        Option(person).map(_.copy( age = person.age + 10)).orElse(Some(Person("123", "123", 123)))
    }

    val commandUpdate: CommandTableViewUpdate[Person] = tableView.updateCommand(
        graphicBuilder = () => FontAwesomeIcon.EDIT ){ person =>
        Some(person.copy( age = person.age + 1))
    }

    val commandRemove: CommandTableViewRemove[Person] = tableView.removeCommand(
        graphicBuilder = () => FontAwesomeIcon.REMOVE ){ person =>
        Alerts.confirmation("Remove Item", "Are you sure?")
    }

    val commandChangeOrientation = Command(
        text = "Orientation",
        longText = "Change toolbar orientation",
        graphicBuilder = () => FontAwesomeIcon.REFRESH )
    { e =>
        val newOrientation = toolBarOrientationProperty.get() match  {
            case Orientation.HORIZONTAL => Orientation.VERTICAL
            case Orientation.VERTICAL => Orientation.HORIZONTAL
        }
        toolBarOrientationProperty.set(newOrientation)
    }


    val tableCommands = List(commandInsert, commandUpdate, commandRemove, CommandSeparator, commandChangeOrientation)
    import CommandTools._
    tableCommands.toToolBar(toolbar)
    tableView.setContextMenu(tableCommands.toContextMenu())

    tableView.setItems( FXCollections.observableArrayList(
        Person("Jason", "Rocco", 45),
        Person("Leon", "Greyvenstein", 45),
        Person("Michael", "Fowler", 15)
    ))



}


