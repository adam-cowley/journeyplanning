package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.io.File;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.neo4j.graphdb.traversal.Evaluators.atDepth;

public class IsMultiPlatformTraversalTest {

    private static final File DB_PATH = new File("/Users/adam/neo4j/community-3.2.2/data/databases/journeyplanning-copy.db");

    private static final GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    @Test
    public void shouldIdentifyMultiPlatformTraversal() {
        try (Transaction tx = graph.beginTx()) {
            Node swi1 = graph.findNode(Labels.Platform, "reference", "SWI-1");

            TraversalDescription traversal = graph.traversalDescription()
                    .depthFirst()
                    .relationships(Relationships.CAN_TRANSFER_TO, Direction.OUTGOING)
                    .evaluator( atDepth(2) );

            for ( Path path: traversal.traverse(swi1) ) {
                assertEquals(true, Evaluators.isMultiPlatformTraversal(path));

                ArrayList<Node> reverse = Utils.nodesToArray( path.reverseNodes() );
                assertEquals(true, Evaluators.isMultiPlatformTraversal(reverse));
            }
        }
    }


}
