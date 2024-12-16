package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User updateUser);

    Collection<User> getUsers();

    User getUserById(long id);

    void deleteUser(long id);
}
