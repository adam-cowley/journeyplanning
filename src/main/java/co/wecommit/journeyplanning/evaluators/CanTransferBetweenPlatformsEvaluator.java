package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.FindServices;
import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static co.wecommit.journeyplanning.Utils.hasDegree;
import static co.wecommit.journeyplanning.evaluators.Evaluators.isIgnoringNextLegRelationship;


public class CanTransferBetweenPlatformsEvaluator implements Evaluator {

    private final int max_waiting;

    public CanTransferBetweenPlatformsEvaluator(int max_waiting) {
        this.max_waiting = max_waiting;
    }

    @Override
    public Evaluation evaluate(Path path) {
        ArrayList<Node> nodes = new ArrayList<>();
        path.nodes().forEach(nodes::add);

        ArrayList<Relationship> rels = new ArrayList<>();
        path.relationships().forEach(rels::add);

        Utils.print(path);

        if (path.endNode().hasLabel(Labels.Leg)) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        else if (true) {
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        if ( isIgnoringNextLegRelationship(nodes) ) {
            Utils.print(path);
            System.out.println("-->EXCLUDE_AND_PRUNE (Next Leg)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        if ( isMultiPlatformTransfer(nodes) ) {
            System.out.println("-->EXCLUDE_AND_PRUNE (Multi)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        if ( cannotCatchLeg(nodes) ) {
            System.out.println("--->EXCLUDE_AND_PRUNE (MISSED CONNECTION)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        if ( cannotCatchLegAfterTransfer(nodes, rels) ) {
            System.out.println("--->EXCLUDE_AND_PRUNE (MISSED CONNECTION AFTER PLATFORM)");
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    private boolean isMultiPlatformTransfer(ArrayList<Node> nodes) {
        int size = nodes.size();

        if ( size < 3 ) {
            return false;
        }

        Node n1 = nodes.get(size -3);
        Node n2 = nodes.get(size -2);
        Node n3 = nodes.get(size -1);

        if ( n1.hasLabel(Labels.Platform) && n2.hasLabel(Labels.Platform) && n3.hasLabel(Labels.Platform) ) {
            return true;
        }

        return false;
    }





    /**
     * (l1:Leg)-[:CAN_ALIGHT]->(p1:Platform)-[:CAN_BOARD]->(l2:Leg)
     * where l1.arrival_time < l2.departure_time and l2.departure_time < l1.arrival_time + threshold
     *
     * @param nodes
     * @return
     */
    private boolean cannotCatchLeg(ArrayList<Node> nodes) {
        int nodesize = nodes.size();

        if ( nodesize < 2 ) {
            return false;
        }

        Node l1 = nodes.get( nodesize -2 );
        Node l2 = nodes.get( nodesize -1 );

        return getLong(l1, "arrival_time") < getLong(l2, "departure_time");
    }

    /**
     (l1:Leg)-[:CAN_ALIGHT]->(p1:Platform)-[can_transfer:CAN_TRANSFER_TO]->(p2:Platform)-[:CAN_BOARD]->(l2:Leg)
     *
     * WHERE a.arrival_time + r.minutes + {max_waiting} <= b.departs_at
     *
     *
     * @param nodes
     * @param rels
     * @return boolean
     */
    private boolean cannotCatchLegAfterTransfer(ArrayList<Node> nodes, ArrayList<Relationship> rels) {
        int nodesize = nodes.size();
        int relsize = rels.size();

        if ( nodesize < 4 ) {
            return false;
        }


        Node l1 = nodes.get( nodesize -4 );
        Node p1 = nodes.get( nodesize -3 );
        Node p2 = nodes.get( nodesize -2 );
        Node l2 = nodes.get( nodesize -1 );

        Relationship can_transfer = rels.get( relsize -2 );

        if ( l1.hasLabel(Labels.Leg) && l2.hasLabel(Labels.Leg) && p1.hasLabel(Labels.Platform) && p2.hasLabel(Labels.Platform) ) {

            Long arrival_time = getLong(l1, "arrives_at");
            Long transfer_time = getLong(can_transfer, "minutes");
            Long waiting = new Long(max_waiting);

            Long departure_time = getLong(l2, "departs_at");

            Long arrival_at_platform = arrival_time + transfer_time + waiting;
System.out.println("AAP " + arrival_at_platform + " COMP "+ departure_time);
            if ( arrival_at_platform >= departure_time ) {
                return true;
            }

        }

        return false;

    }

    private Long getLong(Node node, String property) {
        return ((Number) node.getProperty(property,0)).longValue();
    }

    private Long getLong(Relationship node, String property) {
        return ((Number) node.getProperty(property,0)).longValue();
    }
}
