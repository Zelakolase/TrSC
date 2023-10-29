package lib;

import java.util.ArrayList;

public class GORead {
    public Graph G = new Graph();

    public GORead(String path, String srcColumn, String destColumn, String weightColumn) {
        String data = new String(IO.read(path));
        /* Converts TSV format to proper CSV format */
        data = data.replaceAll("\t", ",") // Tab character to [,] String (what is in the brackets)
                .replaceFirst("#", ""); // Remove the first '#' to be a double qoute
        data = data.substring(0, data.length() - 2);

        /* Get the column arrangement */
        String[] lines = data.split("\n");
        String headers[] = lines[0].split(",");

        int srcIndex = 0;
        int destIndex = 0;
        int weightIndex = 0;

        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(srcColumn)) srcIndex = i;
            if (headers[i].equals(destColumn)) destIndex = i;
            if (headers[i].equals(weightColumn)) weightIndex = i;
        }

        /* Add data */
        for (int i = 1; i < lines.length; i++) {
            String[] line = lines[i].split(",");
            ArrayList<String> row = new ArrayList<>(3);
            row.add("");
            row.add("");
            row.add("");
            for (int j = 0; j < line.length; j++) {
                if (j == srcIndex) row.set(0, line[j]);
                if (j == destIndex) row.set(1, line[j]);
                if (j == weightIndex) row.set(2, line[j]);
            }

            G.insert(row.get(0), row.get(1), Double.parseDouble(row.get(2)));
        }
    }
}
