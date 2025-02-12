package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // POST /reviews
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        log.info("Запрос на создание отзыва: {}", review);
        return reviewService.createReview(review);
    }

    // PUT /reviews
    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Запрос на обновление отзыва: {}", review);
        return reviewService.updateReview(review);
    }

    // DELETE /reviews/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("id") int reviewId) {
        log.info("Запрос на удаление отзыва с id: {}", reviewId);
        reviewService.deleteReview(reviewId);
    }

    // GET /reviews/{id}
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") int reviewId) {
        log.info("Запрос на получение отзыва с id: {}", reviewId);
        return reviewService.getReviewById(reviewId);
    }

    // GET /reviews?filmId={filmId}&count={count}
    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Запрос на получение отзывов. filmId = {}, count = {}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    // PUT /reviews/{id}/like/{userId}
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int reviewId,
                        @PathVariable("userId") int userId) {
        log.info("Пользователь {} ставит лайк отзыву {}", userId, reviewId);
        reviewService.addLike(reviewId, userId);
    }

    // PUT /reviews/{id}/dislike/{userId}
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") int reviewId,
                           @PathVariable("userId") int userId) {
        log.info("Пользователь {} ставит дизлайк отзыву {}", userId, reviewId);
        reviewService.addDislike(reviewId, userId);
    }

    // DELETE /reviews/{id}/like/{userId}
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") int reviewId,
                           @PathVariable("userId") int userId) {
        log.info("Пользователь {} удаляет лайк у отзыва {}", userId, reviewId);
        reviewService.removeLike(reviewId, userId);
    }

    // DELETE /reviews/{id}/dislike/{userId}
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") int reviewId,
                              @PathVariable("userId") int userId) {
        log.info("Пользователь {} удаляет дизлайк у отзыва {}", userId, reviewId);
        reviewService.removeDislike(reviewId, userId);
    }
}