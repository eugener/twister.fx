package org.gitfx

import java.util

import com.gluonhq.ignite.DIContext
import com.gluonhq.ignite.spring.SpringContext
import org.twisterfx.App

object GitFXApp extends App {
    protected def diContext: DIContext = new SpringContext(this, () => util.Arrays.asList("org.gitfx"))
    override def stop(): Unit = println("stopping")
}


