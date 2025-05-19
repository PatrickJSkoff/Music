package edu.vt.galleria;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Named(value = "photoService")
@ApplicationScoped
public class PhotoService {
    /*
    ============================
    Instance Variable (Property)
    ============================
     */
    private List<Photo> listOfPhotos;

    /*
    The PostConstruct annotation is used on a method that needs to be executed after
    dependency injection is done to perform any initialization. The initialization
    method init() is the first method invoked before this class is put into service.
     */
    @PostConstruct
    public void init() {
        listOfPhotos = new ArrayList<>();

        listOfPhotos.add(new Photo("/resources/images/photos/photo1.jpg", "/resources/images/photos/photo1s.jpg",
                "Description for Photo 1", "A Country Music Concert at a venue in Martinsville, Virginia."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo2.jpg", "/resources/images/photos/photo2s.jpg",
                "Description for Photo 2", "George Strait, who's known for reviving interest in 1930s and 40s honky-tonk and western swing music, performs on his live music tour in 2025."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo3.jpg", "/resources/images/photos/photo3s.jpg",
                "Description for Photo 3", "Spotify's app allows users to stream their favorite music on their mobile devices."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo4.jpg", "/resources/images/photos/photo4s.jpg",
                "Description for Photo 4", "The Ticketmaster mobile app allows users to purchase tickets to music, sports, and other events."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo5.jpg", "/resources/images/photos/photo5s.jpg",
                "Description for Photo 5", "Alan Jackson, multi-platinum-selling, award-winning country music singer and songwriter, performs at Aquapalooza in 2009 at Lake Martin, Alabama."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo6.jpg", "/resources/images/photos/photo6s.jpg",
                "Description for Photo 6", "Charlie Daniels Band, a pioneering contributor to Southern rock and progressive country, performs in front a live crowd."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo7.jpg", "/resources/images/photos/photo7s.jpg",
                "Description for Photo 7", "Conway Twitty on Stage in the 1970s – A legendary country performance capturing the energy and style of a bygone era."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo8.jpg", "/resources/images/photos/photo8s.jpg",
                "Description for Photo 8", "The Historic Ryman Auditorium – Known as the “Mother Church of Country Music,” this iconic venue stands at the heart of Nashville’s music legacy."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo9.jpg", "/resources/images/photos/photo9s.jpg",
                "Description for Photo 9", "Luke Combs Live at Lincoln Financial Field – A high-energy stadium concert showcasing one of today’s biggest country stars."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo10.jpg", "/resources/images/photos/photo10s.jpg",
                "Description for Photo 10", "Metallica Live at Lane Stadium – Virginia Tech – A bold event flyer promoting Metallica’s highly anticipated campus performance."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo11.jpg", "/resources/images/photos/photo11s.jpg",
                "Description for Photo 11", "Artist Page on the Music App – Displays albums and cover art retrieved from Spotify for a selected artist, enhancing music discovery."));
        listOfPhotos.add(new Photo("/resources/images/photos/photo12.jpg", "/resources/images/photos/photo12s.jpg",
                "Description for Photo 12", "Concert Directions via the Music App – Turn-by-turn route guidance to a selected venue using real-time Ticketmaster concert data."));
    }

    /*
    =============
    Getter Method
    =============
     */
    public List<Photo> getListOfPhotos() {
        return listOfPhotos;
    }

    /*
    ===============
    Instance Method
    ===============
    */
    public String imageCaption(Photo aPhoto) {

        // Obtain photo number 1 to 12
        int index = listOfPhotos.indexOf(aPhoto) + 1;

        // Define the bundle key value
        String bundleKey = "Caption" + index;

        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "bundle");

        return bundle.getString(bundleKey);
    }
}
