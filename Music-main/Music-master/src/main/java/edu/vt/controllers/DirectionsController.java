package edu.vt.controllers;

import edu.vt.globals.Constants;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Named("directionsController")
@ViewScoped
public class DirectionsController implements Serializable {

    private String startLocation;
    private String directionsUrl;
    private String mode;

    @Inject
    private ApiSearchByConcertController apiConcertController;
    @Inject
    private UserConcertController userConcertController;

    public void drivingDirections(){
        mode = "driving";
        buildDirectionsUrl();
    }

    public void walkDirections(){
        mode = "walking";
        buildDirectionsUrl();
    }

    public void bikeDirections(){
        mode = "bicycling";
        buildDirectionsUrl();
    }

    public void transitDirections(){
        mode = "transit";
        buildDirectionsUrl();
    }

    public void buildDirectionsUrl() {
        if (apiConcertController.getSelected() != null) {
            try {
                String encodedStart = URLEncoder.encode(startLocation, StandardCharsets.UTF_8.toString());
                String destination = apiConcertController.getSelected().getVenue_latitude() + "," + apiConcertController.getSelected().getVenue_longitude();

                directionsUrl = "https://www.google.com/maps/embed/v1/directions?key=" + Constants.GOOGLE_API_KEY
                        + "&origin=" + encodedStart
                        + "&destination=" + destination
                        + "&mode=" + mode;

            } catch (Exception e) {
                e.printStackTrace();
                directionsUrl = "";
            }
        }
        else if (userConcertController.getSelected() != null) {
            try {
                String encodedStart = URLEncoder.encode(startLocation, StandardCharsets.UTF_8.toString());
                String destination = userConcertController.getSelected().getVenue_latitude() + "," + userConcertController.getSelected().getVenue_longitude();

                directionsUrl = "https://www.google.com/maps/embed/v1/directions?key=" + Constants.GOOGLE_API_KEY
                        + "&origin=" + encodedStart
                        + "&destination=" + destination
                        + "&mode=" + mode;

            } catch (Exception e) {
                e.printStackTrace();
                directionsUrl = "";
            }
        }
    }

    // Getters and Setters

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDirectionsUrl() {
        return directionsUrl;
    }

    public void setDirectionsUrl(String directionsUrl) {
        this.directionsUrl = directionsUrl;
    }
}
