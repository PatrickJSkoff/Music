package edu.vt.controllers;

import edu.vt.EntityBeans.Playlist;
import edu.vt.EntityBeans.PlaylistSong;
import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserSong;
import edu.vt.FacadeBeans.PlaylistFacade;
import edu.vt.FacadeBeans.PlaylistSongFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
@Named("playlistSongController")

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
public class PlaylistSongController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    PlaylistSongFacade bean into the instance variable 'playlistSongFacade' after it is instantiated at runtime.
     */
    @EJB
    private PlaylistSongFacade playlistSongFacade;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    PlaylistFacade bean into the instance variable 'playlistFacade' after it is instantiated at runtime.
     */
    @EJB
    private PlaylistFacade playlistFacade;

    @Inject
    private UserSongController userSongController;

    @Inject
    private PlaylistController playlistController;


    // List of object references of Playlist objects
    private List<PlaylistSong> listOfPlaylistSongs = null;

    // selected = object reference of a selected PlaylistSong object
    private PlaylistSong selected;

    // The ID of the selected playlist
    private Integer playlistId;

    // The selected playlist object
    private Playlist selectedPlaylist;

    /*
    ***************************************************************
    Return the List of Playlist Songs of the Selected Playlist Created by the Signed-In User
    ***************************************************************
     */
    public List<PlaylistSong> getListOfPlaylistSongs() {
        if (listOfPlaylistSongs == null && playlistId != null) {
            listOfPlaylistSongs = playlistSongFacade.findPlaylistSongByPlaylistDatabasePrimaryKey(playlistId);
            listOfPlaylistSongs.sort(Comparator.comparing(
                    ps -> ps.getSong_id().getName(), String.CASE_INSENSITIVE_ORDER));
        }
        return listOfPlaylistSongs;
    }

    /*
    ***************************************************************
    Return the List of Playlist Songs for an input playlist
    ***************************************************************
     */
    public List<PlaylistSong> getPlaylistSongList(Playlist playlist) {
        List<PlaylistSong> playlistSongs = null;
        if (playlist != null) {
            playlistSongs = playlistSongFacade.findPlaylistSongByPlaylistDatabasePrimaryKey(playlist.getId());
            playlistSongs.sort(Comparator.comparing(
                    ps -> ps.getSong_id().getName(), String.CASE_INSENSITIVE_ORDER));
        }
        return playlistSongs;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public PlaylistSong getSelected() {
        return selected;
    }

    public void setSelected(PlaylistSong selected) {
        this.selected = selected;
    }

    public Integer getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Integer playlistId) {
        this.playlistId = playlistId;
        this.listOfPlaylistSongs = null;
    }

    public Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public void setSelectedPlaylist(Playlist selectedPlaylist) {
        this.selectedPlaylist = selectedPlaylist;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */
    public String loadPlaylistSongs(int id) {
        this.playlistId = id;
        this.listOfPlaylistSongs = null;
        return "/playlistSong/DataTable?faces-redirect=true";
    }

    //=================================
    // Format the Possessive Users Name of the List
    //=================================
    public String getFormattedPlaylistSongHeader() {
        ResourceBundle bundle = ResourceBundle.getBundle("language",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String pattern = bundle.getString("PlaylistSongHeader");

        // Get the playlist name
        String playlistName = playlistController.getSelected().getName();

        // Format the string with the full name
        return MessageFormat.format(pattern, playlistName);
    }

    //=================================
    // Unselect Selected ParksVisit Object
    //=================================
    public void unselect() {
        selected = null;
    }

    //===========================================
    // Cancel and Display playlistSong/DataTable.xhtml
    //===========================================
    public String cancel() {
        return "/playlistSong/DataTable?faces-redirect=true";
    }

    //===================================================
    // Prepare to Create a New PlaylistSong Object in Database
    //===================================================
    public PlaylistSong prepareCreate() {
        /*
        Instantiate a new PlaylistSong object and store its object reference into
        instance variable 'selected'. The PlaylistSong entity class is defined in PlaylistSong.java
         */
        selected = new PlaylistSong();

        // Set the song to be the selected user song
        selected.setSong_id(userSongController.getSelected());

        return selected;
    }

    //===================================================
    // Prepare to Create a New PlaylistSong Object in Database from the playlist window
    //===================================================
    public PlaylistSong prepareCreateFromPlaylist() {
        /*
        Instantiate a new PlaylistSong object and store its object reference into
        instance variable 'selected'. The PlaylistSong entity class is defined in PlaylistSong.java
         */
        selected = new PlaylistSong();

        // Set the playlist to be the selected playlist
        selected.setPlaylist_id(playlistController.getSelected());

        return selected;
    }

    //=====================================
    // CREATE a New PlaylistSong in the Database
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

        /*
         The object reference of the playlist song to be created is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.CREATE, "Song was Successfully Added to Playlist!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The CREATE operation is successfully performed.
            selected = null;            // Remove selection
            listOfPlaylistSongs = null;     // Invalidate listOfPlaylistSongs to trigger re-query.
        }
    }

    //========================================
    // UPDATE Selected PlaylistSong in the Database
    //========================================
    public void update() {
        /*
         The object reference of the playlist song  to be updated is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.UPDATE, "Playlist Song was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfPlaylistSongs = null; // Invalidate listOfPlaylistSongs to trigger re-query.
        }
    }

    //==========================================
    // DELETE Selected Playlist Song from the Database
    //==========================================
    public void destroy() {
        /*
         The object reference of the user song to be deleted is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.DELETE, "Playlist Song was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The DELETE operation is successfully performed.
            selected = null;            // Remove selection
            listOfPlaylistSongs = null;     // Invalidate list of playlist songs visits to trigger re-query.
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

                     PlaylistSongFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    playlistSongFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     PlaylistSongFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    playlistSongFacade.remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                while (cause != null) {
                    if (cause instanceof java.sql.SQLIntegrityConstraintViolationException
                            || cause.getMessage().toLowerCase().contains("duplicate entry")) {
                        JsfUtil.addErrorMessage("This song is already in the selected playlist.");
                        return;
                    }
                    cause = cause.getCause();
                }
                if (ex.getMessage() != null) {
                    msg = ex.getMessage();
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
