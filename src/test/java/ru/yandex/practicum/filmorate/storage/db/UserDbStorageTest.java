package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.modelUser.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

    @JdbcTest
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @RequiredArgsConstructor(onConstructor_ = @Autowired)
    @Import(UserDbStorage.class) // Явно импортируем только UserDbStorage
    class UserDbStorageTest {

        private final UserDbStorage userStorage;
        private final JdbcTemplate jdbcTemplate;

        @BeforeEach
        void setUp() {
            jdbcTemplate.execute("DELETE FROM friends");
            jdbcTemplate.execute("DELETE FROM users");
        }

        @Test
        void testCreateUser() {
            User user = new User();
            user.setEmail("test@mail.ru");
            user.setLogin("testLogin");
            user.setName("Test User");
            user.setBirthday(LocalDate.of(1990, 1, 1));

            User createdUser = userStorage.create(user);

            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getId()).isNotNull();
            assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
        }

        @Test
        void testFindAllUsers() {
            // Создаем двух пользователей
            User user1 = new User();
            user1.setEmail("user1@mail.ru");
            user1.setLogin("user1");
            user1.setName("User One");
            user1.setBirthday(LocalDate.of(1990, 1, 1));
            userStorage.create(user1);

            User user2 = new User();
            user2.setEmail("user2@mail.ru");
            user2.setLogin("user2");
            user2.setName("User Two");
            user2.setBirthday(LocalDate.of(1991, 2, 2));
            userStorage.create(user2);

            List<User> users = userStorage.findAll();

            assertThat(users).hasSize(2);
        }
    }