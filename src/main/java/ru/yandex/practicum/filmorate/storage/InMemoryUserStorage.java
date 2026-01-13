package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friends = new HashMap<>();
    private final Map<Long, Map<Long, String>> friendshipStatuses = new HashMap<>(); // userId -> (friendId -> status)
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        friendshipStatuses.put(user.getId(), new HashMap<>());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        friends.remove(id);
        friendshipStatuses.remove(id);
        // Удаляем пользователя из списков друзей других пользователей
        friends.values().forEach(friendSet -> friendSet.remove(id));
        friendshipStatuses.values().forEach(statusMap -> statusMap.remove(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Односторонняя дружба: userId добавляет friendId в друзья со статусом PENDING
        friends.get(userId).add(friendId);
        friendshipStatuses.get(userId).put(friendId, "PENDING");

        // Для друга создаем запись о заявке (если её нет)
        if (!friendshipStatuses.containsKey(friendId)) {
            friendshipStatuses.put(friendId, new HashMap<>());
        }
        friendshipStatuses.get(friendId).put(userId, "PENDING");
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Удаляем только одностороннюю связь
        friends.get(userId).remove(friendId);
        friendshipStatuses.get(userId).remove(friendId);

        // У друга тоже удаляем информацию об этой заявке
        if (friendshipStatuses.containsKey(friendId)) {
            friendshipStatuses.get(friendId).remove(userId);
        }
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        // userId подтверждает заявку от friendId
        // Проверяем, что friendId отправил заявку userId
        if (friendshipStatuses.containsKey(userId) &&
                friendshipStatuses.get(userId).containsKey(friendId) &&
                "PENDING".equals(friendshipStatuses.get(userId).get(friendId))) {

            // Меняем статус на CONFIRMED у обоих
            friendshipStatuses.get(userId).put(friendId, "CONFIRMED");

            if (friendshipStatuses.containsKey(friendId)) {
                friendshipStatuses.get(friendId).put(userId, "CONFIRMED");
            }

            friends.get(userId).add(friendId);
        } else {
            throw new RuntimeException("Заявка в друзья не найдена или уже обработана");
        }
    }

    @Override
    public List<User> getFriendRequests(Long userId) {
        // Получаем список пользователей, которые отправили заявку данному пользователю
        if (!friendshipStatuses.containsKey(userId)) {
            return new ArrayList<>();
        }

        return friendshipStatuses.get(userId).entrySet().stream()
                .filter(entry -> "PENDING".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getFriends(Long userId) {
        // Получаем список подтвержденных друзей
        if (!friendshipStatuses.containsKey(userId)) {
            return new ArrayList<>();
        }

        return friendshipStatuses.get(userId).entrySet().stream()
                .filter(entry -> "CONFIRMED".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = getFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<Long> otherFriends = getFriends(otherId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
