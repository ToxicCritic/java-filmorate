package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        long filmId = nextId();
        film.setId(filmId);
        if (film.getName() == null || film.getDuration() == null || film.getDescription() == null
                || film.getReleaseDate() == null) {
            log.error("фильм без имени и др параметров");
            throw new ValidationException("У фильма должно быть имя, длительность, описание и дата релиза.");
        }
        if (film.getDescription().length() > 200) {
            log.error("описание > 200");
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("дата релиза позже праздника");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("отрицательная длина фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        films.put(filmId, film);
        log.info("новый фильм создан");
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film filmUpdated) {
        Film filmTemp = films.get(filmUpdated.getId());
        if (filmUpdated.getReleaseDate() == null) {
            filmUpdated.setReleaseDate(filmTemp.getReleaseDate());
        }
        if (filmUpdated.getName() == null) {
            filmUpdated.setName(filmTemp.getName());
        }
        if (filmUpdated.getDescription() == null) {
            filmUpdated.setDescription(filmTemp.getDescription());
        }
        if (filmUpdated.getDuration() == null) {
            filmUpdated.setDuration(filmTemp.getDuration());
        }
        if (filmUpdated.getDescription().length() > 200) {
            log.error("описание > 200");
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }
        if (filmUpdated.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("дата релиза позже праздника");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (filmUpdated.getDuration() <= 0) {
            log.error("отрицательная длина фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        if (!films.containsKey(filmUpdated.getId())) {
            throw new ValidationException("Фильма с таким айди нет.");
        }
        films.put(filmUpdated.getId(), filmUpdated);
        log.info("Фильм изменен");
        return filmUpdated;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    private long nextId() {
        long nowMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nowMaxId;
    }
}