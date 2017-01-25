package net.academy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Anagrams {
    private static final String dictionaryFile = "/test.txt";
    private static List<String> longest;
    private static List<String> mostCommon;
    private static int maxSize;
    private static int maxLength;

    public static void main(String args[]) throws URISyntaxException {

        try (Stream<String> stream = Files.lines(Paths.get(Anagrams.class.getResource(dictionaryFile).toURI()))) {
            long startTime = System.nanoTime();
            List<List<String>> anagrams = stream
                    .collect(Collectors.groupingBy(s -> {
                        char[] v = s.toCharArray();
                        Arrays.sort(v);
                        return new String(v);
                    }))
                    .values().stream().filter(group -> group.size() > 1).collect(Collectors.toList());
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;

            anagrams.forEach(group -> {
                        group.forEach(s -> System.out.printf("%s ", s));
                        System.out.println();
                        if (group.size() > maxSize) {
                            maxSize = group.size();
                            mostCommon = group;
                        }
                        if (group.get(0).length() > maxLength) {
                            maxLength = group.get(0).length();
                            longest = group;
                        }
                    }
            );
            System.out.println("============================");
            System.out.printf("anagram groups found: %s\n", anagrams.size());
            System.out.printf("duration: %s ms\n", duration);
            System.out.printf("largest group: (%d members) - %s\n", mostCommon.size(), mostCommon);
            System.out.printf("longest anagram: %s\n", longest);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
