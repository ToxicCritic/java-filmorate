package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreRepository extends BaseRepository<Genre> implements GenreStorage {


    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> getGenres() {
        return findMany("""
                SELECT * FROM GENRES
                """);
    }

    @Override
    public Genre getGenreById(int id) {
        return findOne("""
                SELECT
                GENRE_ID,
                GENRE_NAME
                FROM GENRES WHERE GENRE_ID = ?
                """, id).get();
    }

    @Override
    public void createGenresForFilmById(int filmId, List<Genre> genresId) {
        batchUpdateBase("""
                        INSERT INTO GENRES_SAVE(FILM_ID, GENRE_ID)
                        VALUES (?, ?)
                        """,
                new BatchPreparedStatementSetter() {
                    @SuppressWarnings("NullableProblems")
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, genresId.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genresId.size();
                    }
                }
        );
    }

    @Override
    public void deleteGenreForFilmById(int id) {
        delete("DELETE FROM GENRES_SAVE WHERE FILM_ID = ?", id);
    }

    @Override
    public Set<Genre> getGenresFilmById(int id) {
        return findMany("""
                SELECT GENRE_ID, GENRE_NAME FROM GENRES WHERE GENRE_ID IN(SELECT GENRE_ID FROM GENRES_SAVE WHERE FILM_ID = ?)
                """, id)
                .stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
