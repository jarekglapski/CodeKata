package net.academy;

import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
interface StringGroupingMethod {
    List<List<String>> execute(Stream<String> s);
}

public class WordGamesTest {
    @Test
    public void getAlphabet() throws Exception {
        WordGames.getAlphabet(dictionaryFile);
    }

    private static final String dictionaryFile = "/slowa.txt";

    @Ignore
    @Test
    public void sortString() throws Exception {

    }

    @Test
    public void getAnagrams() throws Exception {
        System.out.println("\nSerial Execution");
        printStats(runAndMeasure(WordGames::getAnagrams, 2));
    }

    @Test
    public void getAnagramsParallel() throws Exception {
        //System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
        System.out.println("\nParallel Execution");
        printStats(runAndMeasure(WordGames::getAnagramsParallel, 2));
    }

    @Test
    public void getPalindroms() throws Exception {
        List<String> palindroms = WordGames.getPalindroms(WordGames.getLinesStream(dictionaryFile));
        palindroms.forEach(System.out::println);
        System.out.println(palindroms.size());
    }

    @Test
    public void getPalindromicPairs() throws Exception {
        WordGames.printWordGroups(WordGames.getPalindromicPairs(WordGames.getLinesStream(dictionaryFile)));
    }

    @Test
    public void getComposedWords() throws Exception {
        WordGames.getComposedWords(WordGames.getLinesStream(dictionaryFile), 6);
    }

    @Test
    public void getXWords() throws Exception {
        WordGames.getXWords(WordGames.getLinesStream(dictionaryFile));
    }

    static List<Long> runAndMeasure(StringGroupingMethod method, int repetitions) throws URISyntaxException {
        List<Long> execTimes = new ArrayList<>();
        int r = repetitions;

        while (r-- > 0) {
            Stream<String> stream = WordGames.getLinesStream(dictionaryFile);
            long startTime = System.nanoTime();
            List<List<String>> anagrams = method.execute(stream);
            long endTime = System.nanoTime();
            execTimes.add((endTime - startTime) / 1000000);
            WordGames.printWordGroups(anagrams);
        }
        return execTimes;
    }

    static void printStats(List<Long> execTimes) {
        System.out.println(execTimes.stream().collect(Collectors.summarizingLong(Long::longValue)));
    }

}