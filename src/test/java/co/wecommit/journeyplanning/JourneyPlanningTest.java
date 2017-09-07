package co.wecommit.journeyplanning;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.graphdb.Node;

import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;


public class JourneyPlanningTest {

    private static final File DB_PATH = new File("/Users/adam/neo4j/community-3.2.2/data/databases/journeyplanning.db");

    private static final GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
    
    @Test
    public void shouldReturnPath() {
        String start = "BRI";
        String end = "PAD";

        try (Transaction tx = graph.beginTx()) {
            FindServices find = new FindServices(graph);

            ArrayList<FindServices.Journey> journeys = find.between(start, end);

            for (FindServices.Journey journey : journeys) {
                System.out.println(journey.toString());
            }

            tx.success();
        }
    }

}
