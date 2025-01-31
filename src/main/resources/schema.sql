CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    EMAIL    VARCHAR                                              NOT NULL,
    LOGIN    VARCHAR UNIQUE                                       NOT NULL,
    NAME VARCHAR,
    BIRTHDAY DATE                                                 NOT NULL
);
CREATE TABLE IF NOT EXISTS DIRECTORS
(
    DIRECTOR_ID    INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    DIRECTOR_NAME  VARCHAR      NOT NULL
);
CREATE TABLE IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    GENRE_NAME VARCHAR                                          NOT NULL
);
CREATE TABLE IF NOT EXISTS RATING
(
    RATING_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    RATING_NAME VARCHAR                                              NOT NULL
);
CREATE TABLE IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    FILM_NAME    VARCHAR(255)                                     NOT NULL,
    DESCRIPTION  VARCHAR(200)                                     NOT NULL,
    RELEASE_DATE DATE                                             NOT NULL,
    DURATION     INTEGER                                          NOT NULL,
    RATING_ID       INTEGER                                          NOT NULL
        REFERENCES RATING (RATING_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS LIKE_LIST
(
    FILM_ID INTEGER REFERENCES FILMS (FILM_ID) ON DELETE CASCADE NOT NULL,
    USER_ID INTEGER REFERENCES USERS (USER_ID) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (FILM_ID, USER_ID)
);

CREATE TABLE IF NOT EXISTS FILMS_GENRE
(
    FILM_ID  INTEGER REFERENCES FILMS (FILM_ID) ON DELETE CASCADE  NOT NULL,
    GENRE_ID INTEGER REFERENCES GENRE (GENRE_ID) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (FILM_ID, GENRE_ID)
);
CREATE TABLE IF NOT EXISTS FILM_DIRECTORS
(
    FILM_ID  INTEGER REFERENCES FILMS (FILM_ID) ON DELETE CASCADE  NOT NULL,
    DIRECTOR_ID INTEGER REFERENCES DIRECTORS (DIRECTOR_ID) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (FILM_ID, DIRECTOR_ID)
);
CREATE TABLE IF NOT EXISTS FRIENDS_LIST
(
    USER_ID   INTEGER REFERENCES USERS (USER_ID) ON DELETE CASCADE NOT NULL,
    FRIEND_ID INTEGER REFERENCES USERS (USER_ID) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (USER_ID, FRIEND_ID)
);
CREATE TABLE IF NOT EXISTS REVIEWS (
    REVIEWS_ID  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    CONTENT     VARCHAR      NOT NULL,
    IS_POSITIVE BOOLEAN      NOT NULL,
    USER_ID     INTEGER      NOT NULL REFERENCES USERS(USER_ID) ON DELETE CASCADE,
    FILM_ID     INTEGER      NOT NULL REFERENCES FILMS(FILM_ID) ON DELETE CASCADE,
    USEFUL      INTEGER      NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS EVENTS (
    EVENT_ID         INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    EVENT_TIMESTAMP  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    USER_ID          INTEGER      NOT NULL REFERENCES USERS(USER_ID) ON DELETE CASCADE,
    EVENT_TYPE       VARCHAR      NOT NULL,
    OPERATION        VARCHAR      NOT NULL,
    ENTITY_ID        INTEGER      NOT NULL
);