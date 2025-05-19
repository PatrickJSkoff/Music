package edu.vt.controllers;

import edu.vt.EntityBeans.Playlist;
import edu.vt.EntityBeans.UserSong;
import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserPhoto;
import edu.vt.FacadeBeans.PlaylistFacade;
import edu.vt.FacadeBeans.UserSongFacade;
import edu.vt.FacadeBeans.UserPhotoFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
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
@Named("userSongController")

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
public class UserSongController implements Serializable, Converter {
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
    private UserSongFacade userSongFacade;

    @Inject
    private UserController userController;


    // List of object references of UserSong objects
    private List<UserSong> listOfSongs = null;

    // selected = object reference of a selected Country object
    private UserSong selected;

    /*
    ***************************************************************
    Return the List of Songs Created by the Signed-In User
    ***************************************************************
     */
    public List<UserSong> getListOfSongs() {
        if (listOfSongs == null) {
            /*
            user_id (database primary key) was put into the SessionMap in the
            initializeSessionMap() method in LoginManager upon user's sign in.
             */
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            int userPrimaryKey = (int) sessionMap.get("user_id");

            listOfSongs = userSongFacade.findUserQuestionnairesByUserPrimaryKey(userPrimaryKey);
            listOfSongs.sort(Comparator.comparing(UserSong::getName));
        }
        return listOfSongs;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public UserSong getSelected() {
        return selected;
    }

    public void setSelected(UserSong selected) {
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
        return userSongFacade.find(Integer.valueOf(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof UserSong)) return "";
        return ((UserSong) value).getId().toString();
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */
    //=================================
    // Format the Possessive Users Name of the List
    //=================================
    public String getFormattedUserSongsHeader() {
        ResourceBundle bundle = ResourceBundle.getBundle("language",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String pattern = bundle.getString("UserSongsHeader");

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
    // Format the Duration
    //=================================
    public String getFormattedDuration(String duration) {
        try {
            String[] parts = duration.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            if (hours > 0) {
                return String.format("%d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            return duration; // fallback
        }
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
        return "/userSong/DataTable?faces-redirect=true";
    }

    //===================================================
    // Prepare to Create a New UserSong Object in Database
    //===================================================
    public UserSong prepareCreate() {
        /*
        Instantiate a new UserSong object and store its object reference into
        instance variable 'selected'. The UserSong entity class is defined in UserSong.java
         */
        selected = new UserSong();

        // Obtain the signed-in user's object reference that was placed in the
        // sessionMap in initializeSessionMap() method of LoginManager
        User signedInUser = (User) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get("user");

        // Set selected to belong to the signed-in user
        selected.setUserId(signedInUser);

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
        persist(PersistAction.CREATE, "User Song was Successfully Created!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The CREATE operation is successfully performed.
            selected = null;            // Remove selection
            listOfSongs = null;     // Invalidate listOfSongs to trigger re-query.
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
        persist(PersistAction.UPDATE, "User Song was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfSongs = null; // Invalidate listOfSongs to trigger re-query.
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
        persist(PersistAction.DELETE, "User Song was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The DELETE operation is successfully performed.
            selected = null;            // Remove selection
            listOfSongs = null;     // Invalidate list of songs visits to trigger re-query.
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
                    userSongFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     UserSongFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    userSongFacade.remove(selected);
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
