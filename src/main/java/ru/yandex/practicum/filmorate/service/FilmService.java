package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.status.EventOperation;
import ru.yandex.practicum.filmorate.dal.status.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.enums.DirectorSortBy;
import ru.yandex.practicum.filmorate.service.enums.FilmSearchBy;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final LikesStorage likesStorage;
    private final DirectorStorage directorStorage;
    private final RatingStorage ratingStorage;
    private final EventStorage eventStorage;
    private final FilmEnricher filmEnricher;

    public Film createFilm(Film film) {
        // Проверяем существование рейтинга МПА
        if (ratingStorage.getRatingById(film.getMpa().getId()) == null) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        // Проверяем существование жанров и режиссёров (если заданы)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(g -> genreStorage.getGenreById(g.getId()));
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(d -> directorStorage.getDirectorById(d.getId()));
        }
        // Создаем фильм (сохраняются базовые данные)
        filmStorage.createFilm(film);
        // Сохраняем привязки к жанрам и режиссёрам
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(film.getId(), new ArrayList<>(film.getGenres()));
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(film.getId(), new ArrayList<>(film.getDirectors()));
        }
        Film createdFilm = filmStorage.getFilmById(film.getId());
        filmEnricher.enrichFilms(Collections.singletonList(createdFilm));
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        // Проверка существования фильма
        filmStorage.getFilmById(film.getId());
        // Проверяем существование рейтинга МПА
        if (ratingStorage.getRatingById(film.getMpa().getId()) == null) {
            throw new NotFoundException("Рейтинг МПА с ID " + film.getMpa().getId() + " не найден");
        }
        // Обновляем привязки к жанрам: удаляем старые и создаём новые (если заданы)
        genreStorage.deleteGenreForFilmById(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.createGenresForFilmById(film.getId(), new ArrayList<>(film.getGenres()));
        } else {
            film.setGenres(Collections.emptySet());
        }
        // Аналогично для режиссёров
        directorStorage.deleteDirectorsFilmById(film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.createDirectorsForFilmById(film.getId(), new ArrayList<>(film.getDirectors()));
        } else {
            film.setDirectors(Collections.emptySet());
        }
        filmStorage.updateFilm(film);
        Film updatedFilm = filmStorage.getFilmById(film.getId());
        filmEnricher.enrichFilms(Collections.singletonList(updatedFilm));
        return updatedFilm;
    }

    public Set<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        filmEnricher.enrichFilms(films);
        return films.stream()
                .sorted(Comparator.comparing(Film::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        filmEnricher.enrichFilms(Collections.singletonList(film));
        log.debug("Получен фильм: {}", film);
        return film;
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм с id: {}", id);
    }

    public Set<Film> getTopFilms(int count, Integer genreId, Integer year) {
        List<Film> films = filmStorage.getFilms();
        filmEnricher.enrichFilms(films);
        // Применяем фильтрацию по жанру
        if (genreId != null) {
            films = films.stream()
                    .filter(film -> film.getGenres() != null &&
                                    film.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)))
                    .collect(Collectors.toList());
        }
        // Применяем фильтрацию по году
        if (year != null) {
            films = films.stream()
                    .filter(film -> film.getReleaseDate() != null &&
                                    film.getReleaseDate().getYear() == year)
                    .collect(Collectors.toList());
        }
        // Сортируем по количеству лайков (по убыванию)
        films.sort((f1, f2) -> Integer.compare(
                likesStorage.getLikeCountForFilm(f2.getId()),
                likesStorage.getLikeCountForFilm(f1.getId())
        ));
        // Ограничиваем количество
        Set<Film> topFilms = films.stream()
                .limit(count)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // Обогащаем полученные фильмы
        log.debug("Получены топ-{} фильмов по лайкам с genreId={} и year={}: {}", count, genreId, year, topFilms);
        return topFilms;
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        Set<Integer> userLikedFilms = likesStorage.getLikedFilmsByUser(userId);
        Set<Integer> friendLikedFilms = likesStorage.getLikedFilmsByUser(friendId);
        userLikedFilms.retainAll(friendLikedFilms);
        if (userLikedFilms.isEmpty()) {
            log.info("Нет общих фильмов для пользователей {} и {}", userId, friendId);
            return Collections.emptyList();
        }
        List<Film> commonFilms = userLikedFilms.stream()
                .map(filmStorage::getFilmById)
                .collect(Collectors.toList());
        // Сортировка по количеству лайков (по убыванию)
        commonFilms.sort((f1, f2) -> Integer.compare(
                likesStorage.getLikeCountForFilm(f2.getId()),
                likesStorage.getLikeCountForFilm(f1.getId())
        ));
        filmEnricher.enrichFilms(commonFilms);
        log.debug("Получены общие фильмы для пользователей {} и {}: {}", userId, friendId, commonFilms);
        return commonFilms;
    }

    public List<Film> getFilmsByDirector(int directorId, String sortByStr) {
        directorStorage.getDirectorById(directorId);
        DirectorSortBy sortBy = DirectorSortBy.from(sortByStr);
        List<Film> films = filmStorage.getFilmsByDirector(directorId);
        List<Film> filmList = new ArrayList<>(films);
        switch (sortBy) {
            case YEAR:
                filmList.sort(Comparator.comparing(Film::getReleaseDate));
                break;
            case LIKES:
                filmList.sort((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ));
                break;
            default:
                throw new ValidationException("Некорректный параметр сортировки: " + sortByStr);
        }
        filmEnricher.enrichFilms(filmList);
        log.debug("Получены фильмы режиссёра {} с сортировкой '{}': {}", directorId, sortBy, filmList);
        return filmList;
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Строка поиска не может быть пустой");
        }
        String searchParam = "%" + query.toLowerCase() + "%";
        Set<Film> result = new HashSet<>();
        String[] criteria = by.split(",");
        for (String criterion : criteria) {
            FilmSearchBy searchBy = FilmSearchBy.from(criterion.trim());
            switch (searchBy) {
                case TITLE:
                    result.addAll(filmStorage.getFilmsByTitle(searchParam));
                    break;
                case DIRECTOR:
                    result.addAll(filmStorage.getFilmsByDirectorName(searchParam));
                    break;
                default:
                    throw new ValidationException("Некорректное значение параметра 'by': " + criterion);
            }
        }
        List<Film> sortedFilms = result.stream()
                .sorted((f1, f2) -> Integer.compare(
                        likesStorage.getLikeCountForFilm(f2.getId()),
                        likesStorage.getLikeCountForFilm(f1.getId())
                ))
                .collect(Collectors.toList());
        filmEnricher.enrichFilms(sortedFilms);
        log.debug("Результаты поиска для query='{}', by='{}': {}", query, by, sortedFilms);
        return sortedFilms;
    }

    public Film likeFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        // Добавляем событие лайка до попытки добавить сам лайк
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        Set<Integer> likedFilms = likesStorage.getLikedFilmsByUser(userId);
        if (likedFilms.contains(filmId)) {
            log.info("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return film;
        }
        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        Film updatedFilm = filmStorage.getFilmById(filmId);
        filmEnricher.enrichFilms(Collections.singletonList(updatedFilm));
        return updatedFilm;
    }

    public void delLikeFilm(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likesStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }
}