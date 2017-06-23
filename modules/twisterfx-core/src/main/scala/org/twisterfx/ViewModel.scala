package org.twisterfx

import javafx.beans.property._
import javafx.beans.property.adapter._
import javafx.beans.value.WritableValue

import scala.collection.mutable
import scala.language.reflectiveCalls
import scala.reflect.runtime.universe._

object ViewModel {
    type FXProperty[T] = Property[T] with WritableValue[T]
}

import org.twisterfx.ViewModel._

abstract class ViewModel[SUBJECT]( val subject: SUBJECT ) {

    private val knownProperties = mutable.Set[Binding[_]]()

    /**
      * Creates property bound to the source property and registers binding in the view model
      * @param sourceProperty source property
      * @tparam T property type
      * @return newly created property bound to the source property
      */
    final def bind[T: TypeTag](sourceProperty: WritableValue[T]): FXProperty[T] = bind(new FXPropertyBinding[T](sourceProperty))

    /**
      * Creates property bound to a bean property and registers it in view model
      * @param bean
      * @param propertyName
      * @tparam T property type
      * @return newly created property bound to the bean property
      */
    final def bind[T: TypeTag](bean: AnyRef, propertyName: String ): FXProperty[T] = bind( new BeanPropertyBinding[T](bean, propertyName))

    /**
      * Creates property bound to provided getter and setter
      * @param getter
      * @param setter
      * @tparam T
      * @return
      */
    final def bind[T: TypeTag]( getter: => T, setter: T => Unit ): FXProperty[T] = bind( new Binding[T](getter, setter))

    private def bind[T]( binding: Binding[T]): FXProperty[T] = {
        knownProperties.add(binding)
        binding.rollback() // initialize model property
        binding.modelProperty
    }

    /**
      * Commits view model properties to the associated model
      */
    final def commit(): Unit = knownProperties.foreach(_.commit())

    /**
      * Rolls back view model properties to the original state using associated model
      */
    final def rollback(): Unit = knownProperties.foreach(_.rollback())

    /**
      * Checks if the view model state is dirty, i.e. there differences between view model properties and source properties
      * @return true if view model is dirty
      */
    final def isDirty: Boolean = knownProperties.exists(_.isDirty)

    /**
      * Checks the dirty state for provided property
      * @param modelProperty
      * @return true if modelProperty is dirty
      */
    final def isDirty(modelProperty: FXProperty[_] ): Boolean = {
        knownProperties.find(_.modelProperty == modelProperty).exists(_.isDirty)
    }

    /**
      * Unbinds all the bindings for the view model
      */
    final def unbind(): Unit = knownProperties.foreach(_.unbind())

}

class Binding[T: TypeTag]( subjectGetter: => T, val subjectSetter: T => Unit ) {

    require( subjectGetter != null )
    require( subjectSetter != null )

    protected def subjectGet: T = subjectGetter
    protected def subjectSet(value: T): Unit = subjectSetter(value)

    val modelProperty: FXProperty[T] = Binding.createProperty[T]

    def commit(): Unit = if ( isDirty ) subjectSet(modelProperty.getValue)
    def rollback(): Unit = modelProperty.setValue(subjectGet)
    def isDirty: Boolean = modelProperty.getValue != subjectGet
    def unbind(): Unit = modelProperty.unbind()
}


private object Binding {

    def asProperty[T: TypeTag](bean: AnyRef, propName: String): FXProperty[T] = {
        ( typeOf[T] match {
            case t if t =:= typeOf[Boolean] => JavaBeanBooleanPropertyBuilder.create().bean(bean).name(propName.toString).build()
            case t if t =:= typeOf[String]  => JavaBeanStringPropertyBuilder.create().bean(bean).name(propName.toString).build()
            case t if t =:= typeOf[Int]     => JavaBeanIntegerPropertyBuilder.create().bean(bean).name(propName.toString).build()
            case t if t =:= typeOf[Long]    => JavaBeanLongPropertyBuilder.create().bean(bean).name(propName.toString).build()
            case t if t =:= typeOf[Float]   => JavaBeanFloatPropertyBuilder.create().bean(bean).name(propName.toString).build()
            case t if t =:= typeOf[Double]  => JavaBeanDoublePropertyBuilder.create().bean(bean).name(propName.toString).build()
            case _                          => JavaBeanObjectPropertyBuilder.create().bean(bean).name(propName.toString).build()
        }).asInstanceOf[FXProperty[T]]
    }

    def createProperty[T: TypeTag]: FXProperty[T] = {
        (typeOf[T] match {
            case t if t =:= typeOf[Boolean] => new SimpleBooleanProperty()
            case t if t =:= typeOf[String] => new SimpleStringProperty()
            case t if t =:= typeOf[Int] => new SimpleIntegerProperty()
            case t if t =:= typeOf[Long] => new SimpleLongProperty()
            case t if t =:= typeOf[Float] => new SimpleFloatProperty()
            case t if t =:= typeOf[Double] => new SimpleDoubleProperty()
            case _ => new SimpleObjectProperty[T]()
        }).asInstanceOf[FXProperty[T]]
    }

}

private class FXPropertyBinding[T: TypeTag](subjectProperty: WritableValue[T]) extends Binding[T](subjectProperty.getValue, subjectProperty.setValue)

private class BeanPropertyBinding[T: TypeTag](bean: AnyRef, propName: String ) extends FXPropertyBinding[T]( Binding.asProperty(bean,propName))


