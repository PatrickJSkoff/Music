package edu.vt.pojo;

import java.time.LocalDate;
import java.util.Date;

// This class provides a Plain Old Java Object (POJO) representing a Song returned from the API
public class SongApi {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String spotify_id;
    private String name;
    private String duration;
    private LocalDate release_date;
    private String artist_name;
    private String artist_spotify_id;
    private String album_name;
    private String album_artwork_url;
    private boolean is_explicit;
    private Integer popularity;
    private String youtube_id;
    private String lyrics_snippet;

    // Constructor Method

    public SongApi(String spotify_id, String name, String duration, LocalDate release_date, String artist_name, String artist_spotify_id, String album_name, String album_artwork_url, boolean is_explicit, Integer popularity) {
        this.spotify_id = spotify_id;
        this.name = name;
        this.duration = duration;
        this.release_date = release_date;
        this.artist_name = artist_name;
        this.artist_spotify_id = artist_spotify_id;
        this.album_name = album_name;
        this.album_artwork_url = album_artwork_url;
        this.is_explicit = is_explicit;
        this.popularity = popularity;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public String getYoutube_id() {
        return youtube_id;
    }

    public void setYoutube_id(String youtube_id) {
        this.youtube_id = youtube_id;
    }

    public String getLyrics_snippet() {
        return lyrics_snippet;
    }

    public void setLyrics_snippet(String lyrics_snippet) {
        this.lyrics_snippet = lyrics_snippet;
    }

    public String getSpotify_id() {
        return spotify_id;
    }

    public void setSpotify_id(String spotify_id) {
        this.spotify_id = spotify_id;
    }

    public String getName() {
        return name;
    }

    public String getNameExplicit(){
        if(this.is_explicit){
            return this.name + "\uD83C\uDD74";
        } else{
            return this.name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDate getRelease_date() {
        return release_date;
    }

    public String getFormattedReleaseDate() {
        return release_date != null
                ? release_date.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                : "";
    }

    public void setRelease_date(LocalDate release_date) {
        this.release_date = release_date;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getArtist_spotify_id() {
        return artist_spotify_id;
    }

    public void setArtist_spotify_id(String artist_spotify_id) {
        this.artist_spotify_id = artist_spotify_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getAlbum_artwork_url() {
        return album_artwork_url;
    }

    public void setAlbum_artwork_url(String album_artwork_url) {
        this.album_artwork_url = album_artwork_url;
    }

    public boolean getIs_explicit() {
        return is_explicit;
    }

    public void setIs_explicit(boolean is_explicit) {
        this.is_explicit = is_explicit;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }
}
