package implicitly

import sbt._
import Keys._

object LS extends Plugin {

  val ls = config("ls") extend(Runtime)

  val lsVersion = SettingKey[String]("version", "version descriptor file to write to")
  val path = SettingKey[java.io.File]("ls-path", "...")
  val writeVersion = TaskKey[Unit]("write-version", "writes version data to descriptor file")

  def lsSettings: Seq[Setting[_]] = inConfig(ls)(Seq(
    lsVersion <<= (version)(_.replace("-SNAPSHOT","")),
    path <<= (sourceDirectory in Compile) { _ / "ls" },
    // todo, we can't access `projects` here so how do we collect all metadata
    writeVersion <<=
      (path,
       lsVersion,
       name,
       version,
       organization,
       resolvers,
       streams) map { (bd, lv, n, v, o, rsvrs, out) =>
         out.log.info("""would write to %s/%s.json ->
           |{
           | "organization":"%s",
           | "name":"%s",
           | "version":"%s",
           | "description":"",
           | "tags":[],
           | "resolvers": %s
           |}""".format(
           bd.getPath, lv, o, n, v, rsvrs.map(_.toString).mkString("[",",","]")
         ).stripMargin)
      }
  ))
}
