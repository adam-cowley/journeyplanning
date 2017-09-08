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

    private static final Long threshold = new Integer(60).longValue();

    private static final FindServices find = new FindServices(graph);

    @Test
    public void shouldFindBriToPad() {
        String start = "BRI";
        String end = "PAD";

        Long depart_after = new Double(6.5*60).longValue();
        Long arrive_before =  new Double(9*60).longValue();

        test(start, end, depart_after, arrive_before, threshold);
    }

    @Test
    public void shouldFindSwiToPad() {
        test("SWI", "PAD", new Long(540), new Long(585), threshold);
    }

    @Test
    public void shouldFindChlToSwi() {
        test("CHL", "SWI", new Long(450), new Long(500), threshold);
    }

    @Test
    public void shouldTransferBetweenPlatforms() {
        test("CHL", "PAD", new Long(450), new Long(585), threshold);
    }


    private void test(String start, String end, Long depart_after, Long arrive_before, Long threshold) {
        try (Transaction tx = graph.beginTx()) {
            ArrayList<FindServices.Journey> journeys = find.between(start, end, depart_after, arrive_before, threshold);

            System.out.println("Results: "+ journeys.size());

            for (FindServices.Journey journey : journeys) {
                System.out.println(journey.toString());
            }

            tx.success();
        }
    }

}
