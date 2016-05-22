package ru.kozobrodov;

import java.io.*;
import java.util.*;

class Simulator {
    private static int[][][] crossingoverPositions;

    private static void initCrossingoverPositions(String path) {
        crossingoverPositions = new int[20000][22][];
        for (int i = 0; i < 22; i++) {
            final File file = new File(path + (i + 1) + ".txt");
            try {
                final BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    final String[] lineParts = line.split("\\s");
                    final int positionsNumber = Double.valueOf(lineParts[0]).intValue();
                    final int[] positions = new int[positionsNumber];
                    for (int j = 1; j < lineParts.length; j++) {
                        positions[j - 1] = Double.valueOf(lineParts[j]).intValue();
                    }
                    Arrays.sort(positions);
                    crossingoverPositions[lineNum++][i] = positions;
                }
            } catch (IOException e) {
                System.err.println("Cannot collect crossingover positions");
            }
        }
    }

    private static final int[] CHROMOSOME_BOUNDS = new int[]{
            49321, 91440, 126940, 156199, 185712, 219906, 247613, 272028, 295193, 319840, 350501,
            377580, 393012, 410496, 427831, 447685, 469456, 481696, 503747, 517421, 524475, 533859
    };

    private final String dataPath;
    private final String childPath;
    private final int inputSize;
    private int iter;

    Simulator(String dataPath, String childPath, String positions, int inputSize, int iter) {
        this.dataPath = dataPath;
        this.childPath = childPath;
        this.inputSize = inputSize;
        this.iter = iter;
        initCrossingoverPositions(positions);
    }

    void start(Map<String, List<Pair>> inputMates, int genLimit) throws IOException {
        final File dataDir = new File(dataPath);
        if (!dataDir.isDirectory()) {
            System.err.println("Not a directory");
            System.exit(1);
        }
        final File childDir = new File(childPath);
        if (!childDir.exists() && !childDir.mkdirs()) {
            System.err.println("Cannot create child directory");
            System.exit(10);
        }
        final File[] list = dataDir.listFiles();
        assert list != null;
        shuffleArray(list);

        final List<Pair> parent1 = makeCrossingovers(inputMates.get(list[0].getAbsolutePath()), iter++);
        final List<Pair> parent2 = makeCrossingovers(inputMates.get(list[1].getAbsolutePath()), iter++);
        createAndSaveChild(parent1, parent2, childPath + "/0");
        final List<Pair> parent3 = makeCrossingovers(inputMates.get(list[0].getAbsolutePath()), iter++);
        final List<Pair> parent4 = makeCrossingovers(inputMates.get(list[1].getAbsolutePath()), iter++);
        createAndSaveChild(parent3, parent4, childPath + "/1");

        for (int i = 2; i < list.length && i / 2 < genLimit; i++) {
            final List<Pair> parent = makeCrossingovers(inputMates.get(list[i].getAbsolutePath()), iter++);
            final List<Pair> parentChild = makeCrossingovers(
                    matesCache.get(childPath + "/" + (i - 2)),
                    iter++
            );
            createAndSaveChild(parent, parentChild, childPath + "/" + i);
        }
    }

    private void createAndSaveChild(List<Pair> mate1, List<Pair> mate2, String path) throws IOException {
        final boolean left1 = getRandomBoolean();
        final boolean left2 = getRandomBoolean();
        final FileWriter fw = new FileWriter(path);
        final List<Pair> result = new ArrayList<>(inputSize);
        for (int i = 0; i < mate1.size(); i++) {
            final Pair pair = new Pair(
                    left1 ? mate1.get(i).left : mate1.get(i).right,
                    left2 ? mate2.get(i).left : mate2.get(i).right);
            fw.write(pair.left + "/" + pair.right + "\n");
            result.add(pair);
        }
        fw.close();
        matesCache.put(path, result);
    }

    // I our case it will expect about 400Mb of memory
    private final Map<String, List<Pair>> matesCache = new HashMap<>();

    private List<Pair> makeCrossingovers(List<Pair> mate, int iter) throws IOException {
        final int[][] positions = crossingoverPositions[iter];
        final List<Pair> pairs = new ArrayList<>(inputSize);
        int currentChromosome = 0;
        int position = 0;
        int breakNumber = 0;
        boolean itsCrossingoverTime = false;
        for (Pair p : mate) {
            if (positions[currentChromosome].length > breakNumber &&
                    positions[currentChromosome][breakNumber++] == position
            ) {
                itsCrossingoverTime = !itsCrossingoverTime;
            }
            pairs.add(new Pair(itsCrossingoverTime ? p.right : p.left, itsCrossingoverTime ? p.left : p.right));
            if (CHROMOSOME_BOUNDS[currentChromosome] < position) {
                currentChromosome++;
            }
        }
        return pairs;
    }

    private static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    private static File[] shuffleArray(File[] array){
        Random r = new Random();  // Random number generator
        for (int i = 0; i < array.length; i++) {
            int randomPosition = r.nextInt(array.length);
            File temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        return array;
    }

    static class Pair {
        private final String left;
        private final String right;

        Pair(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }
}
