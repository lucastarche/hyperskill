package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

abstract class FindStrategy {
    protected final Map<String, ArrayList<Integer>> invertedIndex;

    FindStrategy(Map<String, ArrayList<Integer>> map) {
        invertedIndex = map;
    }

    abstract Set<Integer> find(String query);
}

class AllStrategy extends FindStrategy {
    AllStrategy(Map<String, ArrayList<Integer>> map) {
        super(map);
    }

    @Override
    Set<Integer> find(String query) {
        String[] queries = query.split("\\s+");
        Set<Integer> ans = new TreeSet<>();
        if (super.invertedIndex.containsKey(queries[0])) {
            ans.addAll(super.invertedIndex.get(queries[0]));
        }

        for (int i = 1; i < queries.length; i++) {
            ans.retainAll(super.invertedIndex.getOrDefault(
                    queries[i], new ArrayList<>()
            ));
        }

        return ans;
    }
}

class AnyStrategy extends FindStrategy {
    AnyStrategy(Map<String, ArrayList<Integer>> map) {
        super(map);
    }

    @Override
    Set<Integer> find(String query) {
        String[] queries = query.split("\\s+");
        Set<Integer> ans = new TreeSet<>();
        for (String q : queries) {
            if (super.invertedIndex.containsKey(q)) {
                ans.addAll(super.invertedIndex.get(q));
            }
        }

        return ans;
    }
}

class NoneStrategy extends FindStrategy {
    private final int amount;

    NoneStrategy(Map<String, ArrayList<Integer>> map, int amount) {
        super(map);
        this.amount = amount;
    }

    @Override
    Set<Integer> find(String query) {
        Set<Integer> ans = new TreeSet<>();
        for (int i = 0; i < amount; i++) ans.add(i);

        String[] queries = query.split("\\s+");
        for (String q : queries) {
            ans.removeAll(super.invertedIndex.getOrDefault(
                    q, new ArrayList<>()
            ));
        }

        return ans;
    }
}

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<String> people = new ArrayList<>();
    private static final Map<String, ArrayList<Integer>> invertedIndex = new TreeMap<>();

    private static boolean running = true;

    private static void menu() {
        System.out.println("=== Menu ===");
        System.out.println("1. Find a person");
        System.out.println("2. Print all people");
        System.out.println("0. Exit");
        int action = scanner.nextInt();
        scanner.nextLine();
        determineAction(action);
    }

    private static void determineAction(int action) {
        switch (action) {
            case 0:
                running = false;
                break;
            case 1:
                System.out.println("Select a matching strategy: ALL, ANY, NONE");
                String strategy = scanner.nextLine();
                System.out.println("Enter a name or email to search all suitable people.");
                String query = scanner.nextLine();
                findPerson(strategy, query);
                break;
            case 2:
                printAll();
                break;
            default:
                System.out.println("Incorrect option! Try again.");
        }
    }

    private static void findPerson(String strategyString, String query) {
        FindStrategy strategy = determineStrategy(strategyString);

        query = query.toLowerCase().trim();

        Set<Integer> ans = strategy.find(query);
        if (ans.isEmpty()) {
            System.out.println("No matching people found");
        } else {
            for (Integer a : ans) {
                System.out.println(people.get(a));
            }
        }
    }

    private static FindStrategy determineStrategy(String strategyString) {
        switch(strategyString) {
            case "ALL":
                return new AllStrategy(invertedIndex);
            case "ANY":
                return new AnyStrategy(invertedIndex);
            case "NONE":
                return new NoneStrategy(invertedIndex, people.size());
        }

        return new AllStrategy(invertedIndex);
    }

    private static void printAll() {
        System.out.println("=== List of people ===");
        for (String person : people) {
            System.out.println(person);
        }
    }

    private static void preprocessInvertedIndex() {
        for (int i = 0; i < people.size(); i++) {
            for (String name : people.get(i).toLowerCase().split("\\s+")) {
                if (invertedIndex.containsKey(name)) {
                    invertedIndex.get(name).add(i);
                } else {
                    invertedIndex.put(name, new ArrayList<>(Collections.singletonList(i)));
                }
            }
        }
    }

    public static void main(String[] args) {
        File file = new File(args[1]);
        try {
            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNext()) {
                String curr = fileScanner.nextLine();
                people.add(curr);
            }
        } catch (FileNotFoundException e) {
            System.out.println("FILE NOT FOUND!");
            return;
        }

        preprocessInvertedIndex();

        while (running) {
            menu();
        }
    }
}
