package org.twisterfx

import javafx.beans.property._
import javafx.beans.property.adapter._
import javafx.beans.value.WritableObjectValue

import scala.collection.mutable
import scala.language.reflectiveCalls
import scala.reflect.runtime.universe._

object ViewModel {
    type FXProperty[T] = Property[T] with WritableObjectValue[T]
}

import ViewModel._

abstract class ViewModel[SUBJECT]( val subject: SUBJECT ) {

    private val knownProperties = mutable.Set[Binding[_]]()

    // Creates property bound to the source property and registers binding in the view model
    final def bind[T](sourceProperty: FXProperty[T]): FXProperty[T] = bind(FXPropertyBinding[T](sourceProperty))

    // Creates property bound to a bean property  and registers it in view model
    final def bind[T: TypeTag](bean: AnyRef, propertyName: String ): FXProperty[T] = bind(BeanPropertyBinding[T](bean, propertyName))

    private def bind[T]( binding: Binding[T]): FXProperty[T] = {
        knownProperties += binding
        binding.rollback() // initialize model property
        binding.modelProperty
    }

    final def commit(): Unit = knownProperties.foreach(_.commit())
    final def rollback(): Unit = knownProperties.foreach(_.rollback())
    final def isDirty: Boolean = knownProperties.exists(_.isDirty)
    final def isDirty(modelProperty: FXProperty[_] ): Boolean = {
        knownProperties.find(_.modelProperty == modelProperty).exists(_.isDirty)
    }

    final def unbind(): Unit = knownProperties.foreach(_.unbind())

}

trait Binding[T] {

    val subjectProperty: FXProperty[T]
    val modelProperty: FXProperty[T]

    def commit(): Unit = if ( isDirty ) subjectProperty.set(modelProperty.get)
    def rollback(): Unit = modelProperty.set(subjectProperty.get)
    def isDirty: Boolean = modelProperty.get != subjectProperty.get
    def unbind(): Unit = modelProperty.unbind()
}

private case class FXPropertyBinding[T](subjectProperty: FXProperty[T]) extends Binding[T]{
    require( subjectProperty != null )
    override val modelProperty: FXProperty[T] = subjectProperty.getClass.newInstance().asInstanceOf[FXProperty[T]]
}

private class BeanBinding[T]( subjectGetter: => T, subjectSetter: T => Unit ) {
    val subjectProperty: FXProperty[T] = new FXProperty[T] {

    }
}

// Java bean property bindings
private case class BeanPropertyBinding[T: TypeTag](bean: AnyRef, propName: String )
    extends Binding[T]{

    val subjectProperty: FXProperty[T] = typeOf[T] match {
        case t if t =:= typeOf[Boolean] => JavaBeanBooleanPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[String]  => JavaBeanStringPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Int]     => JavaBeanIntegerPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Long]    => JavaBeanLongPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Float]   => JavaBeanFloatPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Double]  => JavaBeanDoublePropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
        case _                          => JavaBeanObjectPropertyBuilder.create().bean(bean).name(propName.toString).build().asInstanceOf[FXProperty[T]]
    }

    val modelProperty: FXProperty[T] = typeOf[T] match {
        case t if t =:= typeOf[Boolean] => new SimpleBooleanProperty().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[String]  =>  new SimpleStringProperty().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Int]     =>  new SimpleIntegerProperty().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Long]    =>  new SimpleLongProperty().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Float]   =>  new SimpleFloatProperty().asInstanceOf[FXProperty[T]]
        case t if t =:= typeOf[Double]  =>  new SimpleDoubleProperty().asInstanceOf[FXProperty[T]]
        case _                          =>  new SimpleObjectProperty[T]().asInstanceOf[FXProperty[T]]
    }

}

