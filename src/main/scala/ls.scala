package implicitly

import sbt._
import Keys._

object LS extends Plugin {

  val ls = config("ls") extend(Runtime)

  // ls descriptor file
  val lsVersion = SettingKey[String]("version", "Version name used in ls descriptor file")
  val dir = SettingKey[java.io.File]("dir", "Directory where ls descriptor file will be written")
  val file = SettingKey[String]("file", "File path of ls descriptor file")

  // tasks
  val writeVersion = TaskKey[Unit]("write-version", "writes version data to descriptor file")
  val dependencyFilter = SettingKey[ModuleID => Boolean]("dependency-filter", "Filters dependencies included in write-version")

  // option library info
  val description = SettingKey[Option[String]]("description", "Library description")
  val tags = SettingKey[Option[Seq[String]]]("tags", "List of taxonomy tags for the library")
  val docs = SettingKey[Option[String]]("docs", "Url for library documentation")


  // private setting!
  val optionals = SettingKey[(Option[String], Option[Seq[String]], Option[String])]("optionals", "...")

  private def mjson(m: ModuleID) =
    """{
    |     "organization":"%s",
    |     "name": "%s",
    |     "version": "%s"
    }""".stripMargin.format(m.organization, m.name, m.revision)

  def lsSettings: Seq[Setting[_]] = inConfig(ls)(Seq(
    lsVersion <<= (version)(_.replace("-SNAPSHOT","")),
    dir <<= (sourceDirectory in Compile) { _ / "ls" },
    file <<= (dir, lsVersion)((d, lv) => "%s/%s.json" format (d.getPath, lv)),
    dependencyFilter := { m => m.organization != "org.scala-lang" },
    docs := None,
    tags := None,
    description := None,
    optionals <<= (description, tags, docs)((desc, tags, docs) =>
       (desc, tags, docs)
    ),
    writeVersion <<=
      (file,
       name,
       version,
       organization,
       optionals,
       resolvers,
       projectDependencies,
       libraryDependencies,
       dependencyFilter) map { (file, n, v, o, optionals, rsvrs, pdeps, ldeps, dfilter) =>
         val defn = """
           |{
           | "organization":"%s",
           | "name":"%s",
           | "version":"%s",
           | "description":"%s",
           | "tags":%s,
           | "docs":"%s",
           | "resolvers": %s,
           | "dependencies":{
           |   "projects": %s,
           |   "libraries": %s
           | }
           |}""".format(
             o,
             n,
             v,
             optionals._1.getOrElse(""),
             optionals._2.getOrElse(Nil) match {
               case Nil => "[]"
               case xs => xs.mkString("[\"","\",\"","\"]")
             },
             optionals._3.getOrElse(""),
             rsvrs.map(_.toString) match {
               case Nil => "[]"
               case xs => xs.mkString("['","','","']")
             },
             pdeps.filter(dfilter).map(mjson).mkString("[",",","]"),
             ldeps.filter(dfilter).map(mjson).mkString("[",",","]")
         ).stripMargin
         println("will write to %s %s" format(file, defn))
         val f = new java.io.File(file)
         if(!f.exists) {
           println("creating dirs for %s" format f)
           f.getParentFile().mkdirs()
           IO.write(f, defn)
         } else {
           println("file already written...")
         }
      }
  ))
}
