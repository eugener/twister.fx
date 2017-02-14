package org.gitfx

import java.util
import javafx.fxml.FXML
import javafx.scene.control._

import com.gluonhq.ignite.DIContext
import com.gluonhq.ignite.spring.SpringContext
import org.springframework.stereotype.Component
import org.twisterfx.{App, Command, CommandCheck, CommandGroup, CommandRadio, CommandTools, FXMLView}

object TwisterDemo extends App {
    protected def diContext: DIContext = new SpringContext(this, () => util.Arrays.asList("org.twisterfx"))
    override def stop(): Unit = println("stopping")
}

@Component("RootView")
class DemoView extends FXMLView/*("demoview.fxml")*/ {
    title = "TwisterFX Demo Application"
}

class DemoViewController {

    @FXML var tabs: TabPane = _
    @FXML var btAddTab: Button = _

    @FXML var table: TableView[String] = _
    @FXML implicit var toolbar: ToolBar = _
    @FXML var menuBar: MenuBar = _

    lazy val commands = List(
        Command( "command 1" )(_ => println("command 1") ),
        new CommandGroup( "command 2")(
            Command("subcommand 1"){ _ => println("subcommand 1") },
            Command("subcommand 2"){ _ => println("subcommand 2") },
            Command("subcommand 3"){ _ => println("subcommand 3") },
            new CommandCheck("check 1"),
            new CommandRadio("radio 1")("g1"),
            new CommandRadio("radio 2")("g1"),
            new CommandRadio("radio 3")("g1")
        ),
        Command("command 3")(_ => println("command 3") ),
        new CommandRadio("radio 21")("g2"),
        new CommandRadio("radio 22")("g2"),
        new CommandRadio("radio 23")("g2")
    )

    def initialize(): Unit = {
        import CommandTools._
        commands.toToolBar(toolbar)
        commands.toMenu(menuBar)
        table.setContextMenu(commands.toContextMenu())
    }

    def addNewTab(): Unit = tabs.getTabs.add(new Tab( "Tab " + (tabs.getTabs.size() + 1)))

}