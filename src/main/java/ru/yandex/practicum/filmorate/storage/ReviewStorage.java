package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReviewById(int reviewId);

    List<Review> getReviewsByFilmId(Integer filmId, int count);

    void deleteReview(int reviewId);

    void addReviewReaction(int reviewId, int userId, boolean isLike);

    void removeReviewReaction(int reviewId, int userId);
}