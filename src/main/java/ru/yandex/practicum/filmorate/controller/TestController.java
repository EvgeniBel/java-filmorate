package ru.yandex.practicum.filmorate.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RestController
@RequestMapping("/test")
@ConditionalOnProperty(name = "test.endpoints.enabled", havingValue = "true")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/reset")
    public String resetDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Отключаем проверку внешних ключей
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // Очищаем таблицы
            String[] tables = {"likes", "film_genres", "friends", "users", "films"};
            for (String table : tables) {
                try {
                    stmt.execute("DELETE FROM " + table);
                } catch (Exception e) {
                    System.err.println("Error clearing table " + table + ": " + e.getMessage());
                }
            }

            // Очищаем справочные таблицы и заполняем заново
            try {
                stmt.execute("DELETE FROM genres");
                stmt.execute("DELETE FROM mpa_ratings");

                // Вставляем данные справочных таблиц
                stmt.execute("INSERT INTO mpa_ratings (id, code, description) VALUES " +
                        "(1, 'G', 'Нет возрастных ограничений'), " +
                        "(2, 'PG', 'Детям рекомендуется смотреть с родителями'), " +
                        "(3, 'PG-13', 'Детям до 13 лет просмотр не желателен'), " +
                        "(4, 'R', 'Лицам до 17 лет просмотр только в присутствии взрослого'), " +
                        "(5, 'NC-17', 'Лицам до 18 лет просмотр запрещён')");

                stmt.execute("INSERT INTO genres (id, name) VALUES " +
                        "(1, 'Комедия'), " +
                        "(2, 'Драма'), " +
                        "(3, 'Мультфильм'), " +
                        "(4, 'Триллер'), " +
                        "(5, 'Документальный'), " +
                        "(6, 'Боевик')");
            } catch (Exception e) {
                System.err.println("Error resetting reference tables: " + e.getMessage());
            }

            // Включаем проверку внешних ключей
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            return "Database cleared and reset";
        } catch (Exception e) {
            return "Error clearing database: " + e.getMessage();
        }
    }
}