package implicitly

object Git {
  import sbt.Process._
  import sbt.Logger
  import sbt._
  /** render an easy to parse format for files differing from HEAD */
  def status[T](log: Logger)(f: Stream[String] => T) =
    f("git status --porcelain" lines_!(log))
  /** add a path to the index */
  def add(path: String, log: Logger) = "git add %s" format(path) ! log
  /** checkout a path */
  def checkout(path: String, log: Logger) = "git checkout %s" format(path) ! log
  /** reset changes back to HEAD*/
  def resetHardHEAD(log: Logger) = "git reset --hard HEAD" ! log
  /** commit with message m */
  def commit(m: String, log: Logger) = "git commit -m '%s'" format(m) ! log
  /** push to remote branch */
  def push(remote: String, branch: String, log:Logger) = "git push %s %s".format(remote, branch) ! log
}



