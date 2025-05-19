package edu.vt.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 *
 * @author Balci
 */
/*
The @FacesValidator annotation on a class automatically registers the class with the runtime as a Validator. 
The "durationValidator" attribute is the validator-id used in the JSF facelets page within

    <f:validator validatorId="durationValidator" /> 

to invoke the "validate" method of the annotated class given below.                           
*/
@FacesValidator("durationValidator")

public class DurationValidator implements Validator {
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        
        // Type cast the inputted "value" to duration of type String
        String duration = (String) value;

        if (duration == null || duration.isEmpty()) {
            // Do not take any action. 
            // The required="true" in the XHTML file will catch this and produce an error message.
            return;
        }
        
        /* REGular EXpression (regex) for validating the duration

            ^       start of regex
            HH      Minimum 00 Maximum 99 hours
            MM      Minimum 00 Maximum 59 minutes
            SS      Minimum 00 Maximum 59 seconds
            $       end of regex
        */
        
        // REGular EXpression (regex) to validate the duration entered
        String regex = "^[0-9][0-9]:[0-5][0-9]:[0-5][0-9]$";
        
        if (!duration.matches(regex)) {
            throw new ValidatorException(new FacesMessage("The HOURS must be minimum 00 and maximum 99, "
                    + "The MINUTES must be minimum 00 and maximum 59, "
                    + "The SECONDS must be minimum 00 and maximum 59."));
        } 
    } 
    
}
