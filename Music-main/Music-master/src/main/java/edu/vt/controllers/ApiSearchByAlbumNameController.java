package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserSong;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.SongApi;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.primefaces.event.ItemSelectEvent;
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

@Named("apiSearchByAlbumNameController")
@SessionScoped
public class ApiSearchByAlbumNameController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    @Inject
    private UserController userController;

    private String albumNameQuery;

    private String bearerToken;

    private LocalDateTime tokenExpiration;

    private List<SongApi> foundSongs = null;

    private SongApi selected = null;
    @Named("userSongController")
    @Inject
    private UserSongController userSongController;

    private String barModel;
    private boolean chartInitialized = false;

    private String returnUrl;

    private String baseUrl = "/search/SearchAlbum?faces-redirect=true";
    @Named("playlistSongController")
    @Inject
    private PlaylistSongController playlistSongController;

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public void resetChartState() {
        chartInitialized = false;
        barModel = null;
    }

    public void itemSelect(ItemSelectEvent event) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                "Value: " + event.getData()
                        + ", Item Index: " + event.getItemIndex()
                        + ", DataSet Index:" + event.getDataSetIndex());

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void createAlbumPopularityChart() {
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
                                        .setText("Popularity of Songs in " + sortedSongs.get(0).getAlbum_name()))))
                .toJson();

        chartInitialized = true;
    }

    public String getBarModel() {
        return barModel;
    }

    public void setBarModel(String barModel) {
        this.barModel = barModel;
    }

    public void unselect() {
        selected = null;
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

    public String getAlbumNameQuery() {
        return albumNameQuery;
    }

    public void setAlbumNameQuery(String albumNameQuery) {
        this.albumNameQuery = albumNameQuery;
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
            newUserSong.setLyrics_snippet(getLyricsSnippet());

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

    //=================================
    // Format the Album name
    //=================================
    public String getFormattedAlbumHeader() {
        ResourceBundle bundle = ResourceBundle.getBundle("language",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String pattern = bundle.getString("AlbumHeader");

        String album = "Unknown Album";
        if (foundSongs != null && !foundSongs.isEmpty()) {
            album = foundSongs.getFirst().getAlbum_name();
        }

        // Format the string with the full name
        return MessageFormat.format(pattern, album);
    }

    //================================
    // Perform Spotify API Album Search from user Song
    //================================
    public String performAlbumSearch() throws Exception {
        albumNameQuery = userSongController.getSelected().getAlbum_name();
        return performSearch("/userSong/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Album Search from playlist Song
    //================================
    public String performPlaylistSongAlbumSearch() throws Exception {
        albumNameQuery = playlistSongController.getSelected().getSong_id().getAlbum_name();
        return performSearch("/playlistSong/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Spotify API Album Search by Name Query
    //================================
    public String performSearch(String returnUrlString) throws Exception {
        resetChartState();
        foundSongs = null;
        // Replace spaces with +
        String query = albumNameQuery.replace(" ", "+");
        if (tokenExpiration == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            getNewBearerToken();
        }

        // First, search for the album to get its ID
        String albumSearchApiUrl = "https://api.spotify.com/v1/search?q=" + query + "&type=album&limit=1";

        Methods.preserveMessages();
        try {
            if (albumNameQuery == null || albumNameQuery.isEmpty()) {
                throw new IOException("IOException Occurred");
            }

            // Search for album to get its ID
            URL albumSearchUrl = new URL(albumSearchApiUrl);
            HttpURLConnection albumSearchConn = (HttpURLConnection) albumSearchUrl.openConnection();
            albumSearchConn.setRequestMethod("GET");
            albumSearchConn.setRequestProperty("Authorization", "Bearer " + bearerToken);

            // Read the album search response
            int albumSearchResponseCode = albumSearchConn.getResponseCode();
            if (albumSearchResponseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader albumReader = new BufferedReader(new InputStreamReader(albumSearchConn.getInputStream()));
                StringBuilder albumResponseBuilder = new StringBuilder();
                String line;
                while ((line = albumReader.readLine()) != null) {
                    albumResponseBuilder.append(line);
                }
                JSONObject albumJsonResponse = new JSONObject(albumResponseBuilder.toString());

                if (albumJsonResponse == null) throw new IOException("IOException Occurred");

                JSONArray albumItems = albumJsonResponse.getJSONObject("albums").getJSONArray("items");
                if (albumItems == null || albumItems.length() == 0) throw new IOException("No albums found");

                String albumId = albumItems.getJSONObject(0).getString("id");

                // Now get tracks from the album
                String tracksApiUrl = "https://api.spotify.com/v1/albums/" + albumId + "/tracks?limit=50";

                URL tracksUrl = new URL(tracksApiUrl);
                HttpURLConnection tracksConn = (HttpURLConnection) tracksUrl.openConnection();
                tracksConn.setRequestMethod("GET");
                tracksConn.setRequestProperty("Authorization", "Bearer " + bearerToken);

                // Read the tracks response
                int tracksResponseCode = tracksConn.getResponseCode();
                if (tracksResponseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader tracksReader = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
                    StringBuilder tracksResponseBuilder = new StringBuilder();
                    while ((line = tracksReader.readLine()) != null) {
                        tracksResponseBuilder.append(line);
                    }
                    JSONObject tracksJsonResponse = new JSONObject(tracksResponseBuilder.toString());

                    if (tracksJsonResponse == null) throw new IOException("IOException Occurred");

                    // Get album details from the first search
                    JSONObject album = albumItems.getJSONObject(0);
                    String albumName = album.getString("name");
                    String albumArtworkUrl = null;
                    JSONArray images = album.getJSONArray("images");
                    if (images.length() > 0) {
                        albumArtworkUrl = images.getJSONObject(0).getString("url");
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

                    // First collect all track IDs to get their full details including popularity
                    JSONArray tracks = tracksJsonResponse.getJSONArray("items");
                    List<String> trackIds = new ArrayList<>();
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject track = tracks.getJSONObject(i);
                        trackIds.add(track.getString("id"));
                    }

                    // Get full track details including popularity
                    Map<String, Integer> trackPopularities = new HashMap<>();
                    if (!trackIds.isEmpty()) {
                        String trackIdsParam = String.join(",", trackIds);
                        String tracksDetailsUrl = "https://api.spotify.com/v1/tracks?ids=" + trackIdsParam;

                        HttpURLConnection tracksDetailsConn = (HttpURLConnection) new URL(tracksDetailsUrl).openConnection();
                        tracksDetailsConn.setRequestMethod("GET");
                        tracksDetailsConn.setRequestProperty("Authorization", "Bearer " + bearerToken);

                        if (tracksDetailsConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader detailsReader = new BufferedReader(
                                    new InputStreamReader(tracksDetailsConn.getInputStream()));
                            StringBuilder detailsResponse = new StringBuilder();
                            while ((line = detailsReader.readLine()) != null) {
                                detailsResponse.append(line);
                            }

                            JSONObject detailsJson = new JSONObject(detailsResponse.toString());
                            JSONArray tracksWithDetails = detailsJson.getJSONArray("tracks");

                            for (int i = 0; i < tracksWithDetails.length(); i++) {
                                JSONObject detailedTrack = tracksWithDetails.getJSONObject(i);
                                trackPopularities.put(
                                        detailedTrack.getString("id"),
                                        detailedTrack.getInt("popularity")
                                );
                            }
                        }
                    }

                    // Now create the SongApi objects with actual popularity values
                    foundSongs = new ArrayList<>();
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject track = tracks.getJSONObject(i);
                        if (track == null) throw new IOException("IOException Occurred");

                        String spotify_id = track.getString("id");
                        if (spotify_id == null) throw new IOException("IOException Occurred");
                        String name = track.getString("name");
                        if (name == null) throw new IOException("IOException Occurred");

                        // Get the actual popularity or default to 50 if not available
                        Integer popularity = trackPopularities.getOrDefault(spotify_id, 50);

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

                        // Create SongApi object with actual popularity
                        SongApi song = new SongApi(
                                spotify_id, name, duration, releaseDateParsed,
                                artist_name, artist_spotify_id,
                                albumName, albumArtworkUrl,
                                is_explicit, popularity
                        );

                        foundSongs.add(song);
                    }
                } else {
                    Methods.showMessage("Information", "Spotify API Error!",
                            "Spotify API tracks request failed!");
                }
            } else {
                Methods.showMessage("Information", "Spotify API Error!",
                        "Spotify API album search request failed!");
            }
        } catch (IOException ex) {
            Methods.showMessage("Information", "Unrecognized Search Query!",
                    "Spotify API provides no data for the search query entered!");

            return "/search/SearchAlbum?faces-redirect=true";
        }
        // Reset search queries
        albumNameQuery = "";

        setReturnUrl(returnUrlString);
        return "/search/SearchAlbumResults?faces-redirect=true";
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
}
