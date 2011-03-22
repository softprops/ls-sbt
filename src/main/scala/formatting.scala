package implicitly

import sbt._

object Formatting {
  trait ProjectFormat extends ((Project, Option[String]) => String)

  implicit val json: ProjectFormat = new ProjectFormat {
    def apply(p: Project, description: Option[String]) =
      """|{
      |  "id": ["%s","%s","%s"],
      |  "description":"%s",
      |  "resolvers":[%s],
      |  "scala-versions":[%s]
      |  "parent":%s
      |}""".stripMargin.format(
        p.organization,
        p.name,
        p.version.toString,
        description.getOrElse("<fill>"),
        resolvers(p).mkString("\"", "\",\"", "\""),
        p.crossScalaVersions.mkString("\"","\",\"","\""),
        p.info.parent match {
          case Some(parent) =>
            """["%s","%s","%s"]""" format(parent.organization, parent.name, parent.version.toString)
          case _ => "\"self\""
        }
      )

    def dependencies(p: Project) =
      p match {
        case rmp: ReflectiveManagedProject =>
          rmp.libraryDependencies /* (organization, name, revision) */
        case _ => Nil
      }

    def resolvers(p: Project): Iterable[String] = p match {
      case rmp: ReflectiveManagedProject =>
        rmp.info.parent match {
          case Some(par) =>
            rmp.reflectiveRepositories.map(resolver) ++ resolvers(par)
          case _ => rmp.reflectiveRepositories.map(resolver)
        }
      case _ => Nil
    }

    /** TODO what is the equiv of `root` for other resolver types */
    def resolver(res: (String, Resolver)) =
      res  match {
        case (_, mvn: MavenRepository) => mvn.root
        case (_, f: FileRepository) => f.toString
        case (_, sftp: SftpRepository) => sftp.toString
        case (_, ssh: SshRepository) => ssh.toString
        case (_, url: URLRepository) => url.toString
        case (n, r) => error("unknown resolver type %s" format r)
      }
  }

  def apply(proj: Project, desc: Option[String])(implicit fmt: ProjectFormat): String =
    fmt(proj, desc)
}
