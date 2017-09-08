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
        System.out.println("Path length is "+ path.length());

        if ( path.length() < length ) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }

        Node last = path.endNode();

        // Check that Node is a :Leg
        System.out.println("Labels are "+ last.getLabels());
        if ( ! last.hasLabel(Labels.Leg) ) {
            return Evaluation.EXCLUDE_AND_PRUNE;
        }

        // Get the Property
        Long value = ((Number) last.getProperty(property,0)).longValue();

        // Check that the property is within the bounds
        System.out.println("Prop "+ property + " is "+ value);
        if ( value < floor && value > ceiling ) {

            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
System.out.println("OK");
        // This is OK
        return Evaluation.INCLUDE_AND_PRUNE;
    }


}
