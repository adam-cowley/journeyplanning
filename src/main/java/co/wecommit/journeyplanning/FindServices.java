package co.wecommit.journeyplanning;

import co.wecommit.journeyplanning.evaluators.CanTransferBetweenPlatformsEvaluator;
import co.wecommit.journeyplanning.evaluators.IsValidLegEvaluator;
import co.wecommit.journeyplanning.evaluators.ValidJourneyEvaluator;
import co.wecommit.journeyplanning.expanders.JourneyExpander;
import co.wecommit.journeyplanning.results.Journey;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


import static co.wecommit.journeyplanning.Utils.*;

/**
 * Created by adam on 07/09/2017.
 */
public class FindServices {

    private GraphDatabaseService graph;

    private final int max_waiting_time = 30;

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

        // Get all valid start legs
        ArrayList<Node> starting = getStartLegs(origin, "departs_at", depart_after, depart_after + threshold);
        ArrayList<Node> ending = getEndLegs(destination, "arrives_at", arrive_before - threshold, arrive_before);

        for ( Node start_leg : starting ) {
            for ( Node end_leg : ending ) {
                // Single leg journey
                if (starting.equals(ending)) {
                    ArrayList<HashMap<String, Object>> legs = new ArrayList<>();
                }

                Traverser paths = getPathsBetween(start_leg, end_leg);

                System.out.println("START: "+ start_leg.getId());
                System.out.println("END:   "+ end_leg.getId());

                for ( Path route : getPathsBetween(start_leg, end_leg) ) {
                    ArrayList<HashMap<String, Object>> legs = new ArrayList<>();

                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                    System.out.println("Res");
                    System.out.println("");

                    print(route);
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");

                    for ( Node leg : route.nodes() ) {
                        HashMap<String, Object> details = getLegDetails(leg);

                        if ( details != null ) {
                            legs.add(details);
                        }
                    }

                    if ( legs.size() > 0 ) {
                        Journey journey = new Journey(origin, destination, legs);
                        output.add(journey);
                    }

                }
            }

        }

        return output;
    }


    /**
     * Get all valid paths between two legs
     *
     * @param start_leg
     * @param end_leg
     * @return
     */
    private Traverser getPathsBetween(Node start_leg, Node end_leg) {
        InitialBranchState.State<Long> state = new InitialBranchState.State<>(
                (Long) getLong(start_leg, "departs_at"),
                (Long) getLong(end_leg, "arrives_at")
        );

        JourneyExpander biExpander = new JourneyExpander();

        TraversalDescription traversal = graph.traversalDescription()
                .depthFirst()
                .expand(biExpander, state)
                .uniqueness(Uniqueness.NODE_PATH)
                .evaluator( new ValidJourneyEvaluator(start_leg, end_leg) );

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
     * @param node
     * @return
     */
    private HashMap<String, Object> getLegDetails(Node node) {
        HashMap<String, Object> output = new HashMap<>();


        if ( node.hasLabel(Labels.Leg) ) {
            output.put("type", "leg");

            output.put("leg", node);

            // Get Service
            Node service = node.getSingleRelationship(Relationships.HAS_LEG, Direction.INCOMING).getStartNode();
            output.put("service", service);

            // Get Platform
            Node platform = node.getSingleRelationship(Relationships.CAN_BOARD, Direction.INCOMING).getStartNode();
            output.put("platform", platform);

            // Get Station
            Node station = platform.getSingleRelationship(Relationships.HAS_PLATFORM, Direction.INCOMING).getStartNode();
            output.put("station", station);
        }
        else if ( node.hasLabel(Labels.Platform) ) {
            output.put("type", "platform_transfer");

            output.put("platform", node);
        }

        return output;
    }
}
