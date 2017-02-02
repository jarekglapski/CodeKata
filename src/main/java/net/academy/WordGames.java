package net.academy;

import com.sun.deploy.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class WordGames {
    private static final String dictionaryFile = "/test.txt";

    static String sortString(String value) {
        char[] array = value.toCharArray();
        Arrays.sort(array);
        return new String(array);
    }

    protected static int reduceToHash(String value, Map<Integer, Byte> alphabet) {
        char array[] = new char[alphabet.size()];
        value.chars().forEach(c -> array[alphabet.get(c)]++);
        return getHash(array);
    }

    protected static int getHash(char a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (char element : a)
            result = 71 * result + element;

        return result;
    }

    static List<List<String>> getAnagrams(Stream<String> stream) {
        return stream
                .collect(Collectors.groupingBy(WordGames::sortString))
                .values().stream().filter(group -> group.size() > 1).collect(Collectors.toList());

    }

    static List<List<String>> getAnagramsParallel(Stream<String> stream) {
        return stream.parallel().unordered()
                .collect(Collectors.groupingByConcurrent(WordGames::sortString))
                .values().parallelStream().unordered().filter(group -> group.size() > 1).collect(Collectors.toList());

    }

    static List<List<String>> getAnagramsByAlphabet(Stream<String> stream, Map<Integer, Byte> alphabet) {
        return stream
                .collect(Collectors.groupingBy(s -> reduceToHash(s, alphabet)))
                .values().stream().filter(group -> group.size() > 1).collect(Collectors.toList());

    }

    static List<String> getPalindroms(Stream<String> stream) {
        return stream
                .filter(s -> s.length() > 2)
                .filter(s -> s.equals(new StringBuilder(s).reverse().toString()))
                .collect(Collectors.toList());
    }

    static List<List<String>> getPalindromicPairs(Stream<String> stream) {
        return stream
                .filter(s -> s.length() > 2)
                .collect(Collector.of(
                        HashMap<String, List<String>>::new,
                        (map, key) -> {
                            map.compute(key, (value, list) -> {
                                if (list == null) {
                                    list = new ArrayList<>();
                                }
                                list.add(value);
                                return list;
                            });
                            String reversed = new StringBuilder(key).reverse().toString();
                            map.compute(reversed, (value, list) -> {
                                if (list == null) {
                                    list = new ArrayList<>();
                                }
                                list.add(key);
                                return list;
                            });
                        },
                        (left, right) -> {
                            left.putAll(right);
                            return left;
                        }))
                .values().stream()
                .collect(Collectors.toSet())
                .stream().filter(group -> group.size() > 1).collect(Collectors.toList());
    }

    static List<List<String>> getComposedWords(Stream<String> wordStream, int length) {
        Map<Integer, Set<String>> map = wordStream
                .filter(word -> word.length() > 1 && word.length() <= length)
                .collect(Collectors.groupingBy(String::length, Collectors.toSet()));
        for (int i = 2; i < length-1; i++) {
            getComposedWords(map, length, i);
        }
        return null;
    }

    static List<List<String>> getXWords(Stream<String> wordStream) {
        long startTime = System.nanoTime();
        Map<Character,Set<String>> wordsByFirstChar = wordStream.collect(Collectors.groupingBy(s -> s.charAt(0),Collectors.mapping(s -> s.substring(1),Collectors.toSet())));
        long time1 = System.nanoTime();
        //wordsByFirstChar.get('a').forEach(System.out::println);
        Set<String> intersection = new HashSet<String>(wordsByFirstChar.get('a'));
        long time2 = System.nanoTime();
        intersection.retainAll(wordsByFirstChar.get('j'));
        long time3 = System.nanoTime();
        System.out.println((time1 - startTime) / 1000000);
        System.out.println((time2 - time1) / 1000000);
        System.out.println((time3 - time2) / 1000000);
        intersection.forEach(System.out::println);
        return null;
    }

    static void getComposedWords(Map<Integer, Set<String>> wordsByLength, int length, int prefixLength) {
        int suffixLength = length - prefixLength;
        wordsByLength.get(length).stream()
                .collect(
                        HashMap<String, List<String>>::new,
                        (wordsByPrefix, word) -> {
                            String prefix = word.substring(0, prefixLength);
                            if (wordsByLength.get(prefixLength).contains(prefix)) {
                                wordsByPrefix.compute(prefix, (k, list) -> {
                                    if (list == null) {
                                        list = new ArrayList<>();
                                    }
                                    String suffix = word.substring(prefixLength);
                                    if (wordsByLength.get(suffixLength).contains(suffix)) {
                                        list.add(suffix);
                                    }
                                    return list;
                                });
                            }
                        },
                        Map::putAll)
                .entrySet().stream().filter(stringListEntry -> !stringListEntry.getValue().isEmpty()).forEach(System.out::println);
    }

    public static void main(String args[]) {

        Map<Integer, Byte> alphabet = new HashMap<>();
        byte i = 0;
        for (Character l : getAlphabet(dictionaryFile).keySet()) {
            alphabet.put((int) l, i++);
        }

        Stream<String> stream = getLinesStream(dictionaryFile);

        long startTime = System.nanoTime();
        List<List<String>> anagrams = getAnagramsByAlphabet(stream, alphabet);
        long endTime = System.nanoTime();

        printWordGroups(anagrams);
        System.out.printf("execution time: %d", (endTime - startTime) / 1000000);
    }

    static void printWordGroups(List<List<String>> anagrams) {
        List<String> longest = null;
        List<String> mostCommon = null;
        int maxSize = 0;
        int maxLength = 0;

        for (List<String> group : anagrams) {
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
        System.out.println("============================");
        System.out.printf("groups found: %s\n", anagrams.size());
        if (anagrams.size() > 0) {
            System.out.printf("largest group: (%d members) - %s\n", mostCommon.size(), mostCommon);
            System.out.printf("longest word: %s\n", longest);
        }
    }

    static Map<Character, Long> getAlphabet(String fileName) {
        Map<Character, Long> alphabet = new HashMap<>();
        getLinesStream(fileName)
                .flatMapToInt(CharSequence::chars)
                //.map(Character::toLowerCase)
                .filter(character -> !Character.isWhitespace(character))
                .forEach(character -> alphabet.merge((char) character, 1L, Long::sum));
        //alphabet.entrySet().stream().sorted((o1, o2) -> Long.compare(o1.getKey(), o2.getKey())).forEach(entry -> System.out.printf("%s, ", entry.getKey()));
        return alphabet;
    }

    protected static Stream<String> getLinesStream(String fileName) {
        try {
            return Files.lines(Paths.get(WordGames.class.getResource(fileName).toURI()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
