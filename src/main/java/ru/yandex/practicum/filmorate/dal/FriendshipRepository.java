package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Repository
public class FriendshipRepository extends BaseRepository<User>  {
    private static final String ADD_FRIEND_QUERY = "INSERT INTO FRIENDS_LIST (USER_ID, FRIEND_ID) VALUES (?, ?)";
    private static final String DEL_FRIEND_QUERY = "DELETE FROM FRIENDS_LIST WHERE USER_ID = ? AND FRIEND_ID = ?";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT * FROM USERS WHERE USER_ID IN " +
            "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?) AND USER_ID IN " +
            "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)";
    private static final String GET_ALL_USER_FRIENDS = "SELECT * FROM USERS WHERE USER_ID IN " +
            "(SELECT FRIEND_ID FROM FRIENDS_LIST WHERE USER_ID = ?)";
    private static final String SQL_EVENT = "INSERT INTO EVENTS(USER_ID, EVENT_TYPE, OPERATION, ENTITY_ID) VALUES (?, ?, ?, ?)";

    public FriendshipRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public void addFriend(Integer userId, Integer friendId) {
        boolean updated = update(ADD_FRIEND_QUERY, userId, friendId);
        if (!updated) {
            throw new NotFoundException("Не удалось добавить друга: пользователь с id " + userId +
                    " или друг с id " + friendId + " не найден");
        }
        update(SQL_EVENT, userId, "FRIEND", "ADD", friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        boolean deleted = delete(DEL_FRIEND_QUERY, userId, friendId);
        if (!deleted) {
            throw new NotFoundException("Не удалось удалить друга: дружба между пользователями с id " +
                    userId + " и " + friendId + " не найдена");
        }
        update(SQL_EVENT, userId, "FRIEND", "REMOVE", friendId);
    }

    public Collection<User> getCommonFriends(Integer firstUser, Integer secondUser) {
        return findMany(GET_COMMON_FRIENDS_QUERY, firstUser, secondUser);
    }

    public Collection<User> getAllUserFriends(Integer userId) {
        return findMany(GET_ALL_USER_FRIENDS, userId);
    }
}