package org.twisterfx

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.WritableObjectValue

import scala.collection.mutable
import scala.language.reflectiveCalls

abstract class ViewModel[ENTITY]( val domainEntity: ENTITY ) {

    private val knownProperties = mutable.Set[PropertyBinding[_,_]]()

    // Creates property bound to the source property and registers binding in the view model
    final def bind[PROPERTY <: WritableObjectValue[ENTITY]](sourceProperty: PROPERTY): PROPERTY = {
          val binding = PropertyBinding[PROPERTY,ENTITY](sourceProperty)
          knownProperties += binding
          binding.modelProperty
    }

    final def commit(): Unit = knownProperties.foreach(_.commit())
    final def rollback(): Unit = knownProperties.foreach(_.rollback())
    final def isDirty: Boolean = knownProperties.exists(_.isDirty)


}

private case class PropertyBinding[ PROPERTY <: WritableObjectValue[TYPE], TYPE ](domainProperty: PROPERTY) {

    require( domainProperty != null )

    val modelProperty: PROPERTY = domainProperty.getClass.newInstance()

    def commit(): Unit = domainProperty.set(modelProperty.get)
    def rollback(): Unit = modelProperty.set(domainProperty.get)

    def isDirty: Boolean = modelProperty.get != domainProperty.get

}


