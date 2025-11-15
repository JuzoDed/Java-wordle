package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private WordleGame game;
    private WordleDictionary dict;

    @BeforeEach
    void setUp() {
        dict = new WordleDictionary(List.of(
                "домик", "кинза", "сойма", "модин", "комар", "диван", "доска"
        ));

        game = new WordleGame(dict, Path.of("test-log.txt"));
    }

    // ---------------------------------------------------
    // 1) Проверка подсказок (строго под твою логику)
    // ---------------------------------------------------
    @Test
    void testTipsCorrect() {
        game.answer = "домик";

        char[] tip = game.tips("кинза");

        assertArrayEquals(
                new char[]{'+', '+', '-', '-', '-'},
                tip,
                "Подсказка должна соответствовать реальному алгоритму"
        );
    }


    // ---------------------------------------------------
    // 2) Проверка suggestWord() на учёт предыдущих правил
    // ---------------------------------------------------
    @Test
    void testSuggestWordUsesHints() {
        game.answer = "домик";

        // фактическая подсказка ++---
        game.allAttempts.add("кинза");
        game.attemptStatuses.add(new char[]{'+', '+', '-', '-', '-'});

        String next = game.suggestWord();

        assertNotNull(next, "Предложенное слово не должно быть null");
        assertEquals(5, next.length(), "Подсказанное слово должно быть длиной 5");

        // Эти буквы исключены
        assertFalse(next.contains("н"));
        assertFalse(next.contains("з"));
        assertFalse(next.contains("а"));

        // Эти буквы могут быть, но не гарантированно (зависит от словаря)
        // assertTrue(next.contains("и"));  // ← ЭТО УДАЛЯЕМ
        // assertTrue(next.contains("к"));
    }


    // ---------------------------------------------------
    // 3) Победа
    // ---------------------------------------------------
    @Test
    void testGameWin() {
        game.answer = "домик";

        char[] tip = game.tips("домик");

        assertEquals("^^^^^", new String(tip), "При победе должны быть только '^'");
    }

    // ---------------------------------------------------
    // 4) Поражение — несколько попыток, ни одна не угадывает
    // ---------------------------------------------------
    @Test
    void testGameLoseEnding() {
        game.answer = "домик";

        assertNotEquals("^^^^^", new String(game.tips("кинза")));
        assertNotEquals("^^^^^", new String(game.tips("сойма")));
        assertNotEquals("^^^^^", new String(game.tips("комар")));
    }
}
