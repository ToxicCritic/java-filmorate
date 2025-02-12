package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Repository
public class ReviewRepository extends BaseRepository<Review> implements ReviewStorage {

    public ReviewRepository(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    // Создание нового отзыва
    @Override
    public Review createReview(Review review) {
        int id = insert("""
                INSERT INTO REVIEWS (USER_ID, FILM_ID, CONTENT, IS_POSITIVE, USEFUL)
                VALUES (?, ?, ?, ?, ?)
                """,
                review.getUserId(),
                review.getFilmId(),
                review.getContent(),
                review.getIsPositive(),
                0  // Рейтинг изначально 0
        );
        review.setReviewId(id);
        return review;
    }


    // Обновление отзыва (изменяются только поля CONTENT и IS_POSITIVE)
    @Override
    public Review updateReview(Review review) {
        update("""
                UPDATE REVIEWS
                SET CONTENT = ?, IS_POSITIVE = ?
                WHERE REVIEW_ID = ?
                """,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return getReviewById(review.getReviewId());
    }

    // Получение отзыва по ID
    @Override
    public Review getReviewById(int reviewId) {
        return findOne("""
                SELECT REVIEW_ID, USER_ID, FILM_ID, CONTENT, IS_POSITIVE, USEFUL
                FROM REVIEWS
                WHERE REVIEW_ID = ?
                """, reviewId).get();
    }

    /**
     * Получение списка отзывов.
     * Если filmId равен null, выбираются отзывы для всех фильмов,
     * иначе — только для указанного фильма.
     */
    @Override
    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        if (filmId == null) {
            return findMany("""
                    SELECT REVIEW_ID, USER_ID, FILM_ID, CONTENT, IS_POSITIVE, USEFUL
                    FROM REVIEWS
                    ORDER BY USEFUL DESC
                    LIMIT ?
                    """, count);
        } else {
            return findMany("""
                    SELECT REVIEW_ID, USER_ID, FILM_ID, CONTENT, IS_POSITIVE, USEFUL
                    FROM REVIEWS
                    WHERE FILM_ID = ?
                    ORDER BY USEFUL DESC
                    LIMIT ?
                    """, filmId, count);
        }
    }

    // Удаление отзыва по ID
    @Override
    public void deleteReview(int reviewId) {
        delete("DELETE FROM REVIEWS WHERE REVIEW_ID = ?", reviewId);
    }

    // Методы для работы с лайками и дизлайками

    @Override
    public void addReviewReaction(int reviewId, int userId, boolean isLike) {
        update("""
                INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, IS_LIKE)
                VALUES (?, ?, ?)
                """, reviewId, userId, isLike);
        if (isLike) {
            update("UPDATE REVIEWS SET USEFUL = USEFUL + 1 WHERE REVIEW_ID = ?", reviewId);
        } else {
            update("UPDATE REVIEWS SET USEFUL = USEFUL - 1 WHERE REVIEW_ID = ?", reviewId);
        }
    }

    @Override
    public void removeReviewReaction(int reviewId, int userId) {
        Boolean isLike;
        try {
            isLike = jdbc.queryForObject(
                    "SELECT IS_LIKE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?",
                    Boolean.class, reviewId, userId);
        } catch (Exception e) {
            return;
        }
        delete("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?", reviewId, userId);
        if (Boolean.TRUE.equals(isLike)) {
            update("UPDATE REVIEWS SET USEFUL = USEFUL - 1 WHERE REVIEW_ID = ?", reviewId);
        } else {
            update("UPDATE REVIEWS SET USEFUL = USEFUL + 1 WHERE REVIEW_ID = ?", reviewId);
        }
    }
}