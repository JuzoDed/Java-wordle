package ru.yandex.practicum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Wordle {

    private static final String LOGFILENAME = "log.txt";
    private static WordleDictionary wordleDictionary;
    private static WordleDictionaryLoader wordleDictionaryLoader;
    private static WordleGame wordleGame;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        setUpGame();

        while (true) {
            printMenu();
            String command = scanner.nextLine();
            switch (command) {
                case "1":
                    wordleGame.getNewWord();
                    wordleGame.game();
                    break;
                case "2":
                    System.out.println("Введите новое количество попыток");
                    wordleGame.setSteps(scanner.nextInt());
                    scanner.nextLine();
                    break;
                case "3":
                    try {
                        System.out.println("Содержимое лог-файла:");
                        Files.lines(Paths.get(LOGFILENAME)).forEach(System.out::println);
                    } catch (IOException e) {
                        System.out.println("Ошибка чтения лог-файла: " + e.getMessage());
                    }
                    break;
                case "4":
                    System.out.println("Завершение работы");
                    System.exit(0);
                    break;
            }
        }
    }

    public static void createLogFile() throws IOException {
        Path logPath = Paths.get(LOGFILENAME);
        try {
            Files.deleteIfExists(logPath);
            Files.createFile(logPath);
            System.out.println("Лог-файл создан");
        } catch (IOException e) {
            System.out.println("Непредвиденная ошибка создания лог-файла");
        }
    }

    public static void setUpGame() throws IOException {
        wordleDictionaryLoader = new WordleDictionaryLoader();
        wordleDictionary = wordleDictionaryLoader.getWordLib("words_ru.txt");
        createLogFile();
        wordleGame = new WordleGame(wordleDictionary, Paths.get(LOGFILENAME));
    }

    public static void printMenu() {
        System.out.println("1 - Новая игра\n" +
                "2 - Изменить количество попыток\n" +
                "3 - Вывести лог-файл\n" +
                "4 - Завершить");
    }
}