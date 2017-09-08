package co.wecommit.journeyplanning;

import co.wecommit.journeyplanning.evaluators.IsValidLegEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by adam on 07/09/2017.
 */
public class FindServices {

    private GraphDatabaseService graph;

    private static final Label Station = Labels.Station;

    public FindServices(GraphDatabaseService db) {
        graph = db;
    }

    public ArrayList<Journey> between(String start_reference, String end_reference, Long depart_after, Long arrive_before, Long threshold) {
        Node origin = graph.findNode(Station, "reference", start_reference);
        Node destination = graph.findNode(Station, "reference", end_reference);

        return between(origin, destination, depart_after, arrive_before, threshold);
    }

    /**
     *
     *
     *
     // Get Results
     with 6.5*60 as depart_after, 9*60 as arrive_before, 60 as threshold

     match (start:Station {reference: "BRI"})
     match (end:Station {reference: "PAD"})

     match (start)-[:HAS_PLATFORM]->(start_platform:Platform)-[:CAN_BOARD]->(start_leg:Leg)
     where  depart_after <= start_leg.departs_at <= depart_after + threshold

     match (end)-[:HAS_PLATFORM]->(end_platform:Platform)<-[:CAN_ALIGHT]-(last_leg:Leg)
     where arrive_before - threshold <= last_leg.arrives_at <= arrive_before

     match path = (start_leg)-[:NEXT_LEG|CAN_ALIGHT*1..100]->(last_leg:Leg)

     unwind nodes(path) as leg

     match (leg)<-[:HAS_LEG]-(service:Service)
     match (leg)<-[:CAN_BOARD]-(platform_from:Platform)<-[:HAS_PLATFORM]-(station_from:Station)
     optional match (leg)-[:CAN_ALIGHT]->(platform_to:Platform)<-[:HAS_PLATFORM]-(station_to:Station)

     with start, end, start_platform, end_platform, path, collect(distinct service) as services, collect({
         leg:leg,
         service: service,
         from: {
             station: station_from,
             platform: platform_from
         },
         to: {
             station: station_to,
             platform: platform_to
     }
     }) as legs

     return *


     * @param origin
     * @param destination
     */
    public ArrayList<Journey> between(Node origin, Node destination, Long depart_after, Long arrive_before, Long threshold) {
        ArrayList<Journey> output = new ArrayList<Journey>();


        ArrayList<Node> starting = getStartLegs(origin, "departs_at", depart_after, depart_after + threshold);

        for ( Node start_leg : starting ) {
            // Get all endings that depart after this leg this but before the arrival time
            Object departs_at = start_leg.getProperty("departs_at");

            ArrayList<Node> ending = getEndLegs(destination, "arrives_at", arrive_before - threshold, arrive_before);

            System.out.println(start_leg.getId() + " - "+ ending.size());

            for ( Node end_leg : ending ) {
                System.out.println("to: "+ end_leg.getId());

                ArrayList<HashMap<String, Node>> legs = new ArrayList<>();

                for ( Path route : getPathsBetween(start_leg, end_leg) ) {
                    for ( Node leg : route.nodes() ) {
                        HashMap<String, Node> details = getLegDetails(leg);

                        legs.add(details);
                    }
                }

                if ( legs.size() > 0 ) {
                    Journey journey = new Journey(origin, destination, legs);
                    output.add(journey);
                }
            }
        }

        return output;
    }

    public static class Journey {

        private final Node origin;

        private final Node destination;

        private final ArrayList<HashMap<String, Node>> legs;

        public Journey(Node origin, Node destination, ArrayList<HashMap<String, Node>> legs) {
            this.origin = origin;
            this.destination = destination;
            this.legs = legs;
        }

        private Date getDate(Node node, String property) {
            Long value = ((Number) node.getProperty(property,0)).longValue();

            return new Date( value * 60 * 1000 );
        }

        public String toString() {
            String output = "\n\nJourney: "+ origin.getProperty("name") + " to "+ destination.getProperty("name") + "\n--\n";

            SimpleDateFormat time = new SimpleDateFormat("HH:mm");

            for (HashMap<String, Node> leg : legs) {
                Node leg_node = leg.get("leg");
                Node station = leg.get("station");
                Node platform = leg.get("platform");

                Date departs_at = getDate(leg_node, "departs_at");

                output += "-- "+ time.format(departs_at);
                output += " " + station.getProperty("name") + " P. "+ platform.getProperty("number");


                output += "\n";
            }

            output += "\n";
            output += "\n";

            HashMap<String, Node> last_leg = legs.get(legs.size() - 1);

            Date arrives_at = getDate(last_leg.get("leg"), "arrives_at");

            output += "Arrives at "+ destination.getProperty("name") + " at "+ time.format(arrives_at);

            output += "\n--\n\n";

            return output;

        }
    }

    /**
     * Get all valid paths between two legs
     *
     * @param start_leg
     * @param end_leg
     * @return
     */
    private Traverser getPathsBetween(Node start_leg, Node end_leg) {
        BidirectionalJourneyExpander biExpander = new BidirectionalJourneyExpander();

        TraversalDescription traversal = graph.traversalDescription()
                .depthFirst()
                .expand(biExpander)
                .uniqueness(Uniqueness.NODE_PATH);

        BidirectionalTraversalDescription bidirerectional = graph.bidirectionalTraversalDescription()
                .mirroredSides(traversal);

        return bidirerectional.traverse(start_leg, end_leg);
    }

    /**
     * Get all valid start legs between two timestamps
     *
     * @param station
     * @param floor
     * @param ceiling
     * @return
     */
    private ArrayList<Node> getStartLegs(Node station, String property, Long floor, Long ceiling) {
        ArrayList<Node> output = new ArrayList<>();

        TraversalDescription traversal = graph.traversalDescription()
                .depthFirst()
                .relationships(Relationships.HAS_PLATFORM, Direction.OUTGOING)
                .relationships(Relationships.CAN_BOARD, Direction.OUTGOING)
                .evaluator( new IsValidLegEvaluator(2, property, floor, ceiling));

        for ( Path path : traversal.traverse(station) ) {
            Node leg = path.endNode();

            output.add(leg);
        }

        return output;
    }

    /**
     * Get all valid legs between two timestamps
     *
     * @param station
     * @param floor
     * @param ceiling
     * @return
     */
    private ArrayList<Node> getEndLegs(Node station, String property, Long floor, Long ceiling) {
        ArrayList<Node> output = new ArrayList<>();

        TraversalDescription traversal = graph.traversalDescription()
                .depthFirst()
                .relationships(Relationships.HAS_PLATFORM, Direction.OUTGOING)
                .relationships(Relationships.CAN_ALIGHT, Direction.INCOMING)
                .evaluator( new IsValidLegEvaluator(2, property, floor, ceiling));

        for ( Path path : traversal.traverse(station) ) {
            Node leg = path.endNode();
            output.add(leg);
        }

        return output;
    }

    /**
     * Get the station, leg, service and operator for a leg
     *
     * @param leg
     * @return
     */
    private HashMap<String, Node> getLegDetails(Node leg) {
        HashMap<String, Node> output = new HashMap<String, Node>();

        output.put("leg", leg);

        // Get Service
        Node service = leg.getSingleRelationship(Relationships.HAS_LEG, Direction.INCOMING).getStartNode();
        output.put("service", service);

        // Get Platform
        Node platform = leg.getSingleRelationship(Relationships.CAN_BOARD, Direction.INCOMING).getStartNode();
        output.put("platform", platform);

        // Get Station
        Node station = platform.getSingleRelationship(Relationships.HAS_PLATFORM, Direction.INCOMING).getStartNode();
        output.put("station", station);

        return output;
    }
}
