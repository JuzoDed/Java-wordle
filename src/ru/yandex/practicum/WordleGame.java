package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class WordleGame {

    public String answer;
    private int steps;
    private WordleDictionary dictionary;
    private FileWriter fileWriter;

    public List<String> allAttempts = new ArrayList<>();
    public List<char[]> attemptStatuses = new ArrayList<>();

    public WordleGame(WordleDictionary dictionary, Path logPath) {
        this.dictionary = dictionary;
        this.steps = 5;

        try {
            this.fileWriter = new FileWriter(logPath.toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Не удалось открыть лог для записи: " + e.getMessage());
        }
    }

    public void getNewWord() throws IOException {
        answer = dictionary.getNewWord();
        allAttempts.clear();
        attemptStatuses.clear();
        writeToLog("-------------------------------------");
        writeToLog("Слово игры: " + answer);
        System.out.println("Загадано новое слово");
    }

    public void game() throws IOException {
        Scanner scanner = new Scanner(System.in);

        for (int i = 1; i <= steps; i++) {

            System.out.println("Введите предполагаемое слово (или Enter для подсказки):");
            String playerWord = scanner.nextLine().trim();

            if (playerWord.isEmpty()) {
                String suggestion = suggestWord();
                System.out.println("Автоввод: " + suggestion);
                playerWord = suggestion;
            }

            if (playerWord.length() != 5) {
                System.out.println("Слово должно быть ровно 5 букв");
                i--;
                continue;
            }

            if (!dictionary.wordChek(playerWord)) {
                i--;
                continue;
            }

            writeToLog("Попытка " + i + ": " + playerWord);

            if (playerWord.equals(answer)) {
                System.out.println("Поздравляю! Слово " + answer + " угадано!");
                writeToLog("Угадано с попытки " + i);
                writeToLog("-------------------------------------\n");
                return;
            }

            allAttempts.add(playerWord);
            char[] tip = tips(playerWord);
            attemptStatuses.add(tip);
        }

        System.out.println("Попытки закончились. Правильное слово: " + answer);
        writeToLog("Не угадал слово: " + answer);
        writeToLog("-------------------------------------\n");
    }

    public void setSteps(int steps) {
        this.steps = steps;
        writeToLog("Установлено новое количество попыток: " + steps);
    }

    public char[] tips(String word) {
        char[] result = new char[5];

        char[] game = answer.toCharArray();
        char[] player = word.toCharArray();

        boolean[] used = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (player[i] == game[i]) {
                result[i] = '^';
                used[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == '^') continue;

            boolean found = false;

            for (int j = 0; j < 5; j++) {
                if (!used[j] && player[i] == game[j]) {
                    found = true;
                    used[j] = true;
                    break;
                }
            }

            result[i] = found ? '+' : '-';
        }

        System.out.println(new String(result));
        writeToLog("Подсказка для '" + word + "': " + new String(result));

        return result;
    }

    public String suggestWord() {

        List<String> words = dictionary.getAllWords();
        List<String> possible = new ArrayList<>();

        Map<Integer, Character> exact = new HashMap<>();
        Map<Character, Set<Integer>> wrongPos = new HashMap<>();
        Set<Character> mustHave = new HashSet<>();
        Set<Character> forbiddenLetters = new HashSet<>();

        extractRules(exact, wrongPos, forbiddenLetters, mustHave);

        for (String word : words) {

            if (word == null) continue;
            word = word.trim();
            if (word.length() != 5) continue;
            if (allAttempts.contains(word)) continue;

            boolean ok = true;

            for (var e : exact.entrySet()) {
                if (word.charAt(e.getKey()) != e.getValue()) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            for (char c : mustHave) {
                if (word.indexOf(c) == -1) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            for (char c : word.toCharArray()) {
                if (forbiddenLetters.contains(c)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            for (var e : wrongPos.entrySet()) {
                char letter = e.getKey();
                for (int pos : e.getValue()) {
                    if (word.charAt(pos) == letter) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) break;
            }

            if (ok) possible.add(word);
        }

        if (!possible.isEmpty()) {
            return possible.get(new Random().nextInt(possible.size()));
        }

        List<String> fallback = new ArrayList<>();

        for (String word : words) {
            if (word == null) continue;
            word = word.trim();
            if (word.length() != 5) continue;
            if (allAttempts.contains(word)) continue;

            boolean ok = true;

            for (var e : exact.entrySet()) {
                if (word.charAt(e.getKey()) != e.getValue()) {
                    ok = false;
                    break;
                }
            }

            if (ok) fallback.add(word);
        }

        if (!fallback.isEmpty()) {
            return fallback.get(new Random().nextInt(fallback.size()));
        }

        for (String word : words) {
            if (word == null) continue;
            word = word.trim();
            if (word.length() == 5 && !allAttempts.contains(word)) {
                return word;
            }
        }

        for (String word : words) {
            if (word == null) continue;
            word = word.trim();
            if (word.length() == 5) {
                return word;
            }
        }

        return "слово";
    }

    private void extractRules(
            Map<Integer, Character> exact,
            Map<Character, Set<Integer>> wrongPos,
            Set<Character> forbiddenLetters,
            Set<Character> mustHave
    ) {
        for (int a = 0; a < allAttempts.size(); a++) {
            String attempt = allAttempts.get(a);
            char[] status = attemptStatuses.get(a);

            for (int i = 0; i < 5; i++) {
                char ch = attempt.charAt(i);
                char st = status[i];

                if (st == '^') {
                    exact.put(i, ch);
                    mustHave.add(ch);
                } else if (st == '+') {
                    mustHave.add(ch);
                    wrongPos.computeIfAbsent(ch, k -> new HashSet<>()).add(i);
                }

            }
        }

        for (int a = 0; a < allAttempts.size(); a++) {
            String attempt = allAttempts.get(a);
            char[] status = attemptStatuses.get(a);

            for (int i = 0; i < 5; i++) {
                char ch = attempt.charAt(i);
                char st = status[i];

                if (st == '-') {
                    if (!mustHave.contains(ch)) {
                        forbiddenLetters.add(ch);
                    }
                }
            }
        }
    }

    private void writeToLog(String message) {
        if (fileWriter != null) {
            try {
                fileWriter.write(message + "\n");
                fileWriter.flush();
            } catch (IOException e) {
                System.out.println("Ошибка записи в лог: " + e.getMessage());
            }
        }
    }
}
