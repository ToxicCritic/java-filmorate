package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventStorage eventStorage;
    private final LikesStorage likesStorage;
    private final FilmStorage filmStorage;
    private final FilmEnricher filmEnricher;

    public User createUser(@Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(@Valid User updateUser) {
        getUserById(updateUser.getId());
        return userStorage.updateUser(updateUser);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public void deleteUser(int id) {
        eventStorage.deleteEventsByUserId(id);
        userStorage.deleteUser(id);
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
        eventStorage.addEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendshipStorage.deleteFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        eventStorage.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getFriends(int userId) {
        getUserById(userId);
        return friendshipStorage.getAllUserFriends(userId);
    }

    public List<User> getCommonFriend(int firstId, int secondId) {
        getUserById(firstId);
        getUserById(secondId);
        return friendshipStorage.getCommonFriends(firstId, secondId);
    }

    public List<Film> getRecommendations(int userId) {
        Set<Integer> userLikedFilms = likesStorage.getLikedFilmsByUser(userId);
        if (userLikedFilms.isEmpty()) {
            log.info("Пользователь с id {} не поставил ни одного лайка, рекомендации отсутствуют", userId);
            return Collections.emptyList();
        }

        // Находим пользователей с общими лайками (исключая самого пользователя)
        Map<Integer, Long> commonLikes = findUsersWithCommonLikes(userLikedFilms, userId);
        if (commonLikes.isEmpty()) {
            log.info("У пользователя с id {} нет общих лайков с другими пользователями", userId);
            return Collections.emptyList();
        }

        // Определяем пользователя с максимальным числом общих лайков
        Integer similarUserId = commonLikes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (similarUserId == null) {
            return Collections.emptyList();
        }

        Set<Integer> similarUserLikedFilms = likesStorage.getLikedFilmsByUser(similarUserId);
        // Исключаем те, которые уже лайкнул текущий пользователь
        similarUserLikedFilms.removeAll(userLikedFilms);
        if (similarUserLikedFilms.isEmpty()) {
            log.info("Нет фильмов для рекомендаций: пользователь {} уже лайкнул все фильмы, понравившиеся пользователю {}",
                    userId, similarUserId);
            return Collections.emptyList();
        }

        List<Film> recommendedFilms = similarUserLikedFilms.stream()
                .map(filmStorage::getFilmById)
                .toList();

        List<Film> sortedFilms = recommendedFilms.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toCollection(LinkedList::new));

        filmEnricher.enrichFilms(sortedFilms);
        log.debug("Рекомендации для пользователя {} на основе предпочтений пользователя {}: {}",
                userId, similarUserId, sortedFilms);
        return sortedFilms;
    }

    public Map<Integer, Long> findUsersWithCommonLikes(Set<Integer> filmIds, int userId) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Формируем строку плейсхолдеров, например, "?, ?, ?" для каждого filmId
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT USER_ID, COUNT(FILM_ID) AS COMMON_LIKES " +
                "FROM LIKE_LIST " +
                "WHERE FILM_ID IN (" + placeholders + ") AND USER_ID <> ? " +
                "GROUP BY USER_ID";
        Object[] params = new Object[filmIds.size() + 1];
        int i = 0;
        for (Integer filmId : filmIds) {
            params[i++] = filmId;
        }
        params[i] = userId; // исключаем самого пользователя

        Map<Integer, Long> commonLikesCount = likesStorage.getCommonLikes(sql, params);
        log.debug("Найдено пользователей с общими лайками: {}", commonLikesCount);
        return commonLikesCount;
    }

    public LinkedList<Event> getFeedUserById(int id) {
        LinkedList<Event> events = eventStorage.getFeedUserById(id).stream()
                .sorted(Comparator.comparing(Event::getTimestamp))
                .collect(Collectors.toCollection(LinkedList::new));
        if (events.isEmpty()) {
            throw new NotFoundException("Лента событий для пользователя с id " + id + " пуста.");
        }
        return events;
    }

}