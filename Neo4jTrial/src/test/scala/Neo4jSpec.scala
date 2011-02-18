package neo4j.trial.kernel

import scala.collection.JavaConversions._

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.neo4j.graphdb.DynamicRelationshipType

// Neo4j Test-friendly graph database
import org.neo4j.kernel.ImpermanentGraphDatabase

class Neo4jSpec extends WordSpec with MustMatchers {

  "Neo4j" should {
    "have a testing-friendly temporary graph database" in {
      val tempGraphDb = new ImpermanentGraphDatabase()
      tempGraphDb.shutdown
    }

    "save associated key/values in a Node" in {
      val tempGraphDb = new ImpermanentGraphDatabase()

      val tx = tempGraphDb.beginTx
      val node = tempGraphDb.createNode
      tx.success

      tempGraphDb.shutdown()
    }

    "have a facility for finding existing nodes" in {
      val tempGraphDb = new ImpermanentGraphDatabase()

      val tx = tempGraphDb.beginTx
      val createdNode = tempGraphDb.createNode
      tx.success

      val foundNode = tempGraphDb.getAllNodes.head
      // The following assertion fails because of the reference Node.
      // The reference Node is not involved in the creation of an
      // explicit Node, so it is surprising to find it there.
      // After creating one Node, I expect graph.allNodes to be a Set
      // containing just that Node.
      // I like the reference Node, but we offer no encouragement to
      // use it. It's a dangling participle, a vestigial tail.
      foundNode must equal(createdNode)

      tempGraphDb.shutdown()
    }

    "associate Nodes" in {
      val tempGraphDb = new ImpermanentGraphDatabase()

      val tx = tempGraphDb.beginTx
      val node1 = tempGraphDb.createNode
      val node2 = tempGraphDb.createNode
      // Why isn't the simplest thing I could do to create an anonymous relationship?
      //    node1.createRelationshipTo(node2)
      // Also, if we believe G=[V,E] then why can't I create a Relationship without any Nodes?
      // What do we believe defines a graph? Other than Marko's incoherent rambling.
      val node1ToNode2 = node1.createRelationshipTo(node2, DynamicRelationshipType.withName("") )
      tx.success

      tempGraphDb.shutdown
    }

  }
}
