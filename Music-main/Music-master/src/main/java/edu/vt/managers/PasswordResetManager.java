package edu.vt.managers;

import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.UserFacade;
import edu.vt.globals.Methods;
import edu.vt.globals.Password;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("passwordResetManager")
@SessionScoped
public class PasswordResetManager implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String username;
    private String password;
    private String confirmPassword;
    private String answerToSecurityQuestion;

    // Used in ResetPassword.xhtml to prevent cybercriminal activity
    private Boolean securityQuestionAnsweredCorrectly = false;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store)
    the object reference of the UserFacade bean into the instance variable
    'userFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserFacade userFacade;

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

    public String getAnswerToSecurityQuestion() {
        return answerToSecurityQuestion;
    }

    public void setAnswerToSecurityQuestion(String answerToSecurityQuestion) {
        this.answerToSecurityQuestion = answerToSecurityQuestion;
    }

    public Boolean getSecurityQuestionAnsweredCorrectly() {
        return securityQuestionAnsweredCorrectly;
    }

    public void setSecurityQuestionAnsweredCorrectly(Boolean securityQuestionAnsweredCorrectly) {
        this.securityQuestionAnsweredCorrectly = securityQuestionAnsweredCorrectly;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //==================================================
    // Process the Username Submitted for Password Reset
    //==================================================
    public String usernameSubmit() {

        securityQuestionAnsweredCorrectly = false;

        // Since we will redirect to show the SecurityQuestion page, invoke preserveMessages()
        Methods.preserveMessages();

        // Obtain the object reference of the User object with username
        User user = userFacade.findByUsername(username);

        if (user == null) {
            Methods.showMessage("Fatal Error", "Unknown Username!",
                    "Entered username " + username + " does not exist!");
            return "";
        } else {
            // Redirect to show the SecurityQuestion page
            return "/userPasswordChange/SecurityQuestion?faces-redirect=true";
        }
    }

    //=========================================
    // Return the Security Question Selected by
    // the User at the Time of Account Creation
    //=========================================
    public String getSelectedSecurityQuestionForUsername() {

        // Obtain the object reference of the User object with username
        User user = userFacade.findByUsername(username);

        // Return the security question selected by the user
        return user.getSecurityQuestion();
    }

    //======================================================
    // Process the Submitted Answer to the Security Question
    //======================================================
    public String securityAnswerSubmit() {

        // Since we will redirect to show the ResetPassword page, invoke preserveMessages()
        Methods.preserveMessages();

        // Obtain the object reference of the User object with username
        User user = userFacade.findByUsername(username);

        String actualSecurityAnswer = user.getSecurityAnswer();
        String enteredSecurityAnswer = getAnswerToSecurityQuestion();

        if (actualSecurityAnswer.equals(enteredSecurityAnswer)) {
            /*
            Answer to the security question is correct. Redirect to show the ResetPassword page.
            */
            securityQuestionAnsweredCorrectly = true;
            return "/userPasswordChange/ResetPassword?faces-redirect=true";

        } else {
            Methods.showMessage("Error",
                    "Answer to the Security Question is Incorrect!", "");
            return "";
        }
    }

    //=========================================================
    // Reset User's Password and Redirect to Show the Home Page
    //=========================================================
    public String resetPassword() {
        /*
        ----------------------------------------------------------------
        Password and Confirm Password are validated under 3 conditions:
        
        <1> Non-empty (tested with required="true" in Jakarta Faces page)
        <2> Correct composition satisfying the regex rule.
            (tested with <f:validator validatorId="passwordValidator" />
            in the Jakarta Faces page)
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

        // Since we will redirect to show the home page, invoke preserveMessages()
        Methods.preserveMessages();

        // Obtain the object reference of the User object with username
        User user = userFacade.findByUsername(username);

        try {
            //-------------------------------------------------------------------------------------
            // Convert the user-entered String password to a String containing the following parts
            //       "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
            // for secure storage and retrieval with Key Stretching to prevent brute-force attacks.
            //-------------------------------------------------------------------------------------
            String parts = Password.createHash(password);

            // Reset User object's password
            user.setPassword(parts);

            // Update the database
            userFacade.edit(user);

            // Initialize the instance variables
            username = password = confirmPassword = answerToSecurityQuestion = "";

            Methods.showMessage("Information", "Success!",
                    "Your Password has been Reset!");

            // Redirect to show the index (home) page
            return "/index?faces-redirect=true";

        } catch (EJBException | Password.CannotPerformOperationException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong while resetting your password!",
                    "See: " + ex.getMessage());
        }
        return "";
    }

}
