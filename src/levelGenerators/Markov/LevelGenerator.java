package levelGenerators.Markov;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import MarkovModel.MarkovModel;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

public class LevelGenerator implements MarioLevelGenerator {
    private int sampleWidth = 10;
    private String folderName = "levels/original/";

    private Random rnd;
    private int levelColumns;
    private String input;
    boolean isMarkovFile;
    public LevelGenerator(int columns, String inputFile, boolean isMarkov) {
        this("levels/original/", 10);
        this.levelColumns = columns;
        this.input = inputFile;
        this.isMarkovFile = isMarkov;
    }

    public LevelGenerator(String sampleFolder) {
        this(sampleFolder, 10);
    }

    public LevelGenerator(String sampleFolder, int sampleWidth) {
        this.sampleWidth = sampleWidth;
        this.folderName = sampleFolder;
    }

    /**
     * Generates a level based on the probabilities and hashmap of the Markov Model created from
     * sampling levels
     */
    public String generateLevel() throws IOException {

        String levelString = "";
        int columnsLeft = this.levelColumns;
        int columnsExtra = 0;
        MarkovModel model;
        //using one of the manually generated markov files
        if(this.isMarkovFile){
            model = MarkovModel.fromFile(this.input);
        } else { //generating markov file from an original project level
            model = MarkovModel.parseLevel(this.input);
        }

        //at largest mapBase can have columns number of strings (1 column per string); usually each string will be a chunk, so more than 1 column
        String[] mapBase = new String[this.levelColumns]; //value is chunk string

        int chunkID = (int) (Math.random() * (100));
        while (model.getChunks().get(chunkID) == null) {
            chunkID = (int) (Math.random() * (100));
        }
        mapBase[0] = model.getChunks().get(chunkID);
        int indexNewline = model.getChunks().get(chunkID).indexOf("\n"); //first newline
        //if first newline appears at index 3, that chunk has a row size of 4; therefore, 4 columns are account for already.
        //subtract 4 from total number of columns needed to be filled in; i.e. if total columns must be 20, only need to fill in 16 more.
        columnsLeft = this.levelColumns - (indexNewline);

        //adapted from
        //https://stackoverflow.com/questions/6737283/weighted-randomness-in-java
        //start from index 1 since index 0 is already filled with first random string
        int i = 1;
        while (columnsLeft > 0) {

            double totalWeight = 1.0d; //this should always be 1

            // Now choose a random item
            int chunkIndex = -1;
            double random = Math.random();
            for (int m = 0; m < model.getProbabilities().length; ++m) {
                random -= model.getProbabilities()[chunkID][m];
                if (random <= 0.0d) {
                    chunkIndex = m;
                    break;
                }
            }
            //if chunk is last one in level, has 0 probabilities for any next chunk
            if (chunkIndex == -1) {
                System.out.println("no chunk!");
                break;
            }
            String nextChunk = model.getChunks().get(chunkIndex); //get randomly chosen chunk string
            mapBase[i] = nextChunk; //place next chunk string into list of chunks
            chunkID = chunkIndex; //move to next chunk

            indexNewline = model.getChunks().get(chunkID).indexOf("\n"); //first newline

            columnsLeft = columnsLeft - (indexNewline);
            //if next chunk is 3 columns but only need 2, extra is 1
            if (columnsLeft < 0) {
                columnsExtra = columnsLeft * -1;
            }
            i++;


        }

        //once mapBase is filled build actual string
        //for each string
        String[][] splitChunks = new String[mapBase.length][];
        for (int n = 0; n < mapBase.length; n++) {
            if (mapBase[n] != null) {
                String[] split = mapBase[n].split("\n"); //split string by newline to get array of rows
                splitChunks[n] = split;
            }
        }

        for (int t = 0; t < 16; t++) {
            for (int y = 0; y < splitChunks.length; y++) { //16 is height of each map
                if (splitChunks[y] != null) {
                    levelString = levelString + splitChunks[y][t]; //add first split of each splitChunk, then second, etc
                }
            }
            levelString = levelString + "\n"; //add newline between rows
        }

        FileWriter fileWriter = new FileWriter("C:/Users/di/Documents/GitHub/IMGD4001-MarkovMario/sample.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(levelString);
        printWriter.close();


        //hashmap is id string
        //2D array is probs

        //specify number of columns; for n columns,
        //rand pick first col by id; get next col by going through its probs and picking corresponding value from 0-99
        //map needs to be 1 string with newlines

        //create array of string chunks to build the level
        //to create actual string, go though each string at same index; once reach n number of strings, insert newline
        //and circle back to next index value


        return levelString;
    }

    private String getRandomLevel() throws IOException {
        File[] listOfFiles = new File(folderName).listFiles();
        List<String> lines = Files.readAllLines(listOfFiles[rnd.nextInt(listOfFiles.length)].toPath());
        String result = "";
        for (int i = 0; i < lines.size(); i++) {
            result += lines.get(i) + "\n";
        }
        return result;
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) throws IOException {
        return generateLevel();
    }

    @Override
    public String getGeneratorName() {
        return "SamplerLevelGenerator";
    }
}
