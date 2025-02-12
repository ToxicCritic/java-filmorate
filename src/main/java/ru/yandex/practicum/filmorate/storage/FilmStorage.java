package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film createFilm(Film film);

    Film updateFilm(Film filmUpdated);

    List<Film> getFilms();

    Film getFilmById(int id);

    void deleteFilm(int id);

    List<Film> getFilmsByDirector(int directorId);

    List<Film> getFilmsByTitle(String searchQuery);

    List<Film> getFilmsByDirectorName(String searchQuery);
}
