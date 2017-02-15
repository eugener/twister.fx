package org.gitfx.domain

import java.io.File

import org.eclipse.jgit.lib.{Repository => JGitRepo}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

object Repository {

   def open( location: File ): Repository = new Repository(location)

}

case class Repository( location: File ) {

   val name: String = location.getName

   val repository: JGitRepo = {
      new FileRepositoryBuilder()
        .setGitDir(location)
        .readEnvironment // scan environment GIT_* variables
        .findGitDir // scan up the file system tree
        .build
   }

}
