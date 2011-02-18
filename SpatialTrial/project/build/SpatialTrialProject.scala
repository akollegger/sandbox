import sbt._

class SpatialTrialProject(info: ProjectInfo) extends DefaultProject(info)
{
  //
  // Repositories
  //
  val Neo4jRepo = "neo4j-public" at "http://m2.neo4j.org"
  val GeoTools = "geotools-repo" at "http://download.osgeo.org/webdav/geotools"
  val JavaDevNet = "java-dev-net" at "http://download.java.net/maven/2/" // required for jsr-275

  //
  // Versions
  //
  val NEO4J_VERSION = "1.3.M02"
  val NEO4J_SPATIAL_VERSION = "0.3-SNAPSHOT"
  val GEOTOOLS_VERSION = "2.7-M3"

  //
  // Dependencies
  //
  val scala_test = "org.scalatest" % "scalatest" % "1.2" % "test"
  val neo4j_kernel = "org.neo4j" % "neo4j-kernel" % NEO4J_VERSION
  val neo4j_kernel_test = "org.neo4j" % "neo4j-kernel" % NEO4J_VERSION % "test" classifier "tests"
  val neo4j_spatial = "org.neo4j" % "neo4j-spatial" % NEO4J_SPATIAL_VERSION

  // required to create a SpatialDatabaseService (should be transitive. isn't)
  val jts_model = "com.vividsolutions" % "jts" % "1.11"

  // required to create a Layer (marked as "provided?")
  // Too many required dependencies of neo4j-spatial are marked as provided.
  val geotools = "org.geotools" % "gt-main" % GEOTOOLS_VERSION

}
