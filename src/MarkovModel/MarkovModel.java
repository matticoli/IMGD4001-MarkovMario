/**
 * MarkovModel - Object representation of Markov transition table for Mario level
 * IMGD4100 B19 - Mario Level Generator
 * @author Mikel Matticoli & Diana Kumykova
 */
package MarkovModel;

import java.io.*;
import java.util.HashMap;

public class MarkovModel {
    HashMap<Integer, String> chunks;
    float[][] probabilities;

    /**
     * Create Markov Model from id-chunk map and transition table
     * @param chunks HashMap of unique chunk index to unique chunk
     * @param probabilities transition table (indices are chunk ids)
     */
    private MarkovModel(HashMap<Integer, String> chunks, float[][] probabilities) {
        this.chunks = chunks;
        this.probabilities = probabilities;
    }

    /**
     * Parses manual or programmatically generated analysis file to MarkovModel
     * @param filename name of file containing Markov data (see README for format)
     * @return MarkovModel representation of input file
     */
    public static MarkovModel fromFile(String filename) {
        try {
            // Read in file
            BufferedReader r = new BufferedReader(new FileReader(filename));
            String str = r.readLine();
            String[] l1 = str.split(" ");
            // Get metadata from line 1
            int numChunks = Integer.parseInt(l1[0]);
            int chunkHeight = Integer.parseInt(l1[1]);
            float[][] probabilities = new float[numChunks][numChunks];
            HashMap<Integer, String> chunks = new HashMap<>();
            // get chunks
            for (int i = 0; i < numChunks; i++) {
                String line = r.readLine();
                String[] probsStrArr = line.split(" ");
                // Fill probabilities into transition table
                for (int j = 0; j < numChunks; j++) {
                    probabilities[i][j] = Float.parseFloat(probsStrArr[j]);
                }
                // get chunk
                line = "";
                for (int k = 0; k < chunkHeight; k++) {
                    line += r.readLine() + "\n";
                }
                // add chunk to hashmap
                chunks.put(i, line);
            }
            return new MarkovModel(chunks, probabilities);
        } catch (FileNotFoundException e) {
            System.out.println("No such file");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
        return null;
    }

    /**
     * Writes MarkovModel object to file (see README for format)
     * @param filename file to write to
     * @return true if write was successful, else false
     */
    public boolean toFile(String filename) {
        try {
            PrintWriter p = new PrintWriter(filename);
            // Print metadata line (#chunks and chunkSize)
            p.write(chunks.size() + " " + (chunks.get(0).split("\n").length) + "\n");
            // Print chunk probabilities and chunks
            for (int i = 0; i < probabilities.length; i++) {
                if(chunks.get(i) == null) {
                    break;
                }
                for (int j = 0; j < probabilities[i].length; j++) {
                    p.write(probabilities[i][j] + (j == probabilities[i].length ? "" : " "));
                }
                p.write("\n");
                p.write(chunks.get(i));
            }
            p.flush();
            p.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public HashMap<Integer, String> getChunks() {
        return chunks;
    }

    public float[][] getProbabilities() {
        return probabilities;
    }

    /**
     * Main method for testing - set breakpoint at line
     * @param args not used
     */
    public static void main(String[] args) {
        MarkovModel m = fromFile("levels/levelBreakdowns/notchlvl2_analysis");
        m = parseLevel("levels/notch/lvl-7.txt");
        m.toFile("levels/autoBreakdown/notchlvl7_analysis");
        MarkovModel m1 = parseLevel("levels/ore/lvl-17.txt");
        m1.toFile("levels/autoBreakdown/orelvl17_analysis");
        MarkovModel m2 = parseLevel("levels/original/lvl-12.txt");
        m2.toFile("levels/autoBreakdown/originallvl12_analysis");
        HashMap<Integer, String> chunks = m.getChunks();
        float[][] probabilities = m.getProbabilities();
        System.out.println("Markov complete");
    }

    /**
     * Returns MarkovModel based on analysis of given level file
     * @param filename path of level file to analyze
     * @return
     */
    public static MarkovModel parseLevel(String filename) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(filename));
            // Store current line
            String str;
            // Store chunks
            String chunks[] = null;
            // For each line
            while((str = r.readLine()) != null) {
                // Split line into arr
                String[] row = str.split("");
                // Init chunk arr
                if(chunks == null) {
                    chunks = new String[str.length()];
                }
                // For each char in line
                for(int i = 0; i < row.length; i++) {
                    // Init chunk str
                    if(chunks[i] == null) chunks[i] = "";
                    // Append char to chunk
                    chunks[i] += row[i] + "\n";
                }
            }
            int index = 0; // Track index of chunk
            HashMap<String, Integer> chunkIds = new HashMap<>();
            int[][] occurence = new int[chunks.length][chunks.length];
            int prevChunkIndex = 0;
            for(String s : chunks) {
                // Add chunk id if missing
                if(!chunkIds.containsKey(s)) {
                    chunkIds.put(s, index);
                    if(index != 0) {
                        // update probabilities
                        occurence[index - 1][index]++;
                    }
                    prevChunkIndex = index;
                    index++;
                } else {
                    // if not missing, get the index
                    int ind = chunkIds.get(s);
                    // update probabilities
                    occurence[prevChunkIndex][ind]++;
                    // track prev chunk index
                    prevChunkIndex = index;
                }
            }
            // Swap keys and values
            HashMap<Integer, String> chunkStrings = new HashMap<>();
            for(String key : chunkIds.keySet()) {
                chunkStrings.put(chunkIds.get(key), key);
            }
            // Build probability table
            float[][] probability = new float[chunkIds.size()][chunkIds.size()];
            for (int j = 0; j < chunkIds.size(); j++) {
                int[] chunkOcc = occurence[j];
                float sum = 0;
                for (int i = 0; i < chunkIds.size(); i++) {
                    sum += occurence[j][i];
                }
                for (int i = 0; i < chunkIds.size(); i++) {
                    probability[j][i] = occurence[j][i] / sum;
                }
            }
            return new MarkovModel(chunkStrings, probability);
        } catch (FileNotFoundException e) {
            System.out.println("No such file: "+filename);
        } catch (IOException e) {
            System.out.println("Error reading file: "+filename);
        }
        return null;
    }
}