package org.gitfx.domain

import java.io.File
import java.nio.file.{Files, Path, Paths}

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Repository => JGitRepo}
import org.eclipse.jgit.revwalk.{RevCommit => JGitCommit}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.util.Try
import scala.collection.JavaConverters._

object Repository {

   private[domain] val GIT_DIR = ".git"
   def open( location: Path ): Try[Repository] = Try(new Repository(location))

}

case class Repository( location: Path ) {

   import Repository._

   private val gitDir = if ( location.endsWith(GIT_DIR) ) location else Paths.get(location.toString).resolve(GIT_DIR)
   require( Files.exists(gitDir), "Git repository is not found")

   private val repository: JGitRepo = {
      new FileRepositoryBuilder()
        .setGitDir(gitDir.toFile)
        .readEnvironment // scan environment GIT_* variables
        .build
   }

   private lazy val git = new Git(repository)

   lazy val name: String =  {
      val nameCount = location.getNameCount
      if ( nameCount> 0 ) location.getName(nameCount-1).toString else "???"
   }

   def getCommits: List[JGitCommit] = git.log.all.call.asScala.toList



}
