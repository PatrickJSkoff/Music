package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserArtistAlbum;
import edu.vt.FacadeBeans.UserArtistAlbumFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
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
@Named("userArtistAlbumController")

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
public class UserArtistAlbumController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserSongFacade bean into the instance variable 'userSongFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserArtistAlbumFacade userArtistAlbumFacade;

    @Inject
    private UserController userController;

    @Inject
    private UserArtistController userArtistController;


    // List of object references of UserSong objects
    private List<UserArtistAlbum> listOfArtistAlbums = null;

    // selected = object reference of a selected Country object
    private UserArtistAlbum selected;

    /*
    ***************************************************************
    Return the List of Songs Created by the Signed-In User
    ***************************************************************
     */
    public List<UserArtistAlbum> getListOfArtistAlbums() {
        if (listOfArtistAlbums == null) {
            /*
            user_id (database primary key) was put into the SessionMap in the
            initializeSessionMap() method in LoginManager upon user's sign in.
             */

            listOfArtistAlbums = userArtistAlbumFacade.findByUserArtistId(userArtistController.getLastArtist().getId());
            listOfArtistAlbums.sort(Comparator.comparing(UserArtistAlbum::getName));
        }
        return listOfArtistAlbums;
    }

    public List<UserArtistAlbum> getAlbumsFromSelectedArtist() {
        listOfArtistAlbums = new ArrayList<UserArtistAlbum>();
        if(userArtistController.getSelected() == null){
            return listOfArtistAlbums;
        }
        listOfArtistAlbums = userArtistAlbumFacade.findByUserArtistId(userArtistController.getSelected().getId());
        listOfArtistAlbums.sort(Comparator.comparing(UserArtistAlbum::getName));
        return listOfArtistAlbums;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public UserArtistAlbum getSelected() {
        return selected;
    }

    public void setSelected(UserArtistAlbum selected) {
        this.selected = selected;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

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
        return "/userArtist/DataTable?faces-redirect=true";
    }

    //===================================================
    // Prepare to Create a New UserSong Object in Database
    //===================================================
    public UserArtistAlbum prepareCreate() {
        /*
        Instantiate a new UserSong object and store its object reference into
        instance variable 'selected'. The UserSong entity class is defined in UserSong.java
         */
        selected = new UserArtistAlbum();

        return selected;
    }

    //=====================================
    // CREATE a New User Song in the Database
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
         The object reference of the user song to be created is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.CREATE, "Album Successfully added!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The CREATE operation is successfully performed.
            selected = null;            // Remove selection
            listOfArtistAlbums = null;     // Invalidate listOfSongs to trigger re-query.
        }
    }

    //========================================
    // UPDATE Selected User Song in the Database
    //========================================
    public void update() {
        /*
         The object reference of the user song to be updated is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.UPDATE, "User Artist was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfArtistAlbums = null; // Invalidate listOfSongs to trigger re-query.
        }
    }

    //==========================================
    // DELETE Selected User Song from the Database
    //==========================================
    public void destroy() {
        /*
         The object reference of the user song to be deleted is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.DELETE, "Artist's album was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The DELETE operation is successfully performed.
            selected = null;            // Remove selection
            listOfArtistAlbums = null;     // Invalidate list of songs visits to trigger re-query.
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

                     UserSongFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    userArtistAlbumFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     UserSongFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    userArtistAlbumFacade.remove(selected);
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
