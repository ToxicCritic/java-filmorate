package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmEnricher {
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final RatingStorage ratingStorage;

    public void enrichFilms(Set<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        // Собираем id фильмов для которых нужно обогатить данные
        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Получаем мапы с жанрами и режиссёрами для заданных фильмов
        Map<Integer, Set<Genre>> genresMap = genreStorage.getGenresForFilmIds(filmIds);
        Map<Integer, Set<Director>> directorsMap = directorStorage.getDirectorsForFilmIds(filmIds);

        for (Film film : films) {
            // Обновляем информацию о рейтинге
            film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));

            // Извлекаем жанры для фильма, сортируем по id по возрастанию и сохраняем в LinkedHashSet для сохранения порядка
            Set<Genre> unsortedGenres = genresMap.getOrDefault(film.getId(), Collections.emptySet());
            Set<Genre> sortedGenres = unsortedGenres.stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(sortedGenres);

            // Режиссёров можно оставить без сортировки или, при необходимости, добавить аналогичную логику
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Collections.emptySet()));
        }
    }

    public void enrichFilms(List<Film> films) {
        enrichFilms(new LinkedHashSet<>(films));
    }
}