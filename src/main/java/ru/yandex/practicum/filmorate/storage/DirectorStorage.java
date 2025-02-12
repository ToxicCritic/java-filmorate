package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

public interface DirectorStorage {

    Set<Director> getDirectors();

    Director getDirectorById(int id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirectorById(int id);

    void createDirectorsForFilmById(int filmId, List<Director> directors);

    Set<Director> getDirectorsFilmById(int filmId);

    void deleteDirectorsFilmById(int filmId);

    Map<Integer, Set<Director>> getDirectorsForFilmIds(Set<Integer> filmIds);
}