package neo4j.trial.spatial

import scala.collection.JavaConversions._

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.neo4j.gis.spatial.encoders.{SimplePointEncoder, SimplePropertyEncoder}
import org.neo4j.gis.spatial.{EditableLayerImpl, SpatialDatabaseService}

// Neo4j Test-friendly graph database
import org.neo4j.kernel.ImpermanentGraphDatabase

// Neo4j Spatial
import org.neo4j.gis.spatial.query.SearchWithinDistance
// to set the bounding box property "bbox"

// 3rd-party geospatial
import com.vividsolutions.jts.geom.Point // a simple geometry for a location
import com.vividsolutions.jts.geom.GeometryFactory // needed to easily create a Point
import com.vividsolutions.jts.geom.Coordinate // to specify the location of the Point (for the GeometryFactory)
import com.vividsolutions.jts.geom.Envelope  // a bounding-box geometry
import com.vividsolutions.jts.io.WKBWriter // needed to encode the point into a Node

class Neo4jSpatialSpec extends WordSpec with MustMatchers {

  "Neo4jSpatial" should {
    "wrap a GraphDatabaseService" in {
      val tempGraphDb = new ImpermanentGraphDatabase()

      val spatial = new SpatialDatabaseService(tempGraphDb)

      tempGraphDb.shutdown
    }

    "establish a context for spatial operations" in {
      val tempGraphDb = new ImpermanentGraphDatabase()
      val spatial = new SpatialDatabaseService(tempGraphDb)
      val layer = spatial.createLayer("points of interest")

      layer must not be null

      tempGraphDb.shutdown
    }

    "complain when adding a node without location information" in {
      val tempGraphDb = new ImpermanentGraphDatabase()
      val spatial = new SpatialDatabaseService(tempGraphDb)
      val layer = spatial.createLayer("points of interest")

      val tx = tempGraphDb.beginTx
      val locatedNode = tempGraphDb.createNode
      // Should throw a more meaningful exception than NotFoundException.
      // Full exception is: org.neo4j.graphdb.NotFoundException: wkb property not found for NodeImpl#5.
      // What is the default "wkb" property that is expected? How can I create one?
      // Also, "bbox" is apparently expected by default
      evaluating { layer.add(locatedNode) } must produce [org.neo4j.graphdb.NotFoundException]
      tx.success

      tempGraphDb.shutdown
    }

    "find a Node located within a distance from a starting coordinate" in {
      val tempGraphDb = new ImpermanentGraphDatabase()
      val spatial = new SpatialDatabaseService(tempGraphDb) // augments graph with spatial ability
      val layer = spatial.createLayer("points of interest") // a context for spatial operations
      val spatialIndex = layer.getIndex                     // performs spatial operations
      val geometryFactory = new GeometryFactory             // for creating points from coordinates
      val wkbWriter = new WKBWriter()                       // to write the Point to the Node's "wkb" property

      val poiCoordinate = new Coordinate(42.0, 3.195)                     // coordinate for point of interest
      val pointOfInterest = geometryFactory.createPoint(poiCoordinate)    // coordinate as a simple geometry, a Point

      // add a location annotated node
      val tx = tempGraphDb.beginTx
      val poiNode = tempGraphDb.createNode
      val wkbProperty = wkbWriter.write(pointOfInterest)
      val bboxProperty = "bounded by me"
      poiNode.setProperty("wkb", wkbProperty)   // by default, "wkb" property expected
      // also, the bounding box property, named "bbox" (presumably an encoded envelope?)
      val bboxEncoder = new SimplePropertyEncoder()
      bboxEncoder.encodeEnvelope(new Envelope(poiCoordinate), poiNode)
      val nameOfPoi = "really interesting point"
      poiNode.setProperty("name", nameOfPoi) // a non-spatial property
      layer.add(poiNode)
      tx.success

      val searchFromCoordinate = new Coordinate(0.0, 0.0)
      val searchFromPoint = geometryFactory.createPoint(searchFromCoordinate)
      val withinDistance: Double = 100.0
      val searchQuery = new SearchWithinDistance(searchFromPoint, withinDistance)
      spatialIndex.executeSearch(searchQuery);

      // where are my results? back in the query
      val searchResults = searchQuery.getResults

      searchResults must not be ('empty)
      searchResults must have length(1)
      val foundSpatialNode = searchResults.head
      foundSpatialNode.getProperty("name") must equal(nameOfPoi)

      tempGraphDb.shutdown
    }

    "find a Node located within a distance from a starting coordinate using a specific geometry encoder" in {
      val tempGraphDb = new ImpermanentGraphDatabase()
      val spatial = new SpatialDatabaseService(tempGraphDb) // augments graph with spatial ability
      val layer = spatial.createLayer("points of interest", classOf[SimplePointEncoder], classOf[EditableLayerImpl], "lon:lat") // a context for spatial operations
      val geometryFactory = new GeometryFactory             // for creating points from coordinates

      // add a location annotated node
      val tx = tempGraphDb.beginTx
      val poiNode = tempGraphDb.createNode
      val latitude = 42.01
      poiNode.setProperty("lat", latitude)
      val longitude = 1.61803
      poiNode.setProperty("lon", longitude)
      // also, the bounding box property, named "bbox" (still needed?)
      val bboxEncoder = new SimplePropertyEncoder()
      bboxEncoder.encodeEnvelope(new Envelope(latitude, latitude, longitude, longitude), poiNode)
      val nameOfPoi = "really interesting point"
      poiNode.setProperty("name", nameOfPoi) // a non-spatial property
      layer.add(poiNode)
      tx.success

      val searchFromCoordinate = new Coordinate(0.0, 0.0)
      val searchFromPoint = geometryFactory.createPoint(searchFromCoordinate)
      val withinDistance = 100.0
      val searchQuery = new SearchWithinDistance(searchFromPoint, withinDistance)
      layer.getIndex.executeSearch(searchQuery);

      // where are my results? back in the query
      val searchResults = searchQuery.getResults

      searchResults must not be ('empty)
      searchResults must have length(1)
      val foundSpatialNode = searchResults.head
      foundSpatialNode.getProperty("name") must equal(nameOfPoi)

      tempGraphDb.shutdown
    }
  }

}
