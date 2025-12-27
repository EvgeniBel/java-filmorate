package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private static final String USER_BY_ID = "/{id}";
    private static final String USER_FRIENDS_LIST = "/{id}/friends";
    public static final String USER_FRIENDS = "/{id}/friends/{friendId}";
    public static final String USER_COMMON_FRIENDS = "/{id}/friends/common/{otherId}";
    public static final String CONFIRM_FRIEND = "/{id}/friends/{friendId}/confirm";
    public static final String FRIEND_REQUESTS = "/{id}/friends/requests";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping(USER_BY_ID)
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping(USER_FRIENDS_LIST)
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping(FRIEND_REQUESTS)
    public List<User> getFriendRequests(@PathVariable Long id) {
        return userService.getFriendRequests(id);
    }

    @GetMapping(USER_COMMON_FRIENDS)
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
    }

    @PutMapping(USER_FRIENDS)
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping(USER_BY_ID)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @DeleteMapping(USER_FRIENDS)
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @PutMapping(CONFIRM_FRIEND)
    public void confirmFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.confirmFriend(id, friendId);
    }
}
