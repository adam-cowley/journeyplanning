package co.wecommit.journeyplanning;

import co.wecommit.journeyplanning.results.Journey;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.graphdb.Node;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;


public class JourneyPlanningTest {

    private static final File DB_PATH = new File("/Users/adam/neo4j/community-3.2.2/data/databases/journeyplanning-copy.db");

    private static final GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    private static final Long threshold = new Integer(30).longValue();

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
    public void shouldFindSwiToDpw() {
        test("SWI", "DPW", new Long(540), new Long(560), threshold);
    }

    @Test
    public void shouldFindSwiToRea() {
        ArrayList<Journey> res = test("SWI", "REA", new Long(540), new Long(570), threshold);

        assertEquals(1, res.size());
    }


    @Test
    public void shouldFindSwiToPad() {
        ArrayList<Journey> res = test("SWI", "PAD", new Long(540), new Long(585), threshold);

        assertEquals(1, res.size());
    }


    @Test
    public void shouldFindChlToSwi() {
        test("CHL", "SWI", new Long(450), new Long(500), threshold);
    }

    @Test
    public void shouldTransferBetweenPlatforms() {







        test("CHL", "PAD", new Long(430), new Long(585), threshold);









    }


    private ArrayList<Journey> test(String start, String end, Long depart_after, Long arrive_before, Long threshold) {
        try (Transaction tx = graph.beginTx()) {
            ArrayList<Journey> journeys = find.between(start, end, depart_after, arrive_before, threshold);

            System.out.println("");
            System.out.println("");
            System.out.println("--");
            System.out.println("Results: "+ journeys.size());

            for (Journey journey : journeys) {
                System.out.println(journey.toString());
            }

            tx.success();

            return journeys;
        }
    }

}
