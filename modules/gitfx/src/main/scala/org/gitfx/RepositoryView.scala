package org.gitfx

import java.time.ZoneId
import java.time.format.{DateTimeFormatter, FormatStyle}
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.fxml.FXML
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Label, ListCell, ListView, OverrunStyle}
import javafx.scene.layout._

import org.eclipse.jgit.revwalk.RevCommit
import org.gitfx.domain.Repository
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.twisterfx.FXMLView
import org.eclipse.jgit.revwalk.{RevCommit => JGitCommit}

import scala.collection.JavaConverters._

@Component
@Scope("prototype")
class RepositoryView extends FXMLView

class RepositoryViewController {

    val repositoryProperty: ObjectProperty[Repository] = new SimpleObjectProperty[Repository]()
    def repository: Repository = repositoryProperty.get
    def repository_=(value: Repository): Unit = repositoryProperty.set(value)

    @FXML
    var commitList: ListView[RevCommit] = _

    def initialize(): Unit = {

        commitList.setMinWidth(250)
        commitList.setCellFactory(_ => new CommitCell())

        repositoryProperty.addListener((_, _, repo) => refreshRepo(repo))
    }

    def refreshRepo( repo: Repository ): Unit = {
        commitList.getItems.setAll(repo.getCommits.asJavaCollection)
    }

}

private class CommitCell extends ListCell[JGitCommit] {

    private val pane = new CommitPanel
    setGraphic(pane)
    setText(null)

    override def updateItem(item: JGitCommit, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        pane.commit = if (empty) null else item
    }
}

private class CommitPanel extends VBox {

    val committerName = new Label()
    val date = new Label()
    val id = new Label()
    val comment = new Label()

    initUI()

    private var commitOption: Option[JGitCommit] = None

    def commit: JGitCommit = commitOption.get

    def commit_=(value: JGitCommit): Unit = {

        commitOption = Option(value)

        updateLabel(committerName)(_.getAuthorIdent.getName)
        updateLabel(id)(_.getName.take(8))
        updateLabel(comment)(_.getShortMessage)
        updateLabel(date){ c =>
            val date = c.getAuthorIdent.getWhen.toInstant.atZone(ZoneId.systemDefault).toLocalDate
            date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }

    }

    def updateLabel(lb: Label)(text: JGitCommit => String ): Unit = lb.setText(commitOption.map(text).orNull)

    def initUI(): Unit = {

        setMinWidth(250)

        committerName.setTextOverrun(OverrunStyle.ELLIPSIS)
        committerName.setAlignment(Pos.CENTER_LEFT)

        comment.setTextOverrun(OverrunStyle.ELLIPSIS)

        val firstRow = new BorderPane(committerName)
        firstRow.setRight(date)
        firstRow.setPadding(new Insets(5))
        BorderPane.setAlignment(committerName, Pos.CENTER_LEFT)

        val secondRow = new BorderPane(comment)
        secondRow.setLeft(id)
        secondRow.setPadding(new Insets(5))
        BorderPane.setAlignment(comment, Pos.CENTER_LEFT)

        getChildren.addAll(firstRow, secondRow)

    }

}
