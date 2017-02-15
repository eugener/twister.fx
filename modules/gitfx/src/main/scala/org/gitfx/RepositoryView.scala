package org.gitfx

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}

import org.gitfx.domain.Repository
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.twisterfx.FXMLView

@Component
@Scope("prototype")
class RepositoryView extends FXMLView

class RepositoryViewController {

  val repositoryProperty: ObjectProperty[Repository] = new SimpleObjectProperty[Repository]()
  def repository: Repository = repositoryProperty.get
  def repository_=( value: Repository ): Unit = repositoryProperty.set(value)

}
