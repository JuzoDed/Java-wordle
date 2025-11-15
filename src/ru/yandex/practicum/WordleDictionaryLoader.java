package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    public WordleDictionary getWordLib(String path) throws IOException {
        List<String> list = new ArrayList<>();

        try {
            FileReader reader = new FileReader(path, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);
            while (br.ready()) {
                list.add(br.readLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл словаря не найдет");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return new WordleDictionary(list);
    }
}
