package org.twisterfx.demo

import java.util

import com.gluonhq.ignite.DIContext
import com.gluonhq.ignite.spring.SpringContext
import org.springframework.stereotype.Component
import org.twisterfx.{App, View}

object TwisterDemo extends App {
    protected def diContext: DIContext = new SpringContext(this, () => util.Arrays.asList("org.twisterfx"))
    override def stop(): Unit = println("stopping")
}

@Component("RootView")
class DemoView extends View("TwisterFX Demo Application", "/demo.fxml")