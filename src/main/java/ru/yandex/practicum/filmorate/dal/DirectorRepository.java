package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Set<Director> getDirectors() {
        return streamQuery("""
                SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS
                """);
    }

    @Override
    public Director getDirectorById(int id) {
        return findOne("""
                SELECT
                DIRECTOR_ID,
                DIRECTOR_NAME
                FROM DIRECTORS WHERE DIRECTOR_ID = ?
                """, id).get();
    }

    @Override
    public Director createDirector(Director director) {
        int id = insert("""
                INSERT INTO DIRECTORS(DIRECTOR_NAME) VALUES (?)
                """, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update("""
                    UPDATE DIRECTORS SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?
                """, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        update("""
                DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?
                """, id);
    }

    @Override
    public void createDirectorsForFilmById(int filmId, List<Director> directors) {
        batchUpdateBase("""
                        INSERT INTO DIRECTORS_SAVE(FILM_ID, DIRECTOR_ID)
                        VALUES (?, ?)
                        """,
                new BatchPreparedStatementSetter() {
                    @SuppressWarnings("NullableProblems")
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                }
        );
    }

    @Override
    public Set<Director> getDirectorsFilmById(int filmId) {
        return findMany("""
                SELECT DIRECTOR_ID, DIRECTOR_NAME FROM DIRECTORS WHERE DIRECTOR_ID IN(SELECT DIRECTOR_ID FROM DIRECTORS_SAVE WHERE FILM_ID = ?)
                """, filmId)
                .stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteDirectorsFilmById(int filmId) {
        delete("""
                DELETE FROM DIRECTORS_SAVE WHERE FILM_ID = ?
                """, filmId);
    }

    @Override
    public Map<Integer, Set<Director>> getDirectorsForFilmIds(Set<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(
                "SELECT FD.FILM_ID, D.DIRECTOR_ID, D.DIRECTOR_NAME " +
                "FROM DIRECTORS_SAVE FD " +
                "JOIN DIRECTORS D ON FD.DIRECTOR_ID = D.DIRECTOR_ID " +
                "WHERE FD.FILM_ID IN (%s)", placeholders);
        Object[] params = filmIds.toArray();
        return jdbc.query(sql, rs -> {
            Map<Integer, Set<Director>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("FILM_ID");
                Director director = new Director();
                director.setId(rs.getInt("DIRECTOR_ID"));
                director.setName(rs.getString("DIRECTOR_NAME"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return map;
        }, params);
    }
}
