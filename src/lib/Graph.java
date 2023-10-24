package lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Undirected weighted graph object
 * @author Morad
 */

@SuppressWarnings("unchecked")
public class Graph {
    /**
     * Key: Src./Dest. node name | Value: [0: (Key: Property name | Value: Property value) | 1: (Key: Dest./Src. node name | Value: Weight)]
     * Expect all nodes in G.Key and G.Value.1.Key.
     * Duplicate reciprocal entries exist by design to enhance performance.
     */
    public HashMap<String, List<HashMap>> G = new HashMap<>();

    /**
     * Inserts new edge (and nodes) to Graph Object
     * @param N1 One of edge end nodes name
     * @param N2 The other edge end node name
     * @param Weight The decimal weight between N1 and N2 (and vice versa)
     */
    public void insert(String N1, String N2, double Weight) {
        /* 1. Whether to add input node(s) to G.Key */
        boolean addN1toGKey = ! G.containsKey(N1);
        boolean addN2toGKey = ! G.containsKey(N2);
        
        /* 2. Add one/all/none input nodes to G.Key */
        if(addN1toGKey) G.put(N1, Arrays.asList(new HashMap<String, String>(), new HashMap<String, Double>()));
        if(addN2toGKey) G.put(N2, Arrays.asList(new HashMap<String, String>(), new HashMap<String, Double>()));

        /* 3. Put Connection Entry */
        G.get(N1).get(1).put(N2, Weight);
        G.get(N2).get(1).put(N1, Weight);
    }

    /**
     * Get the decimal weight between N1 and N2
     * @param N1 One of edge end nodes name
     * @param N2 The other edge end node name
     * @return The decimal weight between N1 and N2 (and vice versa)
     */
    public double getWeight(String N1, String N2) {
        return (double) G.get(N2).get(1).get(N1);
    }

    /**
     * Modify edge weight between N1 and N2
     * @param N1 One of edge end nodes name
     * @param N2 The other edge end node name
     * @param newWeight The [new] decimal weight between N1 and N2 (and vice versa)
     */
    public void editWeight(String N1, String N2, double newWeight) {
        /* 1. N1 to N2 edit */
        G.get(N1).get(1).replace(N2, newWeight);
        /* 2. N2 to N1 edit */
        G.get(N2).get(1).replace(N1, newWeight);
    }
    
    /**
     * Multiplies the edge weight between N1 and N2 by a factor
     * @param N1 One of edge end nodes name
     * @param N2 The other edge end node name
     * @param weightMultiplier The weight multiplier [1.5:+50%, 2.0:+100%, etc..]
     */
    public void multiplyWeight(String N1, String N2, double weightMultiplier) {
        double oldWeight = getWeight(N1, N2);
        editWeight(N1, N2, oldWeight * weightMultiplier);
    }

    /**
     * Get all connected nodes with node N
     * @param N The name of node
     * @return All node names connected to node N
     */
    public Set<String> getConnectedNodes(String N) {
        return G.get(N).get(1).keySet();
    }

    /**
     * Make a new graph object with all nodes in input and their interconnected edges
     * @param Nodes Node names to be included in the new sub-cluster
     * @return The sub-cluster graph object
     */
    public Graph subcluster(ArrayList<String> Nodes) {
        Graph tempG = new Graph();

        for(String node : Nodes) {
            HashMap<String, Double> nodeOldConnections = G.get(node).get(1);
            nodeOldConnections.keySet().retainAll(Nodes);
            tempG.G.put(node, Arrays.asList(G.get(node).get(0), new HashMap<>(nodeOldConnections)));
        }

        return tempG;
    }

    /**
     * Adds a property name and value to node N
     * @param N Target node name
     * @param propertyName Name of N property
     * @param propertyValue Value of N property
     */
    public void addProperty(String N, String propertyName, String propertyValue) {
        G.get(N).get(0).put(propertyName, propertyValue);
    }

    /**
     * Edit property value for a specific property name in node N
     * @param N Target node name
     * @param propertyName Target N property name
     * @param propertyNewValue New value for property in node N
     */
    public void editProperty(String N, String propertyName, String propertyNewValue) {
        G.get(N).get(0).replace(propertyName, propertyNewValue);
    }

    /**
     * Get the property value for a specific property in node N
     * @param N Target node name
     * @param propertyName Target N property name
     * @return The property value desired
     */
    public String getProperty(String N, String propertyName) {
        return (String) G.get(N).get(0).get(propertyName);
    }
}