package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserSong;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.SongApi;
import jakarta.el.MethodExpression;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.CartesianScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.CartesianTickOptions;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.enums.IndexAxis;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Named("apiSearchBySongNameController")
@SessionScoped
public class ApiSearchSongByNameController implements Serializable {
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

    private List<SongApi> foundSongs = null;

    private String barModel;

    private boolean chartInitialized = false;

    private SongApi selected = null;

    private String returnUrl;

    private String baseUrl = "/search/SearchArtist?faces-redirect=true";

    @Named("userSongController")
    @Inject
    private UserSongController userSongController;
    @Named("songQuizController")
    @Inject
    private SongQuizController songQuizController;

    @Inject
    private UserArtistController userArtistController;

    @Inject
    private UserConcertController userConcertController;

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public void resetChartState() {
        chartInitialized = false;
        barModel = null;
    }

    public void unselect() {
        selected = null;
    }

    public void itemSelect(ItemSelectEvent event) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                "Value: " + event.getData()
                        + ", Item Index: " + event.getItemIndex()
                        + ", DataSet Index:" + event.getDataSetIndex());

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getYoutubeVideoId() {
        if (selected != null) {
            if (selected.getYoutube_id() == null) {
                getSelectedYoutubeIdFromApi();
            }
            return selected.getYoutube_id();
        }
        return "";
    }

    public String getLyricsSnippet() {
        if (selected != null) {
            if (selected.getLyrics_snippet() == null) {
                getLyricsFromAudD();
            }
            return selected.getLyrics_snippet();
        }
        return "";
    }

    public SongApi getSelected() {
        return selected;
    }

    public void setSelected(SongApi selected) {
        this.selected = selected;
    }

    public String getNameQuery() {
        return nameQuery;
    }

    public void setNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
    }

    public List<SongApi> getFoundSongs() {
        return foundSongs;
    }

    public void setFoundSongs(List<SongApi> foundSongs) {
        this.foundSongs = foundSongs;
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
    //==============================================
    // Normalize duration for storing and sorting
    //==============================================

    public void createSongPopularityChart() {
        // Check if the chart has already been initialized
        if (chartInitialized || foundSongs == null || foundSongs.isEmpty()) {
            return;
        }

        // Sort songs by popularity in descending order
        List<SongApi> sortedSongs = new ArrayList<>(foundSongs);
        sortedSongs.sort(Comparator.comparingInt(SongApi::getPopularity).reversed());

        // Prepare data for the chart
        Integer[] popularityValues = sortedSongs.stream()
                .map(SongApi::getPopularity)
                .toArray(Integer[]::new);

        String[] songNames = sortedSongs.stream()
                .map(SongApi::getName)
                .toArray(String[]::new);

        // Create the bar chart
        barModel = new BarChart()
                .setData(new BarData()
                        .addDataset(new BarDataset()
                                .setData(popularityValues)
                                .setLabel("Song Popularity (0-100)")
                                .setBackgroundColor(new RGBAColor(75, 192, 192))
                                .setBorderColor(new RGBAColor(75, 192, 192))
                                .setBorderWidth(1))
                        .setLabels(songNames))
                .setOptions(new BarOptions()
                        .setResponsive(true)
                        .setMaintainAspectRatio(false)
                        .setIndexAxis(IndexAxis.X)
                        .setScales(new Scales().addScale(Scales.ScaleAxis.Y, new CartesianScaleOptions()
                                .setStacked(false)
                                .setTicks(new CartesianTickOptions()
                                        .setAutoSkip(true)
                                        .setMirror(false))
                                .setMin(0)
                                .setMax(100)))
                        .setPlugins(new Plugins()
                                .setTitle(new Title()
                                        .setDisplay(true)
                                        .setText("Popularity of Found Songs"))))
                .toJson();

        chartInitialized = true;
    }

    public String getBarModel() {
        return barModel;
    }

    public void setBarModel(String barModel) {
        this.barModel = barModel;
    }

    private String normalizeDuration(String input) {
        String[] parts = input.split(":");

        if (parts.length == 1) {
            // SS → 00:00:SS
            return String.format("00:00:%02d", Integer.parseInt(parts[0]));
        } else if (parts.length == 2) {
            // MM:SS → 00:MM:SS
            return String.format("00:%02d:%02d",
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]));
        } else if (parts.length == 3) {
            // HH:MM:SS → already correct, just format
            return String.format("%02d:%02d:%02d",
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } else {
            return "00:00:00"; // fallback
        }
    }

    //==============================================
    // Share Selected Song to Signed-In User's Songs
    //==============================================
    public void shareToUserSongs() {
        Methods.preserveMessages();

        if (userController.userIsSignedIn()) {

            // Ask userVideoController to instantiate a new UserVideo object
            // and store its object reference into newUserVideo
            UserSong newUserSong = userSongController.prepareCreate();

            // Obtain the signed-in user's object reference that was placed in the
            // sessionMap in initializeSessionMap() method of LoginManager
            User signedInUser = (User) FacesContext.getCurrentInstance().
                    getExternalContext().getSessionMap().get("user");

            // Set newUserFavorite to belong to the signed-in user
            newUserSong.setUserId(signedInUser);

            // Dress it up with the attributes of 'selected'
            newUserSong.setSpotify_id(selected.getSpotify_id());
            newUserSong.setName(selected.getName());
            newUserSong.setDuration(normalizeDuration(selected.getDuration()));
            newUserSong.setRelease_date(selected.getRelease_date());
            newUserSong.setArtist_name(selected.getArtist_name());
            newUserSong.setArtist_spotify_id(selected.getArtist_spotify_id());
            newUserSong.setAlbum_name(selected.getAlbum_name());
            newUserSong.setAlbum_artwork_url(selected.getAlbum_artwork_url());
            newUserSong.setIs_explicit(selected.getIs_explicit());
            newUserSong.setYoutube_video_id(getYoutubeVideoId());
            newUserSong.setLyrics_snippet(getLyricsSnippet());;

            userSongController.setSelected(newUserSong);
            // Ask userSongController to create the new user video in the database
            userSongController.create();

        } else {
            // No user is signed in
            Methods.showMessage("Information", "Unable to Add!",
                    "To add a song, a user must have signed in!");
        }
    }

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

    // Perform Spotify API Song Search by Artist Spotify ID
    public String performArtistSearch() throws Exception {
        resetChartState();
        foundSongs = null;
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }
        //We get 20 tracks because they are not sorted by most popular so the actual track user is searching for is usually not first
        String searchApiUrl = "https://api.spotify.com/v1/artists/" + userArtistController.getSelected().getSpotify_id() + "/top-tracks";
        /*
        Redirecting to show a Jakarta Faces page involves more than one subsequent requests and
        the messages would die from one request to another if not kept in the Flash scope.
        Since we will redirect to show the search Results page, we invoke preserveMessages().
         */
        Methods.preserveMessages();
        try {
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
                foundSongs = new ArrayList<>();
                JSONArray items = jsonResponse.getJSONArray("tracks");
                if (items == null) throw new IOException("IOException Occurred");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject track = items.getJSONObject(i);
                    if (track == null) throw new IOException("IOException Occurred");

                    String spotify_id = track.getString("id");
                    if (spotify_id == null) throw new IOException("IOException Occurred");
                    String name = track.getString("name");
                    if (name == null) throw new IOException("IOException Occurred");
                    Integer popularity = track.getInt("popularity");
                    // Convert duration in ms to mm:ss format
                    int duration_ms = track.getInt("duration_ms");
                    if (duration_ms == 0) throw new IOException("IOException Occurred");
                    String duration = String.format("%d:%02d", duration_ms / 60000, (duration_ms % 60000) / 1000);

                    boolean is_explicit = track.getBoolean("explicit");

                    // Artist info (take first artist)
                    JSONObject artist = track.getJSONArray("artists").getJSONObject(0);
                    if (artist == null) throw new IOException("IOException Occurred");

                    String artist_name = artist.getString("name");
                    if (artist_name == null) throw new IOException("IOException Occurred");
                    String artist_spotify_id = artist.getString("id");
                    if (artist_spotify_id == null) throw new IOException("IOException Occurred");

                    // Album info
                    JSONObject album = track.getJSONObject("album");
                    if (album == null) throw new IOException("IOException Occurred");
                    String album_name = album.getString("name");
                    if (album_name == null) throw new IOException("IOException Occurred");

                    String album_artwork_url = null;

                    JSONArray images = album.getJSONArray("images");
                    if (images.length() > 0) {
                        album_artwork_url = images.getJSONObject(0).getString("url");
                    }
                    if (album_artwork_url == null) throw new IOException("IOException Occurred");

                    // Handle release date with precision fallback
                    LocalDate release_date;

                    try {
                        String releaseDateStr = album.getString("release_date");
                        String releaseDatePrecision = album.getString("release_date_precision");

                        if ("day".equals(releaseDatePrecision)) {
                            release_date = LocalDate.parse(releaseDateStr);  // Format: yyyy-MM-dd
                        } else if ("month".equals(releaseDatePrecision)) {
                            // Format: yyyy-MM → add day
                            release_date = LocalDate.parse(releaseDateStr + "-01");  // yyyy-MM-01
                        } else {
                            // Format: yyyy → add month and day
                            release_date = LocalDate.parse(releaseDateStr + "-01-01");  // yyyy-01-01
                        }
                    } catch (Exception e) {
                        release_date = LocalDate.of(1900, 1, 1);  // fallback value
                    }

                    // Create object
                    SongApi song = new SongApi(
                            spotify_id, name, duration, release_date,
                            artist_name, artist_spotify_id,
                            album_name, album_artwork_url,
                            is_explicit, popularity
                    );

                    foundSongs.add(song);
                }
                //Sort by popularity to find song user was most likely searching for
                //foundSongs.sort(Comparator.comparing(SongApi::getPopularity).reversed());
            } else {
                Methods.showMessage("Information", "Spotify API Error!",
                        "Spotify API request failed!");
            }
        } catch (IOException ex) {
            Methods.showMessage("Information", "Unrecognized Search Query!",
                    "Spotify API provides no data for the search query entered!");

            return "/search/SearchSong?faces-redirect=true";
        }
        // Reset search queries
        nameQuery = "";

        setReturnUrl("/userArtist/DataTable?faces-redirect=true");
        return "/search/SearchSongResults?faces-redirect=true";
    }

    //================================
    // Perform Spotify API Song Search by Concert Artist Name Query
    //================================
    public String performConcertArtistSearch() throws Exception {
        nameQuery = "artist:" + userConcertController.getSelected().getArtist_name();
        return performSearch("/userConcert/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Song Search by Name Query
    //================================
    public String performSearch(String returnUrlString) throws Exception {
        resetChartState();
        foundSongs = null;
        // Replace spaces with +
        String query = nameQuery.replace(" ", "+");
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }
        //We get 20 tracks because they are not sorted by most popular so the actual track user is searching for is usually not first
        String searchApiUrl = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=20";
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
                foundSongs = new ArrayList<>();
                JSONArray items = jsonResponse.getJSONObject("tracks").getJSONArray("items");
                if (items == null) throw new IOException("IOException Occurred");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject track = items.getJSONObject(i);
                    if (track == null) throw new IOException("IOException Occurred");

                    String spotify_id = track.getString("id");
                    if (spotify_id == null) throw new IOException("IOException Occurred");
                    String name = track.getString("name");
                    if (name == null) throw new IOException("IOException Occurred");
                    Integer popularity = track.getInt("popularity");
                    // Convert duration in ms to mm:ss format
                    int duration_ms = track.getInt("duration_ms");
                    if (duration_ms == 0) throw new IOException("IOException Occurred");
                    String duration = String.format("%d:%02d", duration_ms / 60000, (duration_ms % 60000) / 1000);

                    boolean is_explicit = track.getBoolean("explicit");

                    // Artist info (take first artist)
                    JSONObject artist = track.getJSONArray("artists").getJSONObject(0);
                    if (artist == null) throw new IOException("IOException Occurred");

                    String artist_name = artist.getString("name");
                    if (artist_name == null) throw new IOException("IOException Occurred");
                    String artist_spotify_id = artist.getString("id");
                    if (artist_spotify_id == null) throw new IOException("IOException Occurred");

                    // Album info
                    JSONObject album = track.getJSONObject("album");
                    if (album == null) throw new IOException("IOException Occurred");
                    String album_name = album.getString("name");
                    if (album_name == null) throw new IOException("IOException Occurred");

                    String album_artwork_url = null;

                    JSONArray images = album.getJSONArray("images");
                    if (images.length() > 0) {
                        album_artwork_url = images.getJSONObject(0).getString("url");
                    }
                    if (album_artwork_url == null) throw new IOException("IOException Occurred");

                    // Handle release date with precision fallback
                    LocalDate release_date;

                    try {
                        String releaseDateStr = album.getString("release_date");
                        String releaseDatePrecision = album.getString("release_date_precision");

                        if ("day".equals(releaseDatePrecision)) {
                            release_date = LocalDate.parse(releaseDateStr);  // Format: yyyy-MM-dd
                        } else if ("month".equals(releaseDatePrecision)) {
                            // Format: yyyy-MM → add day
                            release_date = LocalDate.parse(releaseDateStr + "-01");  // yyyy-MM-01
                        } else {
                            // Format: yyyy → add month and day
                            release_date = LocalDate.parse(releaseDateStr + "-01-01");  // yyyy-01-01
                        }
                    } catch (Exception e) {
                        release_date = LocalDate.of(1900, 1, 1);  // fallback value
                    }

                    // Create object
                    SongApi song = new SongApi(
                            spotify_id, name, duration, release_date,
                            artist_name, artist_spotify_id,
                            album_name, album_artwork_url,
                            is_explicit, popularity
                    );

                    foundSongs.add(song);
                }
                //Sort by popularity to find song user was most likely searching for
                //foundSongs.sort(Comparator.comparing(SongApi::getPopularity).reversed());
            } else {
                Methods.showMessage("Information", "Spotify API Error!",
                        "Spotify API request failed!");
            }
        } catch (IOException ex) {
            Methods.showMessage("Information", "Unrecognized Search Query!",
                    "Spotify API provides no data for the search query entered!");

            return "/search/SearchSong?faces-redirect=true";
        }
        // Reset search queries
        nameQuery = "";

        setReturnUrl(returnUrlString);
        return "/search/SearchSongResults?faces-redirect=true";
    }

    public void getSelectedYoutubeIdFromApi() {
        String searchQuery = selected.getName() + "+" + selected.getArtist_name();
        searchQuery = searchQuery.replace(" ", "+");
        String searchApiUrl = "https://www.googleapis.com/youtube/v3/search?type=video&maxResults=1&q=" + searchQuery + "&key=" + Constants.GOOGLE_API_KEY;

        String firstVideoId = null;
        try {
            // Get the JSON response from the YouTube Search API
            String searchResultsJsonData = Methods.readUrlContent(searchApiUrl);

            // Parse the top-level object
            JSONObject json = new JSONObject(searchResultsJsonData);
            JSONArray items = json.optJSONArray("items");

            // Validate array and extract the first videoId
            if (items != null && items.length() > 0) {
                JSONObject firstItem = items.optJSONObject(0);
                if (firstItem != null) {
                    JSONObject idObject = firstItem.optJSONObject("id");
                    if (idObject != null) {
                        firstVideoId = idObject.optString("videoId", null);
                    }
                }
            }

            if (firstVideoId != null) {
                System.out.println("First Video ID: " + firstVideoId);
            } else {
                throw new IOException("No video ID found");
            }

        } catch (Exception e) {
            Methods.showMessage("Error", "API Error", "Could not retrieve video ID from YouTube.");
            e.printStackTrace();
        }
        selected.setYoutube_id(firstVideoId);
    }

    //================================
    // Perform AudD API Search to get song lyrics
    //================================
    public void getLyricsFromAudD() {
        // Simplify track and artist names
        String trackName = selected.getName().replaceAll("\\s*\\(.*?\\)|\\s*-.*", "").trim();
        String artistName = selected.getArtist_name();

        String searchQuery = trackName + " " + artistName;
        String apiUrl = "https://api.audd.io/findLyrics/?q=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) + "&api_token=" + Constants.AUDD_API_KEY;

        try {
            // Read JSON from API response
            String jsonResponse = Methods.readUrlContent(apiUrl);

            // Parse JSON
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray results = json.optJSONArray("result");

            String lyrics = null;

            if (results != null && !results.isEmpty()) {
                JSONObject topResult = results.getJSONObject(0);
                lyrics = topResult.optString("lyrics", null);
            }

            if (lyrics != null && !lyrics.trim().isEmpty()) {
                lyrics = cleanLyrics(lyrics);
                selected.setLyrics_snippet(lyrics);
            } else {
                selected.setLyrics_snippet("Lyrics unavailable.");
            }

        } catch (Exception e) {
            selected.setLyrics_snippet("Lyrics unavailable.");
            e.printStackTrace();
            Methods.showMessage("Error", "Lyrics API Error", "Could not retrieve lyrics from AudD.");
        }
    }

    //================================
    // Fix Lyric formatting for display
    //================================
    private String cleanLyrics(String lyrics) {
        return lyrics
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("\\s+([.,!?])", "$1")
                .trim();
    }

    //================================
    // Find the users guessed song from the Spotify API
    //================================
    public void guessSongFromApi(String songGuess, String artistGuess) {
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }
        try {
            // Replace spaces with +
            String songQuery = songGuess.replace(" ", "+");
            String artisitQuery = artistGuess.replace(" ", "+");
            String searchApiUrl = "https://api.spotify.com/v1/search?q=track:" + songQuery + "+artist:" + artisitQuery + "&type=track&limit=1";

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
                reader.close();

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                //Parse Json from HttpResponse
                JSONArray items = jsonResponse.getJSONObject("tracks").getJSONArray("items");

                if (items.length() > 0) {
                    JSONObject track = items.getJSONObject(0);

                    // Song name
                    String name = track.getString("name");
                    if (name == null) throw new IOException("IOException Occurred");

                    // Artist info (take first artist)
                    JSONObject artist = track.getJSONArray("artists").getJSONObject(0);
                    if (artist == null) throw new IOException("IOException Occurred");
                    String artistName = artist.getString("name");

                    // Album artwork URL
                    String imageUrl = null;

                    JSONObject album = track.getJSONObject("album");
                    JSONArray images = album.getJSONArray("images");
                    if (!images.isEmpty()) {
                        imageUrl = images.getJSONObject(0).getString("url");
                    }

                    // Set the variables in song quiz controller if found
                    songQuizController.setSongGuess(name);
                    songQuizController.setArtistGuess(artistName);
                    songQuizController.setAlbumArtGuess(imageUrl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
