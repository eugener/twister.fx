package org.gitfx.domain

import java.io.File
import java.nio.file.Path

import org.eclipse.jgit.lib.{Repository => JGitRepo}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.util.Try

object Repository {

   def open( location: Path ): Try[Repository] = Try(new Repository(location))

}

case class Repository( location: Path ) {

   lazy val name: String =  {
      val nameCount = location.getNameCount
      if ( nameCount> 0 ) location.getName(nameCount-1).toString else "???"
   }

   val repository: JGitRepo = {
      new FileRepositoryBuilder()
        .setGitDir(location.toFile)
        .readEnvironment // scan environment GIT_* variables
        .findGitDir // scan up the file system tree
        .build
   }

}
