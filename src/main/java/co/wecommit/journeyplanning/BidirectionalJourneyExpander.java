package co.wecommit.journeyplanning;

import co.wecommit.journeyplanning.evaluators.CanTransferBetweenPlatformsEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import co.wecommit.journeyplanning.Relationships;
import org.neo4j.helpers.collection.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;


public class BidirectionalJourneyExpander implements PathExpander<Long> {

    private final Collection EMPTY = Collections.emptyList();

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Long> state) {

        if ( path.length() == 1 ) {
            return path.endNode().getRelationships(
                    Direction.OUTGOING,
                    Relationships.CAN_TRANSFER_TO,
                    Relationships.CAN_BOARD
            );
        }

        ArrayList<Node> nodes = new ArrayList<>();
        path.nodes().forEach(nodes::add);

        ArrayList<Relationship> rels = new ArrayList<>();
        path.relationships().forEach(rels::add);

        Node last = path.endNode();
        Node penultimate = nodes.get( nodes.size()-1 );

        if ( last.hasLabel(Labels.Leg) ) {
            state.setState( Utils.getLong(last, "arrives_at") );
        }
        else if ( last.hasLabel(Labels.Platform) ) {
            if ( penultimate.hasLabel(Labels.Platform) ) {
                state.setState( state.getState() + (Long) rels.get( rels.size() - 1 ).getProperty("minutes") );
            }
            else {
                // Already two platforms, must board at this point
                return path.endNode().getRelationships(Direction.OUTGOING, Relationships.CAN_BOARD);

            }

        }


        // :Leg -> [:NEXT_LEG|CAN_ALIGHT]->
        if ( last.hasLabel(Labels.Leg) ) {
            return filter( path, path.endNode().getRelationships(Direction.OUTGOING, Relationships.NEXT_LEG, Relationships.CAN_ALIGHT) );
        }

        // :Platform -> [:CAN_BOARD|CAN_TRANSFER_TO]->
        else if ( last.hasLabel(Labels.Platform) ) {
            if ( penultimate.hasLabel(Labels.Platform) ) {
                return path.endNode().getRelationships(Direction.OUTGOING, Relationships.CAN_BOARD);
            }

            return path.endNode().getRelationships(
                    Direction.OUTGOING,
                    Relationships.CAN_TRANSFER_TO,
                    Relationships.CAN_BOARD
            );
        }

        return EMPTY;
    }

    public static Iterable<Relationship> filter(Path path, Iterable<Relationship> rels) {



        return Iterables.stream(rels)
                .filter(a -> {
                    if (1==2) return false;

                    return true;
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }


    public void doSomething(Node node, Direction direction, RelationshipType ...rels) {
        node.getRelationships(direction, rels);
    }

    @Override
    public PathExpander<Long> reverse() {
        return new PathExpander<Long>() {

            @Override
            public Iterable<Relationship> expand(Path path, BranchState<Long> state) {
                ArrayList<Node> nodes = new ArrayList<>();
                path.reverseNodes().forEach(nodes::add);

                Node last = path.endNode();
                Node penultimate = nodes.get( nodes.size()-1 );

                // :Leg -> <-[:NEXT_LEG|CAN_BOARD]-
                if ( last.hasLabel(Labels.Leg) ) {
                    return path.endNode().getRelationships(
                            Direction.INCOMING,
                            Relationships.NEXT_LEG,
                            Relationships.CAN_BOARD
                    );
                }

                // :Platform -> <-[:CAN_BOARD|CAN_TRANSFER_TO]-
                else if ( last.hasLabel(Labels.Platform) ) {

                    if ( penultimate.hasLabel(Labels.Platform) ) {
                        return path.endNode().getRelationships(Direction.INCOMING, Relationships.CAN_BOARD);
                    }

                    return path.endNode().getRelationships(
                            Direction.INCOMING,
                            Relationships.CAN_TRANSFER_TO,
                            Relationships.CAN_ALIGHT
                    );
                }

                return Collections.emptyList();
            }

            @Override
            public PathExpander<Long> reverse() {
                return null;
            }
        };
    }
}
