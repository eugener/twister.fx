package org.gitfx

import javafx.fxml.FXML
import javafx.scene.control.{MenuBar, Tab, TabPane, ToolBar}
import javafx.stage.{DirectoryChooser, FileChooser}

import org.gitfx.domain.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.twisterfx.{Command, FXMLView}
import org.twisterfx.CommandTools._

import scala.util.{Failure, Success}

@Component("RootView")
class MainView extends FXMLView /*("mainview.fxml")*/ {
    title = "GitFX"
}


class MainViewController {

    @FXML var tabs: TabPane = _
    @FXML implicit var toolbar: ToolBar = _
    @FXML var menuBar: MenuBar = _

    @Autowired var context: ApplicationContext = _

    val commands = List(
        Command("Open Repo"){ _ => openRepo() }
    )


    def initialize(): Unit = {
        commands.toToolBar(toolbar)
    }

    def openRepo(): Unit = {

        val dirChooser = new DirectoryChooser
        dirChooser.setTitle("Select repository")
        val stage = toolbar.getScene.getWindow
        val file = dirChooser.showDialog(stage)

        Option(file).map( f => Repository.open(f.toPath)).foreach {

            case Success(repo) =>

                val repoView = context.getBean(classOf[RepositoryView])
                tabs.getTabs.add(new Tab(repo.name, repoView.root))
                val controller = repoView.getController[RepositoryViewController]
                controller.repository = repo

            case Failure(ex) => ex.printStackTrace()

        }


    }


}