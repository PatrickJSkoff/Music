package edu.vt.controllers;

import edu.vt.EntityBeans.Playlist;
import edu.vt.EntityBeans.PlaylistSong;
import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserSong;
import edu.vt.FacadeBeans.PlaylistFacade;
import edu.vt.FacadeBeans.UserSongFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
----------------------------------------------------------------------------
The @Named (jakarta.inject.Named) annotation indicates that the objects
instantiated from this class will be managed by the Contexts and Dependency
Injection (CDI) container. The name "ParkVisitController" is used within
Expression Language (EL) expressions in Jakarta Faces (XHTML) facelets pages
to access the properties and invoke methods of this class.
----------------------------------------------------------------------------
 */
@Named("playlistController")

/*
The @SessionScoped annotation preserves the values of the ParkVisitController
object's instance variables across multiple HTTP request-response cycles
as long as the user's established HTTP session is alive.
 */
@SessionScoped

/*
-----------------------------------------------------------------------------
Marking the ParkVisitController class as "implements Serializable" implies that
instances of the class can be automatically serialized and deserialized.

Serialization is the process of converting a class instance (object)
from memory into a suitable format for storage in a file or memory buffer,
or for transmission across a network connection link.

Deserialization is the process of recreating a class instance (object)
in memory from the format under which it was stored.
-----------------------------------------------------------------------------
 */
public class PlaylistController implements Serializable, Converter {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    PlaylistFacade bean into the instance variable 'playlistFacade' after it is instantiated at runtime.
     */
    @EJB
    private PlaylistFacade playlistFacade;

    @Inject
    private UserController userController;


    // List of object references of Playlist objects
    private List<Playlist> listOfPlaylist = null;

    // selected = object reference of a selected Playlist object
    private Playlist selected;
    @Named("playlistSongController")
    @Inject
    private PlaylistSongController playlistSongController;

    /*
    ***************************************************************
    Return the List of Playlist Created by the Signed-In User
    ***************************************************************
     */
    public List<Playlist> getListOfPlaylist() {
        if (listOfPlaylist == null) {
            /*
            user_id (database primary key) was put into the SessionMap in the
            initializeSessionMap() method in LoginManager upon user's sign in.
             */
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            int userPrimaryKey = (int) sessionMap.get("user_id");

            listOfPlaylist = playlistFacade.findPlaylistByUserDatabasePrimaryKey(userPrimaryKey);
            listOfPlaylist.sort(Comparator.comparing(Playlist::getName));
        }
        return listOfPlaylist;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public Playlist getSelected() {
        return selected;
    }

    public void setSelected(Playlist selected) {
        this.selected = selected;
    }

    /*
     *********************************
     *   Converter Methods   *
     *********************************
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) return null;
        return playlistFacade.find(Integer.valueOf(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof Playlist)) return "";
        return ((Playlist) value).getId().toString();
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */
    //=================================
    // Format the Possessive Users Name of the List
    //=================================
    public String getFormattedPlaylistHeader() {
        ResourceBundle bundle = ResourceBundle.getBundle("language",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String pattern = bundle.getString("PlaylistHeader");

        // Get the user's first and last name
        String firstName = userController.getSignedInUser().getFirstName();
        String lastName = userController.getSignedInUser().getLastName();

        String currentLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();

        String fullName = firstName + " " + lastName;

        // Only add "'s" if the language is English
        if ("en".equals(currentLanguage)) {
            if (!lastName.endsWith("s")) {
                fullName = fullName + "'s";
            } else {
                fullName = fullName + "'";
            }
        }
        else if ("tr".equals(currentLanguage)) {
            fullName += "'ın";
        }

        // Format the string with the full name
        return MessageFormat.format(pattern, fullName);
    }

    //=================================
    // Unselect Selected ParksVisit Object
    //=================================
    public void unselect() {
        selected = null;
    }

    //===========================================
    // Cancel and Display userSong/DataTable.xhtml
    //===========================================
    public String cancel() {
        return "/playlist/DataTable?faces-redirect=true";
    }

    //===========================================
    // Create a composite playlist cover
    //===========================================
    public String createCompositePlaylistCover(List<String> imageUrls, int size) {
        try {
            int grid = 2;
            int tileSize = size / grid;

            BufferedImage collage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = collage.createGraphics();

            for (int i = 0; i < Math.min(imageUrls.size(), 4); i++) {
                BufferedImage img = ImageIO.read(new URL(imageUrls.get(i)));
                Image scaled = img.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);

                int row = i / grid;
                int col = i % grid;
                g.drawImage(scaled, col * tileSize, row * tileSize, null);
            }

            g.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(collage, "jpg", output);
            byte[] imageBytes = output.toByteArray();

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //===========================================
    // Get the cover art for a playlist
    //===========================================
    public List<String> getCoverArtUrlsForPlaylist(Playlist playlist) {
        List<String> urls = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (playlist != null) {
            List<PlaylistSong> songs = playlistSongController.getPlaylistSongList(playlist);
            if (songs != null) {
                for (PlaylistSong ps : songs) {
                    String url = ps.getSong_id().getAlbum_artwork_url();
                    if (url != null && !url.isBlank() && seen.add(url)) {
                        urls.add(url);
                        if (urls.size() == 4) break;
                    }
                }
            }
        }

        return urls;
    }

    //===========================================
    // Get the correct cover for a playlist based on number of songs
    //===========================================
    public String getPlaylistCoverImage(Playlist playlist) {
        List<String> urls = getCoverArtUrlsForPlaylist(playlist);
        if (urls.isEmpty()) {
            return "/Music/resources/images/DefaultAlbum.png";
        }
        else if (urls.size() < 4) {
            return urls.getFirst();
        }
        return createCompositePlaylistCover(urls, 600);
    }

    //===================================================
    // Prepare to Create a New Playlist Object in Database
    //===================================================
    public Playlist prepareCreate() {
        /*
        Instantiate a new Playlist object and store its object reference into
        instance variable 'selected'. The Playlist entity class is defined in Playlist.java
         */
        selected = new Playlist();

        // Obtain the signed-in user's object reference that was placed in the
        // sessionMap in initializeSessionMap() method of LoginManager
        User signedInUser = (User) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get("user");

        // Set selected to belong to the signed-in user
        selected.setUserId(signedInUser);

        return selected;
    }

    //=====================================
    // CREATE a New Playlist in the Database
    //=====================================
    public void create() {
        /*
        An enum is a special Java type used to define a group of constants.
        The constants CREATE, DELETE and UPDATE are defined as follows in JsfUtil.java

                public enum PersistAction {
                    CREATE,
                    DELETE,
                    UPDATE
                }
         */

        if (selected.getImage_url() == null || selected.getImage_url().isEmpty()) {
            selected.setImage_url("/Music/resources/images/DefaultPlaylist.png");
        }

        /*
         The object reference of the playlist to be created is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.CREATE, "Playlist was Successfully Created!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The CREATE operation is successfully performed.
            selected = null;            // Remove selection
            listOfPlaylist = null;     // Invalidate listOfPlaylist to trigger re-query.
        }
    }

    //========================================
    // UPDATE Selected Playlist in the Database
    //========================================
    public void update() {
        /*
         The object reference of the playlist to be updated is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.UPDATE, "Playlist was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfPlaylist = null; // Invalidate listOfPlaylist to trigger re-query.
        }
    }

    //==========================================
    // DELETE Selected Playlist from the Database
    //==========================================
    public void destroy() {
        /*
         The object reference of the user song to be deleted is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.DELETE, "Playlist was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The DELETE operation is successfully performed.
            selected = null;            // Remove selection
            listOfPlaylist = null;     // Invalidate list of playlist visits to trigger re-query.
        }
    }

    /*
     **********************************************************************************************
     *   Perform CREATE, UPDATE (EDIT), and DELETE (DESTROY, REMOVE) Operations in the Database   *
     **********************************************************************************************
     */

    /**
     * @param persistAction  refers to CREATE, UPDATE (Edit) or DELETE action
     * @param successMessage displayed to inform the user about the result
     */
    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    /*
                     -------------------------------------------------
                     Perform CREATE or EDIT operation in the database.
                     -------------------------------------------------
                     The edit(selected) method performs the SAVE (STORE) operation of the "selected"
                     object in the database regardless of whether the object is a newly
                     created object (CREATE) or an edited (updated) object (EDIT or UPDATE).

                     PlaylistFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    playlistFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     PlaylistFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    playlistFacade.remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (!msg.isEmpty()) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, "A persistence error occurred!");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, "A persistence error occurred");
            }
        }
    }
}
