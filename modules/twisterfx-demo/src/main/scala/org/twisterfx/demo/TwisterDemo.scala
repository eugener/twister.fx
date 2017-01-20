package org.twisterfx.demo

import java.util
import javafx.fxml.FXML
import javafx.scene.control.{Button, Tab, TabPane}

import com.gluonhq.ignite.DIContext
import com.gluonhq.ignite.spring.SpringContext
import org.springframework.stereotype.Component
import org.twisterfx.{App, FXMLView}

object TwisterDemo extends App {
    protected def diContext: DIContext = new SpringContext(this, () => util.Arrays.asList("org.twisterfx"))
    override def stop(): Unit = println("stopping")
}

@Component("RootView")
class DemoView extends FXMLView("/demo.fxml") {

    title = "TwisterFX Demo Application"

}

class DemoViewController {

    @FXML var tabs: TabPane = _
    @FXML var btAddTab: Button = _

    def addNewTab: Unit = tabs.getTabs.add(new Tab( "Tab " + (tabs.getTabs.size() + 1)))

}