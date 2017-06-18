package org.gitfx

import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser

import org.gitfx.domain.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.twisterfx.CommandTools._
import org.twisterfx.{Alerts, Command, FXMLView, View}

import scala.util.{Failure, Success}

@Component("root.view")
class MainView extends FXMLView /*("mainview.fxml")*/ {
    title = "GitFX"
}


class MainViewController {

    @FXML var tabs: TabPane = _
    @FXML implicit var toolbar: ToolBar = _
    @FXML var menuBar: MenuBar = _

    @Autowired var context: ApplicationContext = _

    val commands = List(
        Command("Open Repo"){ _ => openRepo()},
        Command("Show Info"){ _ => showInfo()}
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

            case Failure(ex) =>
                val alert = new Alert(AlertType.ERROR)
                alert.initOwner(toolbar.getScene.getWindow)
                alert.setContentText(ex.getLocalizedMessage)
                alert.setHeaderText(null)
                alert.showAndWait()

        }


    }

    def showInfo(): Unit = {
       //new RepoInfo().showDialog(toolbar.getScene.getWindow)
        try {
            1/0
        } catch {
            case ex: Throwable =>  Alerts.exception(ex)
        }
    }


}

class RepoInfo extends View {
    override val root: VBox = new VBox(10)

    title = "Repository Information"

    val txFullName     = new TextField()
    val txEmailAddress = new TextField()

    root.getChildren.addAll(txFullName, txEmailAddress)

}