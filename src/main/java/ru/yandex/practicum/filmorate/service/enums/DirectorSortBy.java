package ru.yandex.practicum.filmorate.service.enums;

import jakarta.validation.ValidationException;

public enum DirectorSortBy {
    YEAR, LIKES;

    public static DirectorSortBy from(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Параметр сортировки не может быть пустым");
        }
        try {
            return DirectorSortBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректный параметр сортировки: " + value);
        }
    }
}