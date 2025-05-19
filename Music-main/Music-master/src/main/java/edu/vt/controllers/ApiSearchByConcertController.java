package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserConcert;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.ConcertApi;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;
import ch.hsr.geohash.GeoHash;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.event.map.PointSelectEvent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;

@Named("apiSearchByConcertController")
@SessionScoped
public class ApiSearchByConcertController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    //Query Values
    private String nameQuery;
    private Date startDate;
    private Date endDate;
    //Center of US default
    private Double selectedLatitude = 38.0902;
    private Double selectedLongitude = -95.7129;
    //locationSelected used to check whether a user selected a location and wishes to search by location or if it was left blank
    private Boolean locationSelected = false;
    private String radiusMiles;
    private String maxResults;

    private MapModel pickLocationMapModel;

    private List<ConcertApi> foundConcerts = null;

    private ConcertApi selected = null;

    private String returnUrl;

    private String baseUrl = "/search/SearchConcert?faces-redirect=true";

    @Inject
    private UserController userController;
    @Named("userConcertController")
    @Inject
    private UserConcertController userConcertController;

    @Inject
    private UserArtistController userArtistController;
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

    public void unselect() {
        selected = null;
    }

    public String googleApiKey() {
        return Constants.GOOGLE_API_KEY;
    }

    //Google Map for concert venue
    public MapModel getGoogleMap() {
        // Instantiate a new DefaultMapModel and put its object reference into local variable googleMap
        MapModel googleMap = new DefaultMapModel();

        // Dress up the newly created MapModel with a Marker (map pin)
        googleMap.addOverlay(new Marker(new LatLng(selected.getVenue_latitude(),
                selected.getVenue_longitude()), selected.getVenue_name()));

        // More markers (map pins) can be added to the map as overlays here

        return googleMap;
    }

    //For Location picker on map
    public MapModel getPickLocationMapModel() {
        pickLocationMapModel = new DefaultMapModel();
        LatLng coords = new LatLng(selectedLatitude, selectedLongitude);
        Marker marker = new Marker(coords, "Previously Selected");
        pickLocationMapModel.addOverlay(marker);
        return pickLocationMapModel;
    }

    public void setPickLocationMapModel(MapModel pickLocationMapModel) {
        this.pickLocationMapModel = pickLocationMapModel;
    }

    public String getMapCenter() {
        return selectedLatitude + ", " + selectedLongitude;
    }

    public ConcertApi getSelected() {
        return selected;
    }

    public void setSelected(ConcertApi selected) {
        this.selected = selected;
    }

    public String getNameQuery() {
        return nameQuery;
    }

    public void setNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
    }

    public List<ConcertApi> getFoundConcerts() {
        return foundConcerts;
    }

    public void setFoundConcerts(List<ConcertApi> foundConcerts) {
        this.foundConcerts = foundConcerts;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getSelectedLatitude() {
        return selectedLatitude;
    }

    public void setSelectedLatitude(Double selectedLatitude) {
        this.selectedLatitude = selectedLatitude;
    }

    public Double getSelectedLongitude() {
        return selectedLongitude;
    }

    public void setSelectedLongitude(Double selectedLongitude) {
        this.selectedLongitude = selectedLongitude;
    }

    public String getRadiusMiles() {
        return radiusMiles;
    }

    public void setRadiusMiles(String radiusMiles) {
        this.radiusMiles = radiusMiles;
    }

    public String getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(String maxResults) {
        this.maxResults = maxResults;
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
    // Share Selected Song to Signed-In User's Songs
    //==============================================
    public void shareToUserConcerts() {
        Methods.preserveMessages();

        if (userController.userIsSignedIn()) {

            // Ask userVideoController to instantiate a new UserVideo object
            // and store its object reference into newUserVideo
            UserConcert newUserConcert = userConcertController.prepareCreate();

            // Obtain the signed-in user's object reference that was placed in the
            // sessionMap in initializeSessionMap() method of LoginManager
            User signedInUser = (User) FacesContext.getCurrentInstance().
                    getExternalContext().getSessionMap().get("user");

            // Set newUserFavorite to belong to the signed-in user
            newUserConcert.setUserId(signedInUser);

            // Dress it up with the attributes of 'selected'
            if (selected.getEvent_id() != null && !selected.getEvent_id().trim().isEmpty()) {
                newUserConcert.setEvent_id(selected.getEvent_id());
            } else {
                newUserConcert.setEvent_id("unknown_event");
            }

            if (selected.getEvent_name() != null && !selected.getEvent_name().trim().isEmpty()) {
                newUserConcert.setEvent_name(selected.getEvent_name());
            } else {
                newUserConcert.setEvent_name("Untitled Event");
            }

            if (selected.getEvent_url() != null && !selected.getEvent_url().trim().isEmpty()) {
                newUserConcert.setEvent_url(selected.getEvent_url());
            } else {
                newUserConcert.setEvent_url("https://example.com");
            }

            if (selected.getArtist_name() != null && !selected.getArtist_name().trim().isEmpty()) {
                newUserConcert.setArtist_name(selected.getArtist_name());
            } else {
                newUserConcert.setArtist_name("Unknown Artist");
            }

            if (selected.getArtist_id() != null && !selected.getArtist_id().trim().isEmpty()) {
                newUserConcert.setArtist_id(selected.getArtist_id());
            } else {
                newUserConcert.setArtist_id("unknown_artist");
            }

            if (selected.getVenue_name() != null && !selected.getVenue_name().trim().isEmpty()) {
                newUserConcert.setVenue_name(selected.getVenue_name());
            } else {
                newUserConcert.setVenue_name("Unknown Venue");
            }

            if (selected.getVenue_id() != null && !selected.getVenue_id().trim().isEmpty()) {
                newUserConcert.setVenue_id(selected.getVenue_id());
            } else {
                newUserConcert.setVenue_id("unknown_venue");
            }

            if (selected.getVenue_state() != null && !selected.getVenue_state().trim().isEmpty()) {
                newUserConcert.setVenue_state(selected.getVenue_state());
            } else {
                newUserConcert.setVenue_state("Unknown State");
            }

            if (selected.getVenue_city() != null && !selected.getVenue_city().trim().isEmpty()) {
                newUserConcert.setVenue_city(selected.getVenue_city());
            } else {
                newUserConcert.setVenue_city("Unknown City");
            }

            if (selected.getVenue_latitude() != null) {
                newUserConcert.setVenue_latitude(selected.getVenue_latitude());
            } else {
                newUserConcert.setVenue_latitude(0.0);
            }

            if (selected.getVenue_longitude() != null) {
                newUserConcert.setVenue_longitude(selected.getVenue_longitude());
            } else {
                newUserConcert.setVenue_longitude(0.0);
            }

            if (selected.getDescription() != null && !selected.getDescription().trim().isEmpty()) {
                newUserConcert.setDescription(selected.getDescription());
            } else {
                newUserConcert.setDescription("No description available.");
            }

            if (selected.getImageUrl() != null && !selected.getImageUrl().trim().isEmpty()) {
                newUserConcert.setImage_url(selected.getImageUrl());
            } else {
                newUserConcert.setImage_url("/Music/resources/images/DefaultAlbum.png");
            }

            if (selected.getStartDateTime() != null && !selected.getStartDateTime().trim().isEmpty()) {
                userConcertController.convertStartDateTime(selected.getStartDateTime());
            } else {
                userConcertController.convertStartDateTime("2025-01-01");
            }

            newUserConcert.setUserId(userController.getSignedInUser());

            userConcertController.setSelected(newUserConcert);
            // Ask userConcertController to create the new user video in the database
            userConcertController.create();

        } else {
            // No user is signed in
            Methods.showMessage("Information", "Unable to Add!",
                    "To add a concert, a user must have signed in!");
        }
    }

    public void useCurrentLocation() {
        String latStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lat");
        String lngStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lng");

        if (latStr != null && lngStr != null) {
            selectedLatitude = Double.parseDouble(latStr);
            selectedLongitude = Double.parseDouble(lngStr);
            locationSelected = true;
            System.out.println("Received location: " + selectedLatitude + ", " + selectedLongitude);
        } else {
            System.out.println("No location parameters received.");
        }
    }

    //When User selects location on map
    public void onPointSelect(PointSelectEvent event) {
        LatLng latlng = event.getLatLng();
        selectedLatitude = latlng.getLat();
        selectedLongitude = latlng.getLng();
        locationSelected = true;
        // Clear old marker and add new one
        pickLocationMapModel = new DefaultMapModel();
        pickLocationMapModel.addOverlay(new Marker(latlng, "Selected Location"));
    }

    //Performs Concert Search using selected Artist from user list
    public String performArtistSearch() throws Exception{
        nameQuery = userArtistController.getSelected().getName();
        maxResults = "20";
        return performSearch("/userArtist/DataTable?faces-redirect=true");
    }

    //Performs Concert Search using selected Song from user list
    public String performConcertSearchFromUserSong() throws Exception{
        nameQuery = userSongController.getSelected().getArtist_name();
        maxResults = "20";
        return performSearch("/userSong/DataTable?faces-redirect=true");
    }

    //Performs Concert Search using selected Song from Playlist
    public String performConcertSearchFromPlaylistSong() throws Exception{
        nameQuery = playlistSongController.getSelected().getSong_id().getArtist_name();
        maxResults = "20";
        return performSearch("/playlistSong/DataTable?faces-redirect=true");
    }

    //================================
    // Perform Ticketmaster API Song Search by Name Query
    //================================
    public String performSearch(String returnUrlString) throws Exception {
        foundConcerts = null;
        // Replace spaces with +
        String query = nameQuery.replace(" ", "+");
        //We get 20 tracks because they are not sorted by most popular so the actual track user is searching for is usually not first
        String searchApiUrl = "https://app.ticketmaster.com/discovery/v2/events?apikey=" + Constants.TICKETMASTER_API_KEY;
        try {
            /*
        Redirecting to show a Jakarta Faces page involves more than one subsequent requests and
        the messages would die from one request to another if not kept in the Flash scope.
        Since we will redirect to show the search Results page, we invoke preserveMessages().
         */
            Methods.preserveMessages();
            if (nameQuery != null && !nameQuery.isEmpty()) {
                searchApiUrl += "&keyword=" + URLEncoder.encode(nameQuery, "UTF-8");
            }
            // Create a formatter with UTC time zone
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (startDate != null) {
                // Set the time to 00:00:01 manually
                Date adjustedStartDate = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate(), 0, 0, 1);
                searchApiUrl += "&startDateTime=" + URLEncoder.encode(isoFormat.format(adjustedStartDate), "UTF-8");
            }
            if (endDate != null) {
                // Set the time to 23:59:59 manually
                Date adjustedEndDate = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate(), 23, 59, 59);
                searchApiUrl += "&endDateTime=" + URLEncoder.encode(isoFormat.format(adjustedEndDate), "UTF-8");
            }
            if (locationSelected) {
                if (radiusMiles == null || radiusMiles.isEmpty()) {
                    Methods.showMessage("Information", "Ticketmaster API Error!",
                            "If searching with location search radius must be provided!");
                    return "/search/SearchConcert?faces-redirect=true";
                } else {
                    //Ticketmaster requires latitude and longitude to be hashed in a format called geoHas with up to 9 digits of precision
                    String geoHash = GeoHash.withCharacterPrecision(selectedLatitude, selectedLongitude, 9).toBase32();
                    searchApiUrl += "&geoPoint=" + URLEncoder.encode(geoHash, "UTF-8");
                    //default is miles so we don't have to specify units
                    searchApiUrl += "&radius=" + radiusMiles;
                }
            }
            searchApiUrl += "&size=" + maxResults;
            if ((nameQuery == null || nameQuery.isEmpty()) && startDate == null && endDate == null && !locationSelected) {
                throw new IOException("IOException Occurred");
            }
            // Obtain the JSON file from the searchApiUrl
            String searchResultsJsonData = Methods.readUrlContent(searchApiUrl);
            JSONObject root = new JSONObject(searchResultsJsonData);

            // If no events are present, throw IOException
            if (!root.has("_embedded") || !root.getJSONObject("_embedded").has("events")) {
                throw new IOException("No events found in API response");
            }

            JSONArray eventArray = root.getJSONObject("_embedded").getJSONArray("events");
            foundConcerts = new ArrayList<>();

            for (int i = 0; i < eventArray.length(); i++) {
                try {
                    JSONObject eventObj = eventArray.getJSONObject(i);
                    ConcertApi event = new ConcertApi();

                    // Required fields
                    if (!eventObj.has("id") || !eventObj.has("name")) continue;
                    event.setEvent_id(eventObj.getString("id"));
                    event.setEvent_name(eventObj.getString("name"));

                    // URL
                    if (!eventObj.has("url")) continue;
                    event.setEvent_url(eventObj.getString("url"));

                    // Image
                    JSONArray images = eventObj.optJSONArray("images");
                    if (images != null && images.length() > 0) {
                        event.setImageUrl(images.getJSONObject(0).optString("url", ""));
                    }

                    // Start date
                    JSONObject dates = eventObj.optJSONObject("dates");
                    if (dates == null || !dates.has("start")) continue;
                    JSONObject start = dates.getJSONObject("start");
                    String startDateTime = start.optString("dateTime", "");
                    if (startDateTime.isEmpty()) continue;
                    event.setStartDateTime(startDateTime);

                    // Artists
                    JSONArray attractions = eventObj.getJSONObject("_embedded").optJSONArray("attractions");
                    List<String> artistNames = new ArrayList<>();
                    if (attractions != null && attractions.length() > 0) {
                        JSONObject firstArtist = attractions.getJSONObject(0);
                        event.setArtist_name(firstArtist.optString("name", ""));
                        event.setArtist_id(firstArtist.optString("id", ""));

                        for (int j = 0; j < attractions.length(); j++) {
                            artistNames.add(attractions.getJSONObject(j).optString("name", ""));
                        }
                    }

                    // Venue
                    JSONArray venues = eventObj.getJSONObject("_embedded").optJSONArray("venues");
                    if (venues == null || venues.length() == 0) continue;

                    JSONObject venue = venues.getJSONObject(0);
                    event.setVenue_name(venue.optString("name", ""));
                    event.setVenue_id(venue.optString("id", ""));
                    event.setVenue_city(venue.getJSONObject("city").optString("name", ""));
                    event.setVenue_state(venue.getJSONObject("state").optString("stateCode", ""));

                    JSONObject location = venue.optJSONObject("location");
                    if (location == null) continue;
                    event.setVenue_latitude(location.optDouble("latitude", 0));
                    event.setVenue_longitude(location.optDouble("longitude", 0));

                    // Description
                    String description = String.format("%s occurs on %s at %s in %s, %s and features %s.",
                            event.getEvent_name(),
                            event.getFormattedStartDate(),
                            event.getVenue_name(),
                            event.getVenue_city(),
                            event.getVenue_state(),
                            String.join(", ", artistNames)
                    );
                    event.setDescription(description);

                    foundConcerts.add(event);
                } catch (Exception e) {
                    // Skip this event and continue with the next one
                    continue;
                }
            }


        } catch (Exception ex) {
            Methods.showMessage("Information", "Unrecognized Search Query!",
                    "Ticketmaster API provides no data for the search query entered!");

            return "/search/SearchConcert?faces-redirect=true";
        }
        // Reset search queries
        nameQuery = null;
        startDate = null;
        endDate = null;
        //Center of US default
        selectedLatitude = 38.0902;
        selectedLongitude = -95.7129;
        radiusMiles = null;
        maxResults = null;
        locationSelected = false;

        setReturnUrl(returnUrlString);
        return "/search/SearchConcertResults?faces-redirect=true";
    }
}
