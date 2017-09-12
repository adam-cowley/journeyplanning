package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

import java.util.ArrayList;

import static co.wecommit.journeyplanning.Utils.print;

public class ValidJourneyEvaluator implements Evaluator {

    private final Node start;
    private final Node end;

    private final Node end_platform;

    // TODO: Make dynamic
    private final Long max_waiting = new Long(30);

    public ValidJourneyEvaluator(Node start, Node end) {
        this.start = start;
        this.end = end;

        this.end_platform = end.getSingleRelationship(Relationships.CAN_ALIGHT, Direction.OUTGOING).getEndNode();
    }

    @Override
    public Evaluation evaluate(Path path) {
        print(path);
        if ( path.length() > 6) {
            System.out.println("--= TOO LONG (TEST)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // If start & end match, we're good...
        /*
        if ( path.startNode().equals(start) && path.endNode().equals(end) ) {
            System.out.println("--= INCLUDE");
            return Evaluation.INCLUDE_AND_PRUNE;
        }

        else if ( path.startNode().equals(end) && path.endNode().equals(start) ) {
            System.out.println("--= INCLUDE REVERSE");
            return Evaluation.INCLUDE_AND_PRUNE;
        }
        */

        // We all need to start somewhere...
        else if ( path.length() == 0 ) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }

        // Work out which way to
        Node first_node = path.startNode();

        if ( first_node.equals(end) ) {
            return evaluateInward(path);
        }
        else {
            return evaluateOutward(path);
        }
    }

    private Evaluation evaluateOutward(Path path) {
        System.out.println("-- Comparing Outward");

        Node last_node = path.endNode();
        Relationship last_rel = path.lastRelationship();

        // Why?
        if ( last_rel.getType() == Relationships.NEXT_LEG ) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }

        // If last node is a platform and is related is at end station, we've gone too far
        if ( Evaluators.hasGoneTooFar(last_node, end_platform) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE Too Far");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // If the tail of the path is :Platform -> :Platform
        if ( Evaluators.isMultiPlatformTraversal(path) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE :Platform -> :Platform");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        ArrayList<Node> nodes = Utils.nodesToArray( path );
        ArrayList<Relationship> rels = Utils.relsToArray( path );

        // Ignore (l1)-[:CAN_ALIGHT]->(p)-[:CAN_BOARD]->(l2)<-[:NEXT_LEG]-(l1)
        if ( Evaluators.isIgnoringNextLegRelationship(nodes) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE (l1)-[:CAN_ALIGHT]->(p)-[:CAN_BOARD]->(l2)<-[:NEXT_LEG]-(l1)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Missed connection
        if ( Evaluators.hasMissedConnection(nodes, max_waiting) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE MISSED CONNECTION");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Missed connection while transferring platforms
        if ( Evaluators.hasMissedConnectionAcrossPlatforms(nodes, rels, max_waiting) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE MISSED CONNECTION ACROSS PLATFORMS");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    private Evaluation evaluateInward(Path path) {
        System.out.println("-- Comparing Inward");

        /*
        Node last_node = path.endNode();


        // If last node is a platform and is related is at end station, we've gone too far
        if ( Evaluators.hasGoneTooFar(last_node, end_platform) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE Too Far");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

                Don't need this bit - we'll always start at the end node
        */

        // If the tail of the path is :Platform -> :Platform
        if ( Evaluators.isMultiPlatformTraversal(path) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE :Platform -> :Platform");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        ArrayList<Node> nodes = Utils.nodesToArray( path.reverseNodes() );
        ArrayList<Relationship> rels = Utils.relsToArray( path.reverseRelationships() );

        // Ignore (l1)-[:CAN_ALIGHT]->(p)-[:CAN_BOARD]->(l2)<-[:NEXT_LEG]-(l1)
        if ( Evaluators.isIgnoringNextLegRelationship(nodes) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE (l1)-[:CAN_ALIGHT]->(p)-[:CAN_BOARD]->(l2)<-[:NEXT_LEG]-(l1)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Missed connection
        if ( Evaluators.hasMissedConnection(nodes, max_waiting) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE MISSED CONNECTION");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Missed connection while transferring platforms
        if ( Evaluators.hasMissedConnectionAcrossPlatforms(nodes, rels, max_waiting) ) {
            System.out.println("---- EXCLUDE_AND_PRUNE MISSED CONNECTION ACROSS PLATFORMS");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        return Evaluation.EXCLUDE_AND_CONTINUE;
    }


}
