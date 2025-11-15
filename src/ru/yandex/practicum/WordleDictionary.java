package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private static List<String> words;
    ArrayList<String> usedWords = new ArrayList<>();

    public WordleDictionary(List<String> list) {
        words = list;
    }

    public String getNewWord() {
        Random rand = new Random();
        String word;
        while (true) {
            int ranWord = rand.nextInt(1, words.size());
            word = words.get(ranWord);

            if (word.length() != 5) {
                continue;
            } else {
                word = word.toLowerCase().replace("ё", "е");
                break;
            }

        }
        return word;
    }

    public boolean wordChek(String word) {
        if (word.length() != 5) {
            System.out.println("Слово должно содержать ровно 5 букв");
            return false;
        }

        if (!words.contains(word)) {
            System.out.println("Извините, я не знаю такое слово!");
            return false;
        }

        // Правильная проверка на русские буквы
        if (!word.matches("[а-яё]+")) {
            System.out.println("Слово содержит не русские буквы");
            return false;
        }

        return true;
    }

    public List<String> getAllWords() {
        return new ArrayList<>(words);
    }

}
