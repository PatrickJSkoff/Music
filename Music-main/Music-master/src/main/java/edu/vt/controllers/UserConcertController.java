package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserConcert;
import edu.vt.FacadeBeans.UserConcertFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import edu.vt.globals.Constants;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
@Named("userConcertController")

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
public class UserConcertController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserConcertFacade bean into the instance variable 'userConcertFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserConcertFacade userConcertFacade;

    @Inject
    private UserController userController;

    //Center of US default
    private Double selectedLatitude = 38.0902;
    private Double selectedLongitude = -95.7129;
    //locationSelected used to check whether a user selected a location and wishes to search by location or if it was left blank
    private Boolean locationSelected = false;
    private String radiusMiles;
    private String maxResults;
    private MapModel pickLocationMapModel;

    // List of object references of UserConcert objects
    private List<UserConcert> listOfConcerts = null;

    // selected = object reference of a selected Country object
    private UserConcert selected;

    private java.util.Date startDateTime;

    /*
    ***************************************************************
    Return the List of Concerts Created by the Signed-In User
    ***************************************************************
     */
    public List<UserConcert> getListOfConcerts() {
        if (listOfConcerts == null) {
            /*
            user_id (database primary key) was put into the SessionMap in the
            initializeSessionMap() method in LoginManager upon user's sign in.
             */
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            int userPrimaryKey = (int) sessionMap.get("user_id");

            listOfConcerts = userConcertFacade.findConcertsByUserDatabasePrimaryKey(userPrimaryKey);
            listOfConcerts.sort(Comparator.comparing(UserConcert::getEvent_name));
        }
        return listOfConcerts;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public UserConcert getSelected() {
        return selected;
    }

    public void setSelected(UserConcert selected) {
        this.selected = selected;
    }

    // This Getter method is called in Create.xhtml and Edit.xhtml
    public Date getStartDateTime() {
        if (selected != null) {
            if (selected.getStartDateTime() != null) {
                // Convert date from SQL type to Java type to display in XHTML file
                startDateTime = new Date(selected.getStartDateTime().getTime());
                return startDateTime;
            }
        }
        // Return today's date
        return new Date();
    }

    // This Setter method is called in Create.xhtml and Edit.xhtml
    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
        if (selected != null) {
            // Convert Date from Java type to SQL type for database storage
            java.sql.Date dateSqlType = new java.sql.Date(startDateTime.getTime());

            // Set selected Movie's release date in SQL type
            selected.setStartDateTime(dateSqlType);
        }
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //================================================
    // Converts Date Published from String to SQL Date
    //================================================
    public void convertStartDateTime(String startDateTime) {
        try {
            // Create a date formatter with the database date format
            SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");

            // Convert the date from String type to java.util.Date type
            java.util.Date utilDate = dateFormater.parse(startDateTime);

            // Convert java.util.Date type to java.sql.Date type
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            selected.setStartDateTime(sqlDate);

        } catch (ParseException e) {
            selected.setStartDateTime(null);
        }
    }

    public String getFormattedUserConcertHeader() {
        ResourceBundle bundle = ResourceBundle.getBundle("language",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String pattern = bundle.getString("UserConcertsHeader");

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
    // Unselect Selected UserConcert Object
    //=================================
    public void unselect() {
        selected = null;
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

    //===========================================
    // Cancel and Display userConcert/DataTable.xhtml
    //===========================================
    public String cancel() {
        return "/userConcert/DataTable?faces-redirect=true";
    }

    //===================================================
    // Prepare to Create a New UserConcert Object in Database
    //===================================================
    public UserConcert prepareCreate() {
        /*
        Instantiate a new UserConcert object and store its object reference into
        instance variable 'selected'. The UserConcert entity class is defined in UserConcert.java
         */
        selected = new UserConcert();

        // Obtain the signed-in user's object reference that was placed in the
        // sessionMap in initializeSessionMap() method of LoginManager
        User signedInUser = (User) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get("user");

        // Set selected to belong to the signed-in user
        selected.setUserId(signedInUser);

        return selected;
    }

    //=====================================
    // CREATE a New User Concert in the Database
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
         The object reference of the user concert to be created is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.CREATE, "User Concert was Successfully Created!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The CREATE operation is successfully performed.
            selected = null;            // Remove selection
            listOfConcerts = null;     // Invalidate listOfConcerts to trigger re-query.
        }
    }

    //========================================
    // UPDATE Selected User Concert in the Database
    //========================================
    public void update() {
        /*
         The object reference of the user concert to be updated is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.UPDATE, "User Concert was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfConcerts = null; // Invalidate listOfConcerts to trigger re-query.
        }
    }

    //==========================================
    // DELETE Selected User Concert from the Database
    //==========================================
    public void destroy() {
        /*
         The object reference of the user concert to be deleted is stored in the instance variable 'selected'
         See the persist method below.
         */
        persist(PersistAction.DELETE, "User Concert was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No Jakarta Faces validation error. The DELETE operation is successfully performed.
            selected = null;            // Remove selection
            listOfConcerts = null;     // Invalidate list of concerts visits to trigger re-query.
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

                     UserConcertFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    userConcertFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     UserConcertFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    userConcertFacade.remove(selected);
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
