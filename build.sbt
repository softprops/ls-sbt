sbtPlugin := true

organization := "me.lessis"

name := "ls-sbt"

version <<= sbtVersion("0.1.0-%s-SNAPSHOT" format _)
