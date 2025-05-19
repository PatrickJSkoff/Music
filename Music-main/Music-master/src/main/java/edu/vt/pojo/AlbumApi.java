package edu.vt.pojo;

// POJO representing an Album returned from the API
public class AlbumApi {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String spotify_id;
    private String name;
    private String artist_name;
    private String release_date;
    private String image_url;

    // Constructor Method
    public AlbumApi(String spotify_id, String name, String artist_name, String release_date, String image_url) {
        this.spotify_id = spotify_id;
        this.name = name;
        this.artist_name = artist_name;
        this.release_date = release_date;
        this.image_url = image_url;
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

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String toString() {
        return name+"\n";
    }
}
