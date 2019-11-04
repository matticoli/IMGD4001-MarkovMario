import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MarkovModel {
    HashMap<Integer, String> chunks;
    float[][] probabilities;

    MarkovModel(HashMap<Integer, String> chunks, float[][] probabilities) {
        this.chunks = chunks;
        this.probabilities = probabilities;
    }

    MarkovModel(String filename) {
        //TODO: Implement
    }

    public HashMap<Integer, String> getChunks() {
        return chunks;
    }

    public float[][] getProbabilities() {
        return probabilities;
    }

    public static void main(String[] args) {
        MarkovModel m = parseLevel("levels/notch/lvl-1.txt");
        HashMap<Integer, String> chunks = m.getChunks();
        float[][] probabilities = m.getProbabilities();
        System.out.println("Markov complete");
    }

    /**
     * Returns MarkovModel based on analysis of given level file
     * @param filename path of level file to analyze
     * @return
     */
    static MarkovModel parseLevel(String filename) {
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
            float[][] probability = new float[occurence.length][occurence[0].length];
            for (int j = 0; j < occurence.length; j++) {
                int[] chunkOcc = occurence[j];
                float sum = 0;
                for (int i = 0; i < chunkOcc.length; i++) {
                    sum += occurence[j][i];
                }
                for (int i = 0; i < chunkOcc.length; i++) {
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
