package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.Labels;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class IsValidLegEvaluator implements Evaluator {

    private final int length;

    private final Long floor;

    private final Long ceiling;

    private final String property;

    public IsValidLegEvaluator(int length, String property, Long floor, Long ceiling) {
        this.length = length;
        this.floor = floor;
        this.ceiling = ceiling;
        this.property = property;
    }

    @Override
    public Evaluation evaluate(Path path) {
        if ( path.length() < length ) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }

        Node last = path.endNode();

        // Check that Node is a :Leg
        if ( ! last.hasLabel(Labels.Leg) ) {
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Get the Property
        Long value = ((Number) last.getProperty(property,0)).longValue();

        // Check that the property is within the bounds, if so, include and stop the traversal
        if ( floor <= value && value <= ceiling ) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }

        // No conditions met,
        return Evaluation.EXCLUDE_AND_PRUNE;
    }


}
