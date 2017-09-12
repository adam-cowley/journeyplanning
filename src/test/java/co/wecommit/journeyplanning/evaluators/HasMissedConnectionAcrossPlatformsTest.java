package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.Utils;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;

public class HasMissedConnectionAcrossPlatformsTest {

    private static final File DB_PATH = new File("/Users/adam/neo4j/community-3.2.2/data/databases/journeyplanning-copy.db");

    private static final GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);


    @Test
    public void shouldIdentifyMissedConnectionAcrossPlatforms() {
        try (Transaction tx = graph.beginTx()) {
            Result result = graph.execute("MATCH p= (n1)-->(n2)-->(n3)-->(n4) where id(n1) = 134 and id(n2) = 31 and id(n3) = 28 and id(n4) = 127 return p");

            while ( result.hasNext() ) {
                Path path = (Path) result.next().get("p");
                ArrayList<Node> nodes = Utils.nodesToArray(path);
                ArrayList<Relationship> rels = Utils.relsToArray(path);

                assertEquals(true, Evaluators.hasMissedConnectionAcrossPlatforms(nodes, rels, new Long(30)));
            }
        }
    }

    @Test
    public void shouldIgnoreValidConnections() {
        try (Transaction tx = graph.beginTx()) {
            Result result = graph.execute("MATCH p= (n1)-->(n2)-->(n3)-->(n4) where id(n1) = 134 and id(n2) = 31 and id(n3) = 28 and id(n4) = 120 return p");

            while ( result.hasNext() ) {
                Path path = (Path) result.next().get("p");
                ArrayList<Node> nodes = Utils.nodesToArray(path);
                ArrayList<Relationship> rels = Utils.relsToArray(path);

                assertEquals(false, Evaluators.hasMissedConnectionAcrossPlatforms(nodes, rels, new Long(30)));
            }
        }
    }

}
