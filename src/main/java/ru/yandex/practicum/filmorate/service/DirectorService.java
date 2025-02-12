package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Set<Director> getDirectors() {
        return directorStorage.getDirectors().stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        directorStorage.getDirectorById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorStorage.getDirectorById(id);
        directorStorage.deleteDirectorById(id);
    }
}

