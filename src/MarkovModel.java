import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MarkovModel {
    HashMap<String, Integer> chunkIds;

    MarkovModel(String filename) {

    }

    static void parseLevel(String filename) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(filename));
            String str;
            String chunks[] = null;
            while((str = r.readLine()) != null) {
                String[] row = str.split(" ");
                if(chunks == null) {
                    chunks = new String[str.length()];
                }
                for(int i = 0; i < chunks.length; i++) {

                }
            }
            int index = 0; // Track index of chunk
        } catch (FileNotFoundException e) {
            System.out.println("No such file: "+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
