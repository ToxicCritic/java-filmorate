package ru.yandex.practicum.filmorate.service.enums;

import jakarta.validation.ValidationException;

public enum FilmSearchBy {
    TITLE, DIRECTOR;

    public static FilmSearchBy from(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Параметр поиска не может быть пустым");
        }
        try {
            return FilmSearchBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректное значение параметра поиска: " + value);
        }
    }
}