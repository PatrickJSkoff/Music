# ------------------------------------------------
# SQL script to create the tables in the 
# Music Database (MusicDB)
# Created by Patrick Skoff for the Music app
# --

/*
Tables to be dropped must be listed in a logical order based on dependency.
UserPhoto, UserSong, UserArtist, and UserConcert depend on User. Therefore, they must be dropped before User.
*/
DROP TABLE IF EXISTS
    PlaylistSong,
    Playlist,
    UserArtistAlbum,
    UserConcert,
    UserArtist,
    UserSong,
    UserPhoto,
    User;

/* Attributes of interest of a user */
CREATE TABLE User
(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(255) NOT NULL,              /* To store Salted and Hashed Password Parts */
    first_name VARCHAR(32) NOT NULL,
    middle_name VARCHAR(32),
    last_name VARCHAR(32) NOT NULL,
    address1 VARCHAR(128) NOT NULL,
    address2 VARCHAR(128),
    city VARCHAR(64) NOT NULL,
    state_name VARCHAR(32) NOT NULL,
    zipcode VARCHAR(10) NOT NULL,                /* example: 24060-1804 */
    security_question VARCHAR(255) NOT NULL,
    security_answer VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    two_fa_status INT NOT NULL,
    cell_phone_number VARCHAR(24),
    cell_phone_carrier VARCHAR(32),
    PRIMARY KEY (id)
);

/* Uploaded profile photo that belongs to a user. Only one profile photo is allowed per user. */
CREATE TABLE UserPhoto
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    extension ENUM('jpeg', 'jpg', 'png', 'gif') NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE UserSong
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    spotify_id VARCHAR(22) NOT NULL, /* Spotify ids are always 22 characters */
    name VARCHAR(128) NOT NULL,
    duration VARCHAR(16) NOT NULL,
    release_date DATE NOT NULL,
    artist_name VARCHAR(128) NOT NULL,
    artist_spotify_id VARCHAR(22) NOT NULL,
    album_name VARCHAR(128) NOT NULL,
    album_artwork_url VARCHAR(256) NOT NULL,
    is_explicit BOOLEAN NOT NULL DEFAULT FALSE,
    youtube_video_id VARCHAR (32) NOT NULL,
    lyrics_snippet TEXT, /* API allows first 30% of song for free */
    user_id INT UNSIGNED,
    FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE UserArtist
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    spotify_id VARCHAR(22) NOT NULL,
    name VARCHAR(128) NOT NULL,
    image_url VARCHAR(256) NOT NULL,
    user_id INT UNSIGNED,
    FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE CASCADE
);

/* This is a concert added to users personal list */
/* Because there can be a limitless number of artists featured in the concert we just store the primary artist info and then list the other artists in the description */
/* Ticketmaster ids vary in length */
CREATE TABLE UserConcert
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    event_id VARCHAR(30) NOT NULL, /* ID provided by ticketmaster used to get ticketing info */
    event_name VARCHAR(128) NOT NULL,
    event_url VARCHAR(256) NOT NULL,
    artist_name VARCHAR(128) NOT NULL,
    artist_id VARCHAR(30) NOT NULL, /* ID provided by ticketmaster used to get other events from same artist */
    venue_name VARCHAR(128) NOT NULL,
    venue_id VARCHAR(30) NOT NULL, /*ID provided by ticketmaster used to get other events at same venue */
    venue_state VARCHAR(2) NOT NULL,
    venue_city VARCHAR(64) NOT NULL,
    venue_latitude DECIMAL(8, 6) NOT NULL,     /* Ranges from  -90 S to  90 N */
    venue_longitude DECIMAL(9, 6) NOT NULL,    /* Ranges from -180 W to 180 E */
    description VARCHAR (1024) NOT NULL,
    start_date_time DATETIME NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    image_url VARCHAR(256) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

/* To store album data for UserArtists */
CREATE TABLE UserArtistAlbum
(
	id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
	spotify_id VARCHAR(22),
	name VARCHAR(128) NOT NULL,
	artists VARCHAR(256) NOT NULL,
	release_date VARCHAR(12) NOT NULL,
    image_url VARCHAR(256) NOT NULL,
	artist_id INT UNSIGNED NOT NULL,
	FOREIGN KEY (artist_id) REFERENCES UserArtist(id) ON DELETE CASCADE
);

/* To store playlist data for users */
CREATE TABLE Playlist
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    image_url VARCHAR(256) NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

/* Join table for playlist songs */
CREATE TABLE PlaylistSong (
   id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
   playlist_id INT UNSIGNED NOT NULL,
   song_id INT UNSIGNED NOT NULL,
   FOREIGN KEY (playlist_id) REFERENCES Playlist(id) ON DELETE CASCADE,
   FOREIGN KEY (song_id) REFERENCES UserSong(id) ON DELETE CASCADE,
   UNIQUE (playlist_id, song_id)
);
