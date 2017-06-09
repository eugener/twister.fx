package org.twisterfx

import javafx.beans.value.WritableObjectValue

import scala.collection.mutable

abstract class ViewModel[T]( val domainEntity: T ) {

    private val knownProps: mutable.Set[PropBinding[_,_]] = mutable.Set[PropBinding[_,_]]()

    final def bind[P <: WritableObjectValue[T]]( prop: P ): P = {
          val binding = PropBinding( prop )
          knownProps += binding
          binding.modelProp
    }

    final def commit(): Unit = knownProps.foreach(_.commit())
    final def rollback(): Unit = knownProps.foreach(_.rollback())
    final def isDirty: Boolean = knownProps.exists(_.isDirty)

}

private case class PropBinding[P <: WritableObjectValue[T], T](domainProp: P) {

    require( domainProp != null )

    val modelProp: P = domainProp.getClass.newInstance()

    def commit(): Unit = domainProp.set(modelProp.get)
    def rollback(): Unit = modelProp.set(domainProp.get)

    def isDirty: Boolean = modelProp.get != domainProp.get

}