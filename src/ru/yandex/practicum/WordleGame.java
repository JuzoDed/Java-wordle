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

    public List<String> allAttempts = new ArrayList<>();         // Все введённые слова
    public List<char[]> attemptStatuses = new ArrayList<>();     // Статусы подсказок для каждого слова ("^+- -+")

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
                // suggestWord() теперь ГАРАНТИРОВАННО возвращает слово (если словарь непустой)
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

    /**
     * Генерация подсказок в стиле Wordle.
     * Возвращает массив символов длины 5, содержащий '^', '+', '-'.
     */
    public char[] tips(String word) {
        char[] result = new char[5];

        char[] game = answer.toCharArray();
        char[] player = word.toCharArray();

        boolean[] used = new boolean[5];

        // 1. '^' — точные совпадения
        for (int i = 0; i < 5; i++) {
            if (player[i] == game[i]) {
                result[i] = '^';
                used[i] = true;
            }
        }

        // 2. '+' — правильная буква, но не на месте; '-' — иначе
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


    /**
     * Автоматическое предложение следующего слова.
     * Гарантированно возвращает слово (если в словаре есть 5-буквенные слова).
     */
    public String suggestWord() {

        List<String> words = dictionary.getAllWords();
        List<String> possible = new ArrayList<>();

        Map<Integer, Character> exact = new HashMap<>();
        Map<Character, Set<Integer>> wrongPos = new HashMap<>();
        Set<Character> mustHave = new HashSet<>();
        Set<Character> forbiddenLetters = new HashSet<>();

        extractRules(exact, wrongPos, forbiddenLetters, mustHave);

        // 1. Жёсткая фильтрация (как в Wordle)
        for (String word : words) {

            if (word == null) continue;
            word = word.trim();
            if (word.length() != 5) continue;
            if (allAttempts.contains(word)) continue;

            boolean ok = true;

            // точные позиции
            for (var e : exact.entrySet()) {
                if (word.charAt(e.getKey()) != e.getValue()) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // обязательные буквы
            for (char c : mustHave) {
                if (word.indexOf(c) == -1) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // буквы, которых нет
            for (char c : word.toCharArray()) {
                if (forbiddenLetters.contains(c)) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // буквы, которые не должны быть на определённых позициях
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

        // 2. Ослабляем критерии — оставляем только '^' (точные позиции)
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

        // 3. Возвращаем ЛЮБОЕ неиспользованное слово длиной 5
        for (String word : words) {
            if (word == null) continue;
            word = word.trim();
            if (word.length() == 5 && !allAttempts.contains(word)) {
                return word;
            }
        }

        // 4. Если ВСЕ слова использованы (крайне редко) — возвращаем любое 5-буквенное
        for (String word : words) {
            if (word == null) continue;
            word = word.trim();
            if (word.length() == 5) {
                return word;
            }
        }

        // Защитный возврат (если словарь пуст или неверно настроен)
        return "слово";
    }

    /**
     * Извлекает правила из всех предыдущих попыток.
     * Делается в два прохода: сначала собираются mustHave, exact, wrongPos,
     * затем на втором проходе помечаются forbiddenLetters для '-' только если буква нигде не была '+' или '^'.
     */
    private void extractRules(
            Map<Integer, Character> exact,
            Map<Character, Set<Integer>> wrongPos,
            Set<Character> forbiddenLetters,
            Set<Character> mustHave
    ) {
        // Первый проход: собираем exact и mustHave и wrongPos (из ^ и +)
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
                // '-' будем обрабатывать во втором проходе
            }
        }

        // Второй проход: помечаем запрещённые буквы ('-') только если они нигде не были '+' или '^'
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
