package lib;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    /*
     * Every element in edgeTable is an Arraylist where index=0 is src-node, index=1 is dest-node, index=2 is the weight
     */
    public ArrayList<ArrayList<String>> edgeTable = new ArrayList<>();
    /*
     * Every pair is a node and its attributes, where the valuealue is a hashmap its key is property name and value is property v
     */
    public HashMap<String, HashMap<String, String>> nodes = new HashMap<>();

    public void importTSV(String data, String srcColumn, String destColumn, String weightColumn) {
        /* Converts TSV format to proper CSV format */
        data = data.replaceAll("\t", ",") // Tab character to [,] String (what is in the brackets)
        .replaceFirst("#", ""); // Remove the first '#' to be a double qoute
        data = data.substring(0, data.length() - 2);

        /* Get the column arrangement */
        String[] lines = data.split("\n");
        String headers[] = lines[0].split(",");

        int srcIndex = 0; int destIndex = 0; int weightIndex = 0;

        for(int i = 0; i < headers.length; i++) {
            if(headers[i].equals(srcColumn)) srcIndex = i;
            if(headers[i].equals(destColumn)) destIndex = i;
            if(headers[i].equals(weightColumn)) weightIndex = i;
        }

        /* Add data */
        for(int i = 1;i < lines.length; i++) {
            String[] line = lines[i].split(",");
            ArrayList<String> row = new ArrayList<>(3);
            row.add(""); row.add(""); row.add("");
            for(int j = 0;j < line.length; j++) {
                if(j == srcIndex) {
                    row.set(0, line[j]);
                    nodes.putIfAbsent(line[j], new HashMap<>());
                }
                if(j == destIndex) {
                    row.set(1, line[j]);
                    nodes.putIfAbsent(line[j], new HashMap<>());
                }
                if(j == weightIndex) row.set(2, line[j]);
            }

            edgeTable.add(row);
        }
    }

    public ArrayList<String> getIndegree(String nodeName) {
        ArrayList<String> out = new ArrayList<>();

        for(ArrayList<String> row : edgeTable) {
            if(row.get(1).equals(nodeName)) out.add(row.get(0));
        }
        
        return out;
    }

    public ArrayList<String> getOutdegree(String nodeName) {
        ArrayList<String> out = new ArrayList<>();

        for(ArrayList<String> row : edgeTable) {
            if(row.get(0).equals(nodeName)) out.add(row.get(1));
        }
        
        return out;
    }

    public double getWeight(String src, String dest) {
        for(ArrayList<String> row : edgeTable) if(row.get(0).equals(src)) if(row.get(1).equals(dest)) return Double.parseDouble(row.get(2));
        return Integer.MIN_VALUE;
    }

    public void editWeight(String src, String dest, double newWeight) {
        for(ArrayList<String> row : edgeTable) if(row.get(0).equals(src)) if(row.get(1).equals(dest)) {
            row.set(2, String.valueOf(newWeight));
            break;
        }
    }

    public Graph subcluster(ArrayList<String> nodes) {
        Graph out = new Graph();

        for(String node : nodes) {
            /* 1. Add attributes */
            out.nodes.put(node, this.nodes.get(node));
        }

        for(ArrayList<String> edge : edgeTable) {
            /* 2. Add all edges where src-node and dest-node exist within nodes */
            if(nodes.contains(edge.get(0)) && nodes.contains(edge.get(1))) out.edgeTable.add(new ArrayList<>(edge));
        }

        return out;
    }

}
