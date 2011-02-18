import sbt._

class Neo4jTrialProject(info: ProjectInfo) extends DefaultProject(info)
{
  //
  // Repositories
  //

  //
  // Versions
  //
  val NEO4J_VERSION = "1.3.M02"

  //
  // Dependencies
  //
  val scala_test = "org.scalatest" % "scalatest" % "1.2" % "test"
  val neo4j_kernel = "org.neo4j" % "neo4j-kernel" % NEO4J_VERSION
  val neo4j_kernel_tests = "org.neo4j" % "neo4j-kernel" % NEO4J_VERSION % "test" classifier "tests"

}
