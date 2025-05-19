package edu.vt.pojo;

import java.util.List;

// This class provides a Plain Old Java Object (POJO) representing a Arist returned from the API
public class ArtistApi {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String spotify_id;
    private String name;
    private String image_url;
    private List<AlbumApi> albums;

    // Constructor Method

    public ArtistApi(String spotify_id, String name, String image_url, List<AlbumApi> albums) {
        this.spotify_id = spotify_id;
        this.name = name;
        this.image_url = image_url;
        this.albums = albums;
    }


    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public String getSpotify_id() {
        return spotify_id;
    }

    public void setSpotify_id(String spotify_id) {
        this.spotify_id = spotify_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public List<AlbumApi> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumApi> albums) {
        this.albums = albums;
    }
}
