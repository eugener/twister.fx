package org.twisterfx.demo

import scala.beans.BeanProperty

case class Person(@BeanProperty firstName: String, @BeanProperty lastName: String, @BeanProperty age: Int)
