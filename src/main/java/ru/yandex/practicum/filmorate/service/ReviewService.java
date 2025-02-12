package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    // Создание нового отзыва
    public Review createReview(Review review) {
        log.info("Попытка получить пользователя по id");
        userStorage.getUserById(review.getUserId());
        log.info("Попытка получить фильм по id");
        filmStorage.getFilmById(review.getFilmId());

        Review created = reviewStorage.createReview(review);
        log.info("Создан отзыв с id: {}", created.getReviewId());
        eventStorage.addEvent(review.getUserId(), EventType.REVIEW, EventOperation.ADD, review.getReviewId());
        return created;
    }

    // Обновление существующего отзыва
    public Review updateReview(Review review) {
        reviewStorage.getReviewById(review.getReviewId());
        Review updated = reviewStorage.updateReview(review);
        log.info("Отзыв с id {} обновлён", review.getReviewId());
        eventStorage.addEvent(updated.getUserId(), EventType.REVIEW, EventOperation.UPDATE, updated.getReviewId());
        return updated;
    }

    // Удаление отзыва по ID
    public void deleteReview(int reviewId) {
        Review reviewToDelete = getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        log.info("Отзыв с id {} удалён", reviewId);
        eventStorage.addEvent(reviewToDelete.getUserId(), EventType.REVIEW, EventOperation.REMOVE, reviewId);
    }

    // Получение отзыва по ID
    public Review getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId);
    }

    // Получение списка отзывов
    public List<Review> getReviews(Integer filmId, Integer count) {
        List<Review> reviews = reviewStorage.getReviewsByFilmId(filmId, count);
        log.info("Получено {} отзывов (filmId = {})", reviews.size(), filmId);
        return reviews;
    }

    // Методы для работы с лайками/дизлайками

    public void addLike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.addReviewReaction(reviewId, userId, true);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.addReviewReaction(reviewId, userId, false);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.removeReviewReaction(reviewId, userId);
        log.info("Пользователь {} удалил лайк у отзыва {}", userId, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        getReviewById(reviewId);
        userStorage.getUserById(userId);
        reviewStorage.removeReviewReaction(reviewId, userId);
        log.info("Пользователь {} удалил дизлайк у отзыва {}", userId, reviewId);
    }
}