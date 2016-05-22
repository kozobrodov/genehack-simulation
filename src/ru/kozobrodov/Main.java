package ru.kozobrodov;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        int experimentsNumber = 500;
        int inputSize = 533858;
        int genLimit = 10;
        String data = "/home/eugene/GeneHack/96_gt";
        String experiments = "/home/eugene/GeneHack/experiments";
        String positions = "/home/eugene/IdeaProjects/MateSimulation/positions/";
        for (String arg : args) {
            final String[] parts = arg.split("=");
            if (parts.length != 2) {
                System.err.println("Unknown argument: " + arg);
                continue;
            }
            switch (parts[0]) {
                case "expNum":
                    experimentsNumber = Integer.parseInt(parts[1]);
                    break;
                case "inputSize":
                    inputSize = Integer.parseInt(parts[1]);
                    break;
                case "genLimit":
                    genLimit = Integer.parseInt(parts[1]);
                    break;
                case "dataDir":
                    data = parts[1];
                    break;
                case "expDir":
                    experiments = parts[1];
                    break;
                case "posDir":
                    positions = parts[1];
                    break;
            }
        }

        final Map<String, List<Simulator.Pair>> inputMates = new HashMap<>();
        int k = 1;
        final File dataDir = new File(data);
        if (!dataDir.isDirectory()) {
            System.err.println("Not a directory");
            System.exit(1);
        }
        final File[] list = dataDir.listFiles();
        assert list != null;
        for (File f : list) {
            System.out.println((k++) + " Reading: " + f.getPath());
            final BufferedReader reader = new BufferedReader(new FileReader(f));
            final List<Simulator.Pair> result = new ArrayList<>(inputSize);
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("/");
                result.add(new Simulator.Pair(parts[0].intern(), parts[1].intern()));
            }
            inputMates.put(f.getAbsolutePath(), result);
            reader.close();
        }

        final int genNum = Math.min(genLimit, list.length / 2);
        for (int i = 0; i < experimentsNumber; i++) {
            final String childPath = experiments + "/" + i;
            final int iteratorInitValue = i * genNum * 4; // generation = 2 child = 4 crossingovers
            final Simulator simulator = new Simulator(data, childPath, positions, inputSize, iteratorInitValue);
            simulator.start(inputMates, genLimit);
        }
    }
}
