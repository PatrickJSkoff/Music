package edu.vt.pojo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// This class provides a Plain Old Java Object (POJO) representing a Concert returned from the API
public class ConcertApi {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String event_id;
    private String event_name;
    private String event_url;
    private String artist_name;
    private String artist_id;
    private String venue_name;
    private String venue_id;
    private String venue_state;
    private String venue_city;
    private Double venue_latitude;
    private Double venue_longitude;
    private String description;
    private String startDateTime;
    private String imageUrl;

    // Constructor Method
    public ConcertApi(){

    }



    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */
    public String getFormattedStartDate() {
        if (startDateTime == null || startDateTime.isEmpty()) {
            return "";
        }
        ZonedDateTime zdt = ZonedDateTime.parse(startDateTime);
        return zdt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_url() {
        return event_url;
    }

    public void setEvent_url(String event_url) {
        this.event_url = event_url;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

    public String getVenue_name() {
        return venue_name;
    }

    public void setVenue_name(String venue_name) {
        this.venue_name = venue_name;
    }

    public String getVenue_id() {
        return venue_id;
    }

    public void setVenue_id(String venue_id) {
        this.venue_id = venue_id;
    }

    public String getVenue_state() {
        return venue_state;
    }

    public void setVenue_state(String venue_state) {
        this.venue_state = venue_state;
    }

    public String getVenue_city() {
        return venue_city;
    }

    public void setVenue_city(String venue_city) {
        this.venue_city = venue_city;
    }

    public Double getVenue_latitude() {
        return venue_latitude;
    }

    public void setVenue_latitude(Double venue_latitude) {
        this.venue_latitude = venue_latitude;
    }

    public Double getVenue_longitude() {
        return venue_longitude;
    }

    public void setVenue_longitude(Double venue_longitude) {
        this.venue_longitude = venue_longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }
}
