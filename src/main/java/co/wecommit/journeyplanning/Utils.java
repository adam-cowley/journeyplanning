package co.wecommit.journeyplanning;

import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by adam on 11/09/2017.
 */
public class Utils {

    public static Long getLong(Node node, String property) {
        return ((Number) node.getProperty(property,0)).longValue();
    }

    public static Long getLong(Relationship rel, String property) {
        return ((Number) rel.getProperty(property,0)).longValue();
    }

    public static Date getDate(Node node, String property) {
        Long value = ((Number) node.getProperty(property,0)).longValue();

        return new Date( value * 60 * 1000 );
    }

    public static boolean hasDegree(Node n1, Node n2, RelationshipType type, Direction direction) {
        for ( Relationship rel : n1.getRelationships(direction, type) ) {
            if ( rel.getOtherNode(n1).equals(n2) ) {
                return true;
            }
        }

        return false;
    }


    public static ArrayList<Node> nodesToArray(Iterable<Node> path) {
        ArrayList<Node> nodes = new ArrayList<>();
        path.forEach(nodes::add);

        return nodes;
    }

    public static ArrayList<Relationship> relsToArray(Iterable<Relationship> path) {
        ArrayList<Relationship> rels = new ArrayList<>();
        path.forEach(rels::add);

        return rels;
    }

    public static ArrayList<Node> nodesToArray(Path path) {
        ArrayList<Node> nodes = new ArrayList<>();
        path.nodes().forEach(nodes::add);

        return nodes;
    }

    public static ArrayList<Relationship> relsToArray(Path path) {
        ArrayList<Relationship> rels = new ArrayList<>();
        path.relationships().forEach(rels::add);

        return rels;
    }

    public static void print(Path path) {
        System.out.println("--");

        Node n = path.startNode();

        System.out.print("-- ("+  n.getId()+ ":"+ n.getLabels()+")");

        for ( Relationship r : path.relationships() ) {
            String start = "";
            String end = "";

            if (r.getStartNode().equals(n)) {
                end = ">";
            }
            else {
                start = "<";
            }

            System.out.print(start+ "-[;"+ r.getType() +"]-"+ end);

            n = r.getOtherNode(n);

            System.out.print("("+ n.getId() +":"+n.getLabels()+")");
        }


        System.out.println("");

        System.out.print("-- [");

        for ( Node rn : path.nodes() ) {
            System.out.print(rn.getId() + ",");
        }

        System.out.println("]");
    }








}
