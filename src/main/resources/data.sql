SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE FRIENDS_LIST      RESTART IDENTITY;
TRUNCATE TABLE FILMS_GENRE  RESTART IDENTITY;
TRUNCATE TABLE REVIEW_LIKES RESTART IDENTITY;
TRUNCATE TABLE REVIEWS RESTART IDENTITY;
TRUNCATE TABLE GENRE RESTART IDENTITY;
TRUNCATE TABLE EVENTS RESTART IDENTITY;
TRUNCATE TABLE LIKE_LIST RESTART IDENTITY;
TRUNCATE TABLE FILMS RESTART IDENTITY;
TRUNCATE TABLE RATING RESTART IDENTITY;
TRUNCATE TABLE DIRECTORS RESTART IDENTITY;
TRUNCATE TABLE FILM_DIRECTORS RESTART IDENTITY;
TRUNCATE TABLE USERS RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO GENRE (GENRE_NAME)
VALUES ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');

INSERT INTO RATING (RATING_NAME)
VALUES ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');