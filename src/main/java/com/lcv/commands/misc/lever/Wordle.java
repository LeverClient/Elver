package com.lcv.commands.misc.lever;

import com.lcv.Main;
import com.lcv.commands.Embed;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Wordle
{
    private static final List<String> WORD_LIST = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/wordle.txt"))).lines().toList();

    private final String word;
    private final Map<Character, Integer> WORD_FREQUENCY;
    private boolean isGreen;

    public Wordle(String word)
    {
        this.word = word;
        this.WORD_FREQUENCY = getFrequency(this.word);
        this.isGreen = true;
    }

    public MessageEmbed getWords()
    {
        String desc = Arrays.stream(new String[]{
                        "xoxxx",
                        "xoxoo",
                        "xxoxx",
                        "ooxox",
                        "xxxox",
                        "ooooo"
                })
                .map(this::getWordFormatted)
                .map(String::trim)
                .takeWhile(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"))
                .trim();
        return new Embed().setTitle("Wordle - " + word)
                .setDescription((desc.lines().count() != 6) ? "Not Possible!" : desc)
                .get();
    }

    private String getWordFormatted(String pattern)
    {
        String s = getWord(pattern);
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == word.charAt(i))
                str.append("\uD83D\uDFE9");
            else if (word.contains(s.charAt(i) + ""))
                str.append("\uD83D\uDFE8");
            else
                str.append("⬜");
        }
        return str + " " + s;
    }

    private String getWord(String pattern)
    {
        Boolean[] isCorrect = IntStream.range(0, 5).mapToObj(i -> pattern.charAt(i) == 'x').toArray(Boolean[]::new);

        loop:
        for (String s : WORD_LIST)
        {
            char[] arr = s.toCharArray();
            if (isGreen)
            {
                for (int i = 0; i < arr.length; i++)
                {
                    if (isCorrect[i] && word.toCharArray()[i] != arr[i]) continue loop;
                    if (!isCorrect[i] && word.contains(String.valueOf(arr[i]))) continue loop;
                }
            }
            else
            {
                for (int i = 0; i < arr.length; i++)
                {
                    if (isCorrect[i] && !word.contains(String.valueOf(arr[i]))) continue loop;
                    if (!isCorrect[i] && word.contains(String.valueOf(arr[i]))) continue loop;
                }
            }

            Map<Character, Integer> arrFrequency = getFrequency(s);
            for (Character c : arrFrequency.keySet())
                if (arrFrequency.get(c) > 1 && !Objects.equals(WORD_FREQUENCY.get(c), arrFrequency.get(c))) continue loop;

            return s;
        }

        if (isGreen)
        {
            isGreen = false;
            return getWord(pattern);
        }
        return "";
    }

    private static Map<Character, Integer> getFrequency(String word)
    {
        return word.chars().mapToObj(c -> (char) c).collect(Collectors.toMap(c -> c, c -> 1,Integer::sum));
    }
}
