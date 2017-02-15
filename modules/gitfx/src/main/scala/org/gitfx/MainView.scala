package org.gitfx

import java.io.File
import javafx.fxml.FXML
import javafx.scene.control.{MenuBar, Tab, TabPane, ToolBar}

import org.gitfx.domain.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.twisterfx.FXMLView

@Component("RootView")
class MainView extends FXMLView/*("mainview.fxml")*/ {
  title = "GitFX"
}


class MainViewController {

  @FXML var tabs: TabPane = _
  @FXML implicit var toolbar: ToolBar = _
  @FXML var menuBar: MenuBar = _

  @Autowired var context: ApplicationContext = _

  def initialize(): Unit = {

    val repo = Repository.open( new File("/Users/exr0bs5/Projects/highbrow"))

    val repoView = context.getBean(classOf[RepositoryView])
    tabs.getTabs.add( new Tab( repo.name, repoView.root))
    val controller = repoView.getController[RepositoryViewController]
    controller.repository = repo

  }

}