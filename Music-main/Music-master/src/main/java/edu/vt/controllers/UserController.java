package edu.vt.controllers;

import edu.vt.EntityBeans.*;
import edu.vt.FacadeBeans.*;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.globals.Password;
import edu.vt.managers.LanguageManager;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Named("userController")
@SessionScoped
public class UserController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String username;
    private String password;
    private String confirmPassword;

    private String firstName;
    private String middleName;
    private String lastName;

    private String address1;
    private String address2;
    private String city;
    private String stateName;
    private String zipcode;

    private String selectedSecurityQuestion;
    private String answerToSecurityQuestion;

    private String email;

    // Two-Factor Authentication is on by sending random code via email
    private Boolean twoFAonViaEmail = false;
    // Two-Factor Authentication is on by sending random code via Short Message Service (SMS) a.k.a. text message
    private Boolean twoFAonViaSMS = false;

    private String cellPhoneNumber = "";
    private String cellPhoneCarrier = "";

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserFacade bean into the instance variable 'userFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserFacade userFacade;

    @EJB
    private UserConcertFacade userConcertFacade;

    @EJB
    private UserArtistFacade userArtistFacade;

    @EJB
    private UserSongFacade userSongFacade;

    @Inject
    private LanguageManager languageManager;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserPhotoFacade bean into the instance variable 'userPhotoFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserPhotoFacade userPhotoFacade;

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getSelectedSecurityQuestion() {
        return selectedSecurityQuestion;
    }

    public void setSelectedSecurityQuestion(String selectedSecurityQuestion) {
        this.selectedSecurityQuestion = selectedSecurityQuestion;
    }

    public String getAnswerToSecurityQuestion() {
        return answerToSecurityQuestion;
    }

    public void setAnswerToSecurityQuestion(String answerToSecurityQuestion) {
        this.answerToSecurityQuestion = answerToSecurityQuestion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getTwoFAonViaEmail() {
        return twoFAonViaEmail;
    }

    public void setTwoFAonViaEmail(Boolean twoFAonViaEmail) {
        this.twoFAonViaEmail = twoFAonViaEmail;
        if (twoFAonViaEmail) {
            this.twoFAonViaSMS = false;
        }
    }

    public Boolean getTwoFAonViaSMS() {
        return twoFAonViaSMS;
    }

    public void setTwoFAonViaSMS(Boolean twoFAonViaSMS) {
        this.twoFAonViaSMS = twoFAonViaSMS;
        if (twoFAonViaSMS) {
            this.twoFAonViaEmail = false;
        }
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public String getCellPhoneCarrier() {
        return cellPhoneCarrier;
    }

    public void setCellPhoneCarrier(String cellPhoneCarrier) {
        this.cellPhoneCarrier = cellPhoneCarrier;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================
    // Return the signed-in User object reference
    //===========================================
    public User getSignedInUser() {
        /*
         "user", the object reference of the signed-in user, was put into the SessionMap
         in the initializeSessionMap() method of LoginManager upon user's sign in.
         */
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        // Return the object reference of the signed-in User object
        return (User) sessionMap.get("user");
    }

    //===================================
    // Return True if a User is Signed In
    //===================================
    public boolean userIsSignedIn() {
        return getSignedInUser() != null;
    }

    //================================
    // Return List of U.S. State Names
    //================================
    public String[] listOfUSStateNames() {
        return Constants.US_STATE_NAMES;
    }

    /*
    *********************************
    Return List of Security Questions
    *********************************
     */
    public String[] securityQuestions() {

        String selectedLanguage = languageManager.getLanguage();

        switch (selectedLanguage)
        {
            case "en":  // English
                return Constants.SECURITY_QUESTIONS_EN;
            case "pt":  // Portuguese
                return Constants.SECURITY_QUESTIONS_PT;
            case "tr":  // Turkish
                return Constants.SECURITY_QUESTIONS_TR;
            default:
                System.out.println("Selected language is out of order!");
        }
        return null;
    }

    //==============================================
    // Process Submitted Answer to Security Question
    //==============================================
    public void securityAnswerSubmit() {
        String actualSecurityAnswer = getSignedInUser().getSecurityAnswer();

        // getAnswerToSecurityQuestion() is the Getter method for the property 'answerToSecurityQuestion'
        String enteredSecurityAnswer = getAnswerToSecurityQuestion();

        if (actualSecurityAnswer.equals(enteredSecurityAnswer)) {
            // Answer to the security question is correct; Delete the user's account.
            // deleteAccount() method is given below.
            deleteAccount();
        } else {
            Methods.showMessage("Error",
                    "Answer to the Security Question is Incorrect!", "");
        }
    }

    //===========================================================
    // Create User's Account and Redirect to Show the SignIn Page
    //===========================================================
    public String createAccount() {
        /*
        ----------------------------------------------------------------
        Password and Confirm Password are validated under 3 tests:
        
        <1> Non-empty (tested with required="true" in Jakarta Faces XHTML page)
        <2> Correct composition satisfying the regex rule.
            (tested with <f:validator validatorId="passwordValidator" /> in the Jakarta Faces XHTML page)
        <3> Password and Confirm Password must match (tested below)
        ----------------------------------------------------------------
         */
        if (!password.equals(confirmPassword)) {
            Methods.showMessage("Fatal Error", "Unmatched Passwords!",
                    "Password and Confirm Password must Match!");
            return "";
        }

        //--------------------------------------------
        // Password and Confirm Password are Validated
        //--------------------------------------------

        /*
        Redirecting to show a Jakarta Faces page involves more than one subsequent requests and
        the messages would die from one request to another if not kept in the Flash scope.
        Since we will redirect to show the SignIn page, we invoke preserveMessages().
         */
        Methods.preserveMessages();

        //-----------------------------------------------------------
        // First, check if the entered username is already being used
        //-----------------------------------------------------------
        // Obtain the object reference of a User object with the username entered by the user
        User aUser = userFacade.findByUsername(username);

        if (aUser != null) {
            // A user already exists with the username entered by the user
            username = "";
            Methods.showMessage("Fatal Error", "Username Already Exists!",
                    "Please Select a Different One!");
            return "";
        }

        //----------------------------------
        // The entered username is available
        //----------------------------------
        try {
            // Instantiate a new User object
            User newUser = new User();

            /*
             Set the properties of the newly created newUser object with the values
             entered by the user in CreateAccount.xhtml
             */
            newUser.setFirstName(firstName);
            newUser.setMiddleName(middleName);
            newUser.setLastName(lastName);
            newUser.setAddress1(address1);
            newUser.setAddress2(address2);
            newUser.setCity(city);
            newUser.setStateName(stateName);
            newUser.setZipcode(zipcode);
            newUser.setSecurityQuestion(selectedSecurityQuestion);
            newUser.setSecurityAnswer(answerToSecurityQuestion);
            newUser.setEmail(email);
            newUser.setUsername(username);

            /*
             Two-Factor Authentication Status:
                 = 0 Off
                 = 1 Send random code via email
                 = 2 Send random code via text message
             */
            if (twoFAonViaEmail) {
                newUser.setTwoFactorAuthenticationStatus(1);  // Send random code via Email
            } else if (twoFAonViaSMS) {
                newUser.setTwoFactorAuthenticationStatus(2);  // Send random code via text message
            } else {
                newUser.setTwoFactorAuthenticationStatus(0);  // 2FA is OFF
            }

            newUser.setCellPhoneNumber(cellPhoneNumber);
            newUser.setCellPhoneCarrier(cellPhoneCarrier);

            /*
            Invoke class Password's createHash() method to convert the user-entered String
            password to a String containing the following parts
                  "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
            for secure storage and retrieval with Key Stretching to prevent brute-force attacks.
             */
            String parts = Password.createHash(password);
            newUser.setPassword(parts);

            // Create the user in the database
            userFacade.create(newUser);

            //-----------------------------
            // Reset the instance variables
            //-----------------------------
            username = "";
            password = "";
            confirmPassword = "";
            firstName = "";
            middleName = "";
            lastName = "";
            address1 = "";
            address2 = "";
            city = "";
            stateName = "";
            zipcode = "";
            selectedSecurityQuestion = "";
            answerToSecurityQuestion = "";
            email = "";
            twoFAonViaEmail = false;
            twoFAonViaSMS = false;
            cellPhoneNumber = "";
            cellPhoneCarrier = "";

        } catch (EJBException | Password.CannotPerformOperationException ex) {
            username = "";
            Methods.showMessage("Fatal Error",
                    "Something went wrong while creating user's account!",
                    "See: " + ex.getMessage());
            return "";
        }

        Methods.showMessage("Information", "Success!",
                "User Account is Successfully Created!");

        return "/signIn/SignIn?faces-redirect=true";
    }

    //==============================================
    // Set 2FA Flags before Showing EditAccount Page
    //==============================================
    public String prepareForEdit() {
        switch(getSignedInUser().getTwoFactorAuthenticationStatus()) {
            // 2FA is turned off
            case 0:
                twoFAonViaEmail = false;
                twoFAonViaSMS = false;
                break;
            // Send 2FA Code via Email
            case 1:
                twoFAonViaEmail = true;
                twoFAonViaSMS = false;
                break;
            // Send 2FA Code via SMS
            case 2:
                twoFAonViaEmail = false;
                twoFAonViaSMS = true;
                break;
            default:
        }
        return "/userAccount/EditAccount?faces-redirect=true";
    }

    //============================================================
    // Update User's Account and Redirect to Show the Profile Page
    //============================================================
    public String updateAccount() {
        // Since we will redirect to show the Profile page, invoke preserveMessages()
        Methods.preserveMessages();
        /*
         Signed-in user's properties are changed directly in EditAccount.xhtml
         */
        try {
            /*
             Two-Factor Authentication Status:
                 = 0 Off
                 = 1 Send random code via email
                 = 2 Send random code via text message
             */
            if (twoFAonViaEmail) {
                getSignedInUser().setTwoFactorAuthenticationStatus(1);  // Send random code via Email
            } else if (twoFAonViaSMS) {
                getSignedInUser().setTwoFactorAuthenticationStatus(2);  // Send random code via text message
            } else {
                getSignedInUser().setTwoFactorAuthenticationStatus(0);  // 2FA is OFF
            }

            // Store the changes in the database
            userFacade.edit(getSignedInUser());

            Methods.showMessage("Information", "Success!",
                    "User's Account is Successfully Updated!");

        } catch (EJBException ex) {
            username = "";
            Methods.showMessage("Fatal Error",
                    "Something went wrong while updating user's profile!",
                    "See: " + ex.getMessage());
            return "";
        }

        // Account update is completed, redirect to show the Profile page.
        return "/userAccount/Profile?faces-redirect=true";
    }

    //==================================================================
    // Delete User's Account, Logout, and Redirect to Show the Home Page
    //==================================================================
    public void deleteAccount() {
        Methods.preserveMessages();
        /*
        The database primary key of the signed-in User object was put into the SessionMap
        in the initializeSessionMap() method of LoginManager upon user's sign in.
         */
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        int userPrimaryKey = (int) sessionMap.get("user_id");

        try {
            // Delete all photo files associated with signed-in user whose primary key is userPrimaryKey
            // deleteAllUserPhotos() is given below
            deleteAllUserPhotos(userPrimaryKey);

            // Delete all user files associated with signed-in user whose primary key is userPrimaryKey
            // deleteAllUserFiles() is given below
            deleteAllUserData(userPrimaryKey);

            // Delete the User entity from the database
            userFacade.deleteUser(userPrimaryKey);

            Methods.showMessage("Information", "Success!",
                    "Your Account is Successfully Deleted!");

        } catch (EJBException ex) {
            username = "";
            Methods.showMessage("Fatal Error",
                    "Something went wrong while deleting user's account!",
                    "See: " + ex.getMessage());
            return;
        }

        // Execute the logout() method given below
        logout();
    }

    //===============================================
    // Logout User and Redirect to Show the Home Page
    //===============================================
    public void logout() {

        // Clear the signed-in User's session map
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.clear();

        // Reset the signed-in User's properties
        username = password = confirmPassword = "";
        firstName = middleName = lastName = "";
        address1 = address2 = city = stateName = zipcode = "";
        selectedSecurityQuestion = answerToSecurityQuestion = email = "";
        cellPhoneNumber = cellPhoneCarrier = "";

        // Since we will redirect to show the home page, invoke preserveMessages()
        Methods.preserveMessages();

        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

            // Invalidate the signed-in User's session
            externalContext.invalidateSession();

            /*
            getRequestContextPath() returns the URI of the webapp directory of the application.
            Obtain the URI of the index (home) page to redirect to.
             */
            String redirectPageURI = externalContext.getRequestContextPath() + "/index.xhtml";

            // Redirect to show the index (home) page
            externalContext.redirect(redirectPageURI);
            /*
            NOTE: We cannot use: return "/index?faces-redirect=true"; here
            because the user's session is invalidated.
             */
        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Unable to redirect to the index (home) page!",
                    "See: " + ex.getMessage());
        }
    }

    //========================================
    // Return Signed-In User's Thumbnail Photo
    //========================================
    public String userPhoto() {
        /*
        The database primary key of the signed-in User object was put into the SessionMap
        in the initializeSessionMap() method of LoginManager upon user's sign in.
         */
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        Integer primaryKey = (Integer) sessionMap.get("user_id");

        List<UserPhoto> photoList = userPhotoFacade.findPhotosByUserPrimaryKey(primaryKey);

        if (photoList.isEmpty()) {
            // No user photo exists. Return defaultUserPhoto.png.
            return Constants.PHOTOS_URI + "defaultUserPhoto.png";
        }

        /*
        photoList.getFirst() returns the object reference of the first Photo object in the list.
        getThumbnailFileName() message is sent to that Photo object to retrieve its
        thumbnail image file name, e.g., 5_thumbnail.jpeg
         */
        String thumbnailFileName = photoList.getFirst().getThumbnailFileName();

        return Constants.PHOTOS_URI + thumbnailFileName;
    }

    //=========================================================
    // Delete the photo, thumbnail, and tempFile that belong to
    // the User object whose database primary key is primaryKey
    //=========================================================
    public void deleteAllUserPhotos(int primaryKey) {
        /*
        Obtain the list of Photo objects that belong to the User whose
        database primary key is primaryKey.
         */
        List<UserPhoto> photoList = userPhotoFacade.findPhotosByUserPrimaryKey(primaryKey);

        // photoList.isEmpty implies that the user has never uploaded a photo file
        if (!photoList.isEmpty()) {

            // Obtain the object reference of the first Photo object in the list.
            UserPhoto photo = photoList.getFirst();
            try {
                /*
                NOTE: Files and Paths are imported as
                        import java.nio.file.Files;
                        import java.nio.file.Paths;

                 Delete the user's photo if it exists. Each user has only one profile photo.
                 getPhotoFilePath() is given in UserPhoto entity class file.
                 */
                Files.deleteIfExists(Paths.get(photo.getPhotoFilePath()));

                /*
                 Delete the user's thumbnail image if it exists. Each user has only one thumbnail photo.
                 getThumbnailFilePath() is given in UserPhoto entity class file.
                 */
                Files.deleteIfExists(Paths.get(photo.getThumbnailFilePath()));

                // Delete the photo file record from the database.
                // UserPhotoFacade inherits the remove() method from AbstractFacade.
                userPhotoFacade.remove(photo);

                /*
                 Delete the user's captured photo file if it exists.
                 The file is named "user's primary key_tempFile".
                 */
                String capturedPhotoFilepath = Constants.PHOTOS_ABSOLUTE_PATH + primaryKey + "_tempFile";
                Files.deleteIfExists(Paths.get(capturedPhotoFilepath));

            } catch (IOException ex) {
                Methods.showMessage("Fatal Error",
                        "Something went wrong while deleting user's photo!",
                        "See: " + ex.getMessage());
            }
        }
    }

    //==========================================================
    // Delete all the artists, concerts, and songs that belong to the User object whose
    // database primary key is primaryKey
    //==========================================================
    public void deleteAllUserData(int primaryKey) {
        //Get list of users Artist
        List<UserArtist> userArtistList = userArtistFacade.findArtistsByUserDatabasePrimaryKey(primaryKey);
        while(!userArtistList.isEmpty()) {
            userArtistFacade.remove(userArtistList.removeFirst());
        }
        //Get list of users concerts
        List<UserConcert> userConcertList = userConcertFacade.findConcertsByUserDatabasePrimaryKey(primaryKey);
        while(!userConcertList.isEmpty()) {
            userConcertFacade.remove(userConcertList.removeFirst());
        }
        //Get List of users songs
        List<UserSong> userSongList = userSongFacade.findSongsByUserDatabasePrimaryKey(primaryKey);
        while(!userSongList.isEmpty()) {
            userSongFacade.remove(userSongList.removeFirst());
        }
    }

}
