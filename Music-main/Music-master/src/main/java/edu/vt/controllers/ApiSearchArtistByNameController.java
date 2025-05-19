package edu.vt.controllers;

import edu.vt.EntityBeans.UserArtist;
import edu.vt.EntityBeans.UserArtistAlbum;
import edu.vt.EntityBeans.UserSong;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.AlbumApi;
import edu.vt.pojo.ArtistApi;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Named("apiSearchByArtistNameController")
@SessionScoped
public class ApiSearchArtistByNameController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    @Inject
    private UserController userController;

    private String nameQuery;

    private String bearerToken;

    private LocalDateTime tokenExpiration;

    private List<ArtistApi> foundArtists = null;

    private ArtistApi selected = null;

    private String returnUrl;

    private String baseUrl = "/search/SearchArtist?faces-redirect=true";

    @Named("userArtistController")
    @Inject
    private UserArtistController userArtistController;

    @Named("userArtistAlbumController")
    @Inject
    private UserArtistAlbumController userArtistAlbumController;

    @Inject
    private UserConcertController userConcertController;
    @Named("userSongController")
    @Inject
    private UserSongController userSongController;
    @Named("playlistSongController")
    @Inject
    private PlaylistSongController playlistSongController;

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public void unselect(){
        selected = null;
    }

    public ArtistApi getSelected() {
        return selected;
    }

    public void setSelected(ArtistApi selected) {
        this.selected = selected;
    }

    public String getNameQuery() {
        return nameQuery;
    }

    public void setNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
    }

    public List<ArtistApi> getFoundArtists() {
        return foundArtists;
    }

    public void setFoundArtists(List<ArtistApi> foundArtists) {
        this.foundArtists = foundArtists;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //Spotify API requires a bearer token requested using user client id and client secret every hour
    //Requests to spotify api are authorized using this bearer token
    private void getNewBearerToken() {
        try {
            // Spotify token endpoint
            URL url = new URL("https://accounts.spotify.com/api/token");

            // Prepare connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Build request body
            String data = "grant_type=client_credentials"
                    + "&client_id=" + URLEncoder.encode(Constants.SPOTIFY_CLIENT_ID, "UTF-8")
                    + "&client_secret=" + URLEncoder.encode(Constants.SPOTIFY_CLIENT_SECRET, "UTF-8");

            // Send POST data
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
                os.flush();
            }

            // Read response
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse JSON
                    JSONObject json = new JSONObject(response.toString());
                    String accessToken = json.getString("access_token");
                    int expiresIn = json.getInt("expires_in");

                    bearerToken = accessToken;
                    //Account for potential delay in response of token 10 seconds should be plenty
                    tokenExpiration = LocalDateTime.now().plusSeconds(expiresIn - 10);
                }
            } else {
                System.err.println("Error response from Spotify: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //================================
    // Perform Spotify API Artist Search by Concert Artist Name Query
    //================================
    public String performConcertArtistSearch() throws Exception {
        nameQuery = userConcertController.getSelected().getArtist_name();
        return performSearch("/userConcert/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Artist Search by Song Artist Name Query
    //================================
    public String performSongArtistSearch() throws Exception {
        nameQuery = userSongController.getSelected().getArtist_name();
        return performSearch("/userSong/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Artist Search by Playlist Song Artist Name Query
    //================================
    public String performPlaylistSongArtistSearch() throws Exception {
        nameQuery = playlistSongController.getSelected().getSong_id().getArtist_name();
        return performSearch("/playlistSong/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Artist Search by Name Query
    //================================
    public String performSearch(String returnUrlString) throws Exception {
        foundArtists = null;
        // Replace spaces with +
        String query = nameQuery.replace(" ", "+");
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }
        String searchApiUrl = "https://api.spotify.com/v1/search?q=" + query + "&type=artist";
        /*
        Redirecting to show a Jakarta Faces page involves more than one subsequent requests and
        the messages would die from one request to another if not kept in the Flash scope.
        Since we will redirect to show the search Results page, we invoke preserveMessages().
         */
        Methods.preserveMessages();
        try {
            if (nameQuery == null || nameQuery.isEmpty()) {
                throw new IOException("IOException Occurred");
            }
            // Setup connection
            URL url = new URL(searchApiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);

            // Read the response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //Build Json from HttpResponse
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                //Parse Json from HttpResponse
                if (jsonResponse == null) throw new IOException("IOException Occurred");
                foundArtists = new ArrayList<>();
                JSONObject artists = jsonResponse.getJSONObject("artists");
                if (artists == null) throw new IOException("IOException Occurred");
                JSONArray items = artists.getJSONArray("items");
                if (items == null) throw new IOException("IOException Occurred");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject artist = items.getJSONObject(i);
                    if (artist == null) throw new IOException("IOException Occurred");
                    String id = artist.getString("id");
                    if (id == null) throw new IOException("IOException Occurred");
                    String name = artist.getString("name");
                    if (name == null) throw new IOException("IOException Occurred");
                    //We just get the first image associated with the artist
                    String imageUrl = null;
                    JSONArray images = artist.getJSONArray("images");
                    if (images.length() > 0) {
                        imageUrl = images.getJSONObject(0).getString("url");
                        if (imageUrl == null) throw new IOException("IOException Occurred");
                    }

                    //search for albums
                    List<AlbumApi> albums = getAlbums(id);

                    ArtistApi artistApi = new ArtistApi(id, name, imageUrl, albums);
                    foundArtists.add(artistApi);
                }
            } else {
                Methods.showMessage("Information", "Spotify API Error!",
                        "Spotify API request failed!");
            }
        } catch (IOException ex) {
            Methods.showMessage("Information", "Unrecognized Search Query!",
                    "Spotify API provides no data for the search query entered!");

            return "/search/SearchArtist?faces-redirect=true";
        }
        // Reset search queries
        nameQuery = "";

        setReturnUrl(returnUrlString);
        return "/search/SearchArtistResults?faces-redirect=true";
    }

    public void shareToUserArtists() {
        Methods.preserveMessages();

        if (userController.userIsSignedIn()) {

            // Ask userVideoController to instantiate a new UserVideo object
            // and store its object reference into newUserVideo
            UserArtist newUserArtist = userArtistController.prepareCreate();

            // Dress it up with the attributes of 'selected'
            newUserArtist.setSpotify_id(selected.getSpotify_id());
            newUserArtist.setName(selected.getName());
            newUserArtist.setImage_url(selected.getImage_url());

            // Ask userSongController to create the new user video in the database
            userArtistController.create();

            //create UserArtistAlbums with recently made artist
            List<AlbumApi> albums = selected.getAlbums();
            UserArtist userArtist = userArtistController.getLastArtist();
            for (AlbumApi album : albums) {
                UserArtistAlbum newUserAlbum = userArtistAlbumController.prepareCreate();
                newUserAlbum.setArtists(album.getArtist_name());
                newUserAlbum.setSpotify_id(album.getSpotify_id());
                newUserAlbum.setName(album.getName());
                newUserAlbum.setImage_url(album.getImage_url());
                newUserAlbum.setRelease_date(album.getRelease_date());
                newUserAlbum.setUserArtistId(userArtist);
                userArtistAlbumController.create();
            }

        } else {
            // No user is signed in
            Methods.showMessage("Information", "Unable to Add!",
                    "To add an artist, a user must have signed in!");
        }
    }

    public List<AlbumApi> getAlbums(String spotifyId) {
        List<AlbumApi> albums = new ArrayList<>();
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }
        String searchApiUrl = "https://api.spotify.com/v1/artists/" + spotifyId + "/albums";
        try{
            URL url = new URL(searchApiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);

            // Read the response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //Build Json from HttpResponse
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                JSONArray items = jsonResponse.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject album = items.getJSONObject(i);
                    String id = album.getString("id");
                    String albumName = album.getString("name");
                    String albumArtworkUrl = null;
                    JSONArray images = album.getJSONArray("images");
                    if (images.length() > 0) {
                        albumArtworkUrl = images.getJSONObject(0).getString("url");
                    }
                    String artistName = "";
                    JSONArray artists = album.getJSONArray("artists");
                    if (artists.length() > 0) {
                        for(int j = 0; j < artists.length(); j++) {
                            JSONObject artist = artists.getJSONObject(j);
                            artistName = artistName + artist.getString("name") + ", ";
                        }
                        artistName = artistName.substring(0, artistName.length() - 2);
                    }
                    String releaseDate = album.getString("release_date");
                    String releaseDatePrecision = album.getString("release_date_precision");

                    // Parse release date with precision fallback
                    LocalDate releaseDateParsed;
                    try {
                        if ("day".equals(releaseDatePrecision)) {
                            releaseDateParsed = LocalDate.parse(releaseDate);  // Format: yyyy-MM-dd
                        } else if ("month".equals(releaseDatePrecision)) {
                            releaseDateParsed = LocalDate.parse(releaseDate + "-01");  // yyyy-MM-01
                        } else {
                            releaseDateParsed = LocalDate.parse(releaseDate + "-01-01");  // yyyy-01-01
                        }
                    } catch (Exception e) {
                        releaseDateParsed = LocalDate.of(1900, 1, 1);  // fallback value
                    }
                    AlbumApi albumApi = new AlbumApi(id, albumName, artistName, releaseDateParsed.toString(), albumArtworkUrl);
                    albums.add(albumApi);
                }
            }
        } catch (IOException ex) {

        }
        return albums;
    }
}
