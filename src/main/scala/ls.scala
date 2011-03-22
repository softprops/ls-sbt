package implicitly

import sbt._

object Ls {
  val LsPath = "ls.json"
}

trait Listing extends Project {
  import Ls._
  import Formatting._
  import sbt.Path._

  implicit def o2t(o: Option[String]) = task { o }

  lazy val writeLs = task { _ match {
    case Array(desc) =>
      val ls = (this.info.projectPath / LsPath).asFile
      log.info("write implicitly listing to %s" format ls)
      FileUtilities.write(ls, Formatting(this,None), log)
      None
    case _ =>
      val ls = (this.info.projectPath / LsPath).asFile
      log.info("write implicitly listing to %s" format ((this.info.projectPath / LsPath).asFile.getClass()))
      FileUtilities.write(ls, Formatting(this,None), log)
      None
  } } describedAs(
    "Writes new implicitly ls metadata"
  )

  lazy val previewLs = task { _ match {
    case Array(desc) =>
      val rep = Formatting(this, Some(desc))
      println(rep)
      None
    case _ =>
      val rep = Formatting(this, None)
      println(rep)
      None
  } } describedAs(
    "Preview implicitly ls metadata"
  )

  lazy val publishLs = task { _ match {
    case Array(desc) =>
      Git.status(log) { lines =>
        println("lines %s" format lines)
      }
      None
    case _ =>
      Git.status(log) { println(_) }
      None
  } } describedAs(
    "Publishes implicitly ls metadata"
  )
}
