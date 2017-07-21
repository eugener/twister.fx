package org.twisterfx.demo

import scala.beans.BeanProperty


trait DomainEntity {

    val id: String

    override def hashCode: Int = id.hashCode
    override def equals(obj: scala.Any): Boolean = obj match {
        case e: DomainEntity => e.id == id
        case _ => false
    }
}

case class Person(@BeanProperty firstName: String, @BeanProperty lastName: String, @BeanProperty age: Int)  extends DomainEntity {

    val id: String = s"id$firstName:$lastName:$age"
}
