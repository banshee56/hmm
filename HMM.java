import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HMM {
    static boolean exampleTesting = true;  // only tests example-hmm.txt
    static boolean normalTesting = false;    // tests brown or simple tests
    // MUST set one of the above to true alongside consoleTesting to work with the above generated HMM
    static boolean consoleTesting = true;

    static String start="#";
    static Set<String> currStates = new HashSet<>(); // set of current states
    static Map<String, Double> currScores = new HashMap<>(); // current tags to their scores

    // maybe keep them as local variables and not instance?
    static Set<String> nextStates = new HashSet<>(); // set of next states
    static Map<String, Double> nextScores = new HashMap<>(); // next possible tags with their scores

    // from training
    // we train on a similar train file and then test on its corresponding test file
    static Map<String, Map<String, Double>> transitionsMap = new HashMap<>();    // tag (state) to tag, weight
    static Map<String, Map<String, Double>> observationsMap = new HashMap<>();   // tag (state) to word (observation), weight

    // from test file, the actual observations being made
    static String[] observations;  // array of words observed

    // backPointer: state1 -> {state2 -> word/obs}; state1 = new state, state2 = predecessor state, word = observation i
    static List<Map<String, Map<Double, String>>> backPointer;

    /**
     * The Viterbi Algorithm
     * Helps to tag the observations in a sentence by generating backPointer
     */
    public static void viterbi() {
        backPointer = new ArrayList<>();    // create a new backPointer for every new sentence
        currStates.add(start);
        currScores.put(start, 0.0);

        // but values include totals? should we leave totals when creating map?
        // should observations be the # of words in the test file?
        for (int i = 0; i<= observations.length-1; i++) {
            nextStates = new HashSet<>();
            nextScores = new HashMap<>();

            Map<String, Map<Double, String>> mainMap = new HashMap<>(); // the map inside backPointer list

            for (String currState: currStates) {    // getting one of our curr states

                // for full stops or tags that only appear at end of line
                if (!transitionsMap.containsKey(currState)) continue;

                // finding possible next states and their corresponding scores
                for (String nextState: transitionsMap.get(currState).keySet()) {
                    // deal with one possible nextState at a time, calculating its score
                    nextStates.add(nextState);

                    // if observation not found in a state, observation score/unseen penalty is set to -100
                    double observationScore = -100.0;

                    // else if it is found, it equals the probability of finding that observation in the next state
                    if (observationsMap.get(nextState).get(observations[i])!=null) {
                        observationScore = observationsMap.get(nextState).get(observations[i]);
                    }
                    // calculating the next score
                    double nextScore = currScores.get(currState) + transitionsMap.get(currState).get(nextState)
                            + observationScore;

                    if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                        // set nextScores[nextState] to nextScore
                        nextScores.put(nextState, nextScore);

                        // remember that the predecessor of nextState at observation i is currState
                        // mainMap: {nextState -> {score -> currState} }
                        Map<Double, String> innerMap = new HashMap<>(); // the value map of backPointer map
                        innerMap.put(nextScore, currState);

                        // adding the inner score->currState map as value for nextState in mainMap
                        mainMap.put(nextState, innerMap);
                    }
                }
            }
            // adding the main map to the backPointer
            backPointer.add(mainMap);

            // updating current states & scores
            currStates = nextStates;
            currScores = nextScores;
        }
    }

    /**
     * Back tracing after viterbi() and then giving out most likely tags for the observations (words in a sentence)
     * Tags the observed sentence in a test case
     * @return      an ArrayList containing the tags (ordered) of the words in the sentence
     */
    public static List<String> tagObservations() {
        viterbi();  // generating the backPointer

        // get the last map in the backPointer list, find the best score here
        Map<String, Map<Double, String>> lastObservationMap = backPointer.get(backPointer.size()-1);
        ArrayList<String> tagPath = new ArrayList<>(); // the tags of the words in the sentence
        String currState="0";   // initializing the current tag to an unnecessary 0

        // finding the lowest score & last tag of the last observation
        double bestScore = -1500000.0;  // initializing to a horrible score that can easily be bested

        for (String state: lastObservationMap.keySet()) {   // going through the last map in the list
            // finding the best score in the map
            for (Double score: lastObservationMap.get(state).keySet()) {    // going through the keys of scores
                if (bestScore < score) {
                    bestScore = score;   // if score is better, set it to lowest score
                    currState = state;   // update the current best tag
                }
            }
        }
        tagPath.add(currState); // add most likely tag to the path of tags
        for (int ind = backPointer.size()-1; ind>0; ind--) {
            Map<Double, String> innerMap = backPointer.get(ind).get(currState);

            // below only runs once as there is only one score
            for (String nextState: innerMap.values()) {
                tagPath.add(0, nextState);    // adding the corresponding next state
                currState = nextState;  // updating the current state our path is on
            }
        }
        return tagPath;
    }

    /**
     * Helps generate the HMM
     * Counts the number of times a word is observed with a corresponding tag in order to find and create
     * an observationsMap
     * @param sentencesFile                 the file containing train sentences
     * @param tagsFile                      the file containing train tags corresponding to sentences
     * @throws FileNotFoundException        arises due to FileReader()
     */
    public static void TrainingObservations(String sentencesFile, String tagsFile) throws IOException {
        BufferedReader tagsIn = new BufferedReader(new FileReader(tagsFile));   // reads file w corresponding tags

        ArrayList<String> tagsList = new ArrayList<>(); // list of all tags in tagsIn in order

        String line;
        // adding all the tags/states as keys of observationsMap
        while ((line = tagsIn.readLine()) != null) {
            String[] tagsArray = line.toLowerCase().split(" "); // splitting line by space to identify tags
            for (String tag: tagsArray) {
                tagsList.add(tag);  // adding every tag to the list
                if (!observationsMap.containsKey(tag)) observationsMap.put(tag, new HashMap<>());   // only adding keys
            }
        }
        tagsIn.close(); // finished reading tagsFile

        BufferedReader sentencesIn = new BufferedReader(new FileReader(sentencesFile)); // reads file with sentences

        int ind = 0;    // ind gets the tag from tagsList
        while ((line = sentencesIn.readLine()) != null) {
            // using lowercase for words/observations and splitting line by space
            String[] obsArray = line.toLowerCase().split(" ");

            for (String observation: obsArray) {    // grabs a word from sentencesFile (in order)
                String tag = tagsList.get(ind);     // grabs the corresponding tag from tagsList (also ordered)
                Map<String, Double> innerMap = observationsMap.get(tag);    // gets inner map { word -> probability }

                if (!observationsMap.get(tag).containsKey(observation)) {
                    innerMap.put(observation, 1.0); // if the observation is new, put it as appearing 1 time
                } else {
                    innerMap.put(observation, innerMap.get(observation)+1); // update count
                }
                ind++;
            }
        }
        sentencesIn.close();    // finished reading sentencesFile
        logProbabilities(observationsMap);  // calculates logarithmic probabilities to complete observationsMap
        }

    /**
     * Helps generate the HMM by creating a transitionsMap which maps from tag1 to tag2 with their weights
     * @param tagsFile          the file containing the tags in order
     * @throws IOException      rises from FileReader
     */
    public static void TrainingTransitions(String tagsFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(tagsFile));   // reads tagsFile

        String line;
        while ((line = in.readLine()) != null) {
            String[] tagsArray = line.toLowerCase().split(" ");
            String currTag = start; // starts at start

            for (String nextTag: tagsArray) {   // add array list that can index the tags in order
                // if seeing tag for first time
                if (!transitionsMap.containsKey(currTag)) {
                    Map<String, Double> innerMap = new HashMap<>();
                    innerMap.put(nextTag, 1.0); // count is 1
                    transitionsMap.put(currTag, innerMap);  // updates transitionsMap
                }
                else {
                    Map<String, Double> innerMap = transitionsMap.get(currTag);
                    if (innerMap.containsKey(nextTag)) innerMap.put(nextTag, innerMap.get(nextTag)+1);
                    else innerMap.put(nextTag, 1.0);
                }
                currTag = nextTag;
            }
        }
        in.close();
        logProbabilities(transitionsMap);
    }

    /**
     * Counts the totals for each "row" of our "tables" and then uses logarithm to calculate probabilities
     * @param map   either observationsMap or transitionsMap
     */
    public static void logProbabilities(Map<String, Map<String, Double>> map) {
        for (String tag: map.keySet()) {
            Map<String, Double> innerMap = map.get(tag);    // gets inner map corresponding to tag
            double total = 0;   // the total for the tag
            for (Double count: innerMap.values()) {   // goes through values (counts) of innerMap
                total+=count; // increases total by count
            }
            for (String observation: innerMap.keySet()) {
                double probability = innerMap.get(observation); // initially equates count to probability
                probability = Math.log(probability/total);  // calculates actual probability
                innerMap.put(observation, probability);     // updates map
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String fileName = "simple"; // only change this file name for normalTesting

        // training & testing file names
        String trainFileName = "inputs/" + fileName + "-train-";
        String testFileName = "inputs/" + fileName + "-test-";
        String sentencesFile = "sentences.txt";
        String tagsFile = "tags.txt";

        // for example-hmm
        if (exampleTesting) {
            // generates the HMM using POSLib
            ArrayList<Map<String, Map<String, Double>>> mapsList = POSLib.loadData("inputs/example-hmm.txt");
            // grab the maps from the generated HMM, example-hmm.txt
            observationsMap = mapsList.get(0);
            transitionsMap = mapsList.get(1);
        }

        // for normal testing
        else if (normalTesting) {
            // generates the HMM by training using train files
            TrainingObservations(trainFileName + sentencesFile, trainFileName + tagsFile);
            TrainingTransitions(trainFileName + tagsFile);
        }

        // file-based testing for example-hmm, brown, and simple test files
        if ((normalTesting || exampleTesting) && !consoleTesting) {
            BufferedReader in;
            // choosing file name based on testing method
            if (exampleTesting) in = new BufferedReader(new FileReader("inputs/example-sentences.txt"));
            else in = new BufferedReader(new FileReader(testFileName + sentencesFile));

            String line;
            ArrayList<String> tagsResults = new ArrayList<>();  // list of output tags by viterbi

            // getting most likely tags from tagObservations
            while ((line = in.readLine()) != null) {
                observations = line.toLowerCase().split(" ");   // updating observations
                ArrayList<String> path = (ArrayList<String>) tagObservations();
                tagsResults.addAll(path);
            }
            in.close();

            // comparing with the solution tags file
            BufferedReader in1;
            if (exampleTesting) in1 = new BufferedReader(new FileReader("inputs/example-tags.txt"));
            else in1 = new BufferedReader(new FileReader(testFileName + tagsFile));

            ArrayList<String> tagsSolutions = new ArrayList<>();    // list of tags according to solution

            while ((line = in1.readLine()) != null) {
                String[] solutions = line.toLowerCase().split(" ");
                Collections.addAll(tagsSolutions, solutions);   // getting a full list of tags from solution file
            }
            in1.close();

            int i = 0;
            int wrongCount = 0; // number of incorrect tags

            // compares lists at each index and increments wrongCount every time they don't match
            while (i < tagsResults.size()) {
                if (!tagsResults.get(i).equals(tagsSolutions.get(i))) wrongCount++;
                i++;
            }

            // results
            if (exampleTesting) System.out.println("Results of example-hmm:");
            else System.out.println("Results of " + fileName + ":");

            System.out.println("# of tags correct: " + (tagsResults.size() - wrongCount));
            System.out.println("# of tags wrong: " + wrongCount);
        }

        // console-based testing
        if (consoleTesting) {
            if (!exampleTesting && !normalTesting) {
                System.out.println("Please choose exampleTesting or normalTesting to generate an HMM.");
            }
            else {
                Set<String> wordsInHMM = new HashSet<>();  // words in the current HMM
                for (String tag : observationsMap.keySet()) {
                    wordsInHMM.addAll(observationsMap.get(tag).keySet());
                }
                Scanner in = new Scanner(System.in);
                System.out.println("Words in HMM:");
                System.out.println(wordsInHMM);
                System.out.println("Please separate words using a single space.");

                while (consoleTesting) {
                    System.out.println();
                    System.out.println(">");    // input line
                    String line = in.nextLine();

                    // setting observations to the input sentence
                    observations = line.toLowerCase().split(" ");
                    ArrayList<String> path = (ArrayList<String>) tagObservations();
                    System.out.println(path);   // sending out tags as output
                }
            }
        }
    }
}
