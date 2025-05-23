package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserPhoto;
import edu.vt.FacadeBeans.UserPhotoFacade;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.imgscalr.Scalr;
import org.primefaces.event.CaptureEvent;
import org.primefaces.model.file.UploadedFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Named("userPhotoController")
@SessionScoped
public class UserPhotoController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */
    private String filename;
    private UploadedFile file;

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
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //=============================
    // Return Captured Photo's Path
    //=============================
    public String capturedPhoto() {
        return Constants.PHOTOS_URI + filename;
    }

    //==================================
    // Handle User Profile Photo Capture
    //==================================
    public void onCapture(CaptureEvent captureEvent) {
        
        // Obtain the object reference of the signed-in User object
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        
        /*
        We use the filename "userPrimaryKey_tempFile" to store the signed-in
        user's photo captured with the web camera. Using the same file for multiple
        users would create file locking issues, which increase complexity.
         */
        filename = signedInUser.getId() + "_tempFile";

        String absolutePathOfFilename = Constants.PHOTOS_ABSOLUTE_PATH + filename;

        // Create a new File object with absolute path of 'filename'
        File capturedPhotoTemporaryFile = new File(absolutePathOfFilename);

        // The class FileImageOutputStream enables writing its output directly to a File 
        FileImageOutputStream imageOutput;

        try {
            // Obtain the captured photo image data as a stream of bytes
            byte[] capturedPhotoImageData = captureEvent.getData();

            // Instantiate a new FileImageOutputStream object for the temporary file
            imageOutput = new FileImageOutputStream(capturedPhotoTemporaryFile);

            // Write the capturedPhotoImageData byte stream into the temporary file
            imageOutput.write(capturedPhotoImageData, 0, capturedPhotoImageData.length);

            // Close the temporary file
            imageOutput.close();

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Unable to write captured photo image!",
                    "See: " + ex.getMessage());
            return;
        }

        // Delete signed-in user's uploaded photo file, its thumbnail file, and its database record.
        deletePhoto();

        // Construct a new Photo object with PNG file extension and user's object reference
        UserPhoto newPhoto = new UserPhoto("png", signedInUser);

        // Create a record for the new Photo object in the database
        userPhotoFacade.create(newPhoto);

        // Obtain the object reference of the first and only Photo object of the
        // user whose primary key is signedInUser.getId()
        UserPhoto photo = userPhotoFacade.findPhotosByUserPrimaryKey(signedInUser.getId()).getFirst();

        try {
            // Create a new File object for temporary file using its absolute filepath
            File photoTempFile = new File(absolutePathOfFilename);

            // Convert the captured photo stored in the temporary file into an inputStream
            InputStream inputStream = new FileInputStream(photoTempFile);

            // Write the captured photo's input stream of bytes under the photo object's
            // filename using the inputStreamToFile method given below
            File capturedPhotoFile = inputStreamToFile(inputStream, photo.getPhotoFilename());

            // Create and save the thumbnail version of the captured photo file
            saveThumbnail(capturedPhotoFile, photo);

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Unable to convert temp file into input stream!",
                    "See: " + ex.getMessage());
        }
    }

    //================================
    // Redo User Profile Photo Capture
    //================================
    public String redo() {
        filename = "";
        return "/userPhoto/ChangePhoto?faces-redirect=true";
    }

    //=================================
    // Handle User Profile Photo Upload
    //=================================
    public String upload() {
        /*
        Redirecting to show a Jakarta Faces page involves more than one subsequent requests and
        the messages would die from one request to another if not kept in the Flash scope.
        Since we will redirect to show the Profile page, we invoke preserveMessages().
         */
        Methods.preserveMessages();

        // Check if a file is selected
        if (file.getSize() == 0) {
            Methods.showMessage("Information", "No File Selected!",
                    "You need to choose a file first before clicking Upload.");
            return "";
        }

        /*
        MIME (Multipurpose Internet Mail Extensions) is a way of identifying files on
        the Internet according to their nature and format. 

        A "Content-type" is simply a header defined in many protocols, such as HTTP, that 
        makes use of MIME types to specify the nature of the file currently being handled.

        Some MIME file types: (See http://www.freeformatter.com/mime-types-list.html)

            JPEG Image      = image/jpeg or image/jpg
            PNG image       = image/png
            GIF image       = image/gif
            Plain text file = text/plain
            HTML file       = text/html
            JSON file       = application/json

         Some of the MIME type mappings are specified in web.xml
         */
        // Obtain the uploaded file's MIME file type
        String mimeFileType = file.getContentType();

        if (mimeFileType.startsWith("image/")) {
            // The uploaded file is an image file
            /*
            The subSequence() method returns the portion of the mimeFileType string from the 6th
            position to the last character. Note that it starts with "image/" which has 6 characters at
            positions 0,1,2,3,4,5. Therefore, we start the subsequence at position 6 to obtain the file extension.
             */
            String fileExtension = mimeFileType.subSequence(6, mimeFileType.length()).toString();

            String fileExtensionInCaps = fileExtension.toUpperCase();

            switch (fileExtensionInCaps) {
                case "JPG", "JPEG", "PNG", "GIF" -> {
                    // File is an acceptable image type
                }
                default -> {
                    Methods.showMessage("Fatal Error", "Unrecognized File Type!",
                            "Selected file type is not a JPG, JPEG, PNG, or GIF!");
                    return "";
                }
            }
        } else {
            Methods.showMessage("Fatal Error", "Unrecognized File Type!",
                    "Selected file type is not a JPG, JPEG, PNG, or GIF!");
            return "";
        }

        storePhotoFile(file);

        // Redirect to show the Profile page
        return "/userAccount/Profile?faces-redirect=true";
    }

    //====================================================================
    // Store Uploaded User Profile Photo File and its Thumbnail Image File
    //====================================================================
    public void storePhotoFile(UploadedFile file) {

        // Since we will redirect to show the Profile page, invoke preserveMessages()
        Methods.preserveMessages();

        try {
            // Obtain the object reference of the signed-in User object
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            User signedInUser = (User) sessionMap.get("user");

            /*
            deletePhoto() method given below deletes signedInUser's earlier uploaded
            profile photo file, its thumbnail image file, and its database record.
             */
            deletePhoto();

            // Obtain the uploaded file's MIME file type
            String mimeFileType = file.getContentType();

            // If it is an image file, obtain its file extension; otherwise, set JPEG as the file extension anyway.
            String fileExtension = mimeFileType.startsWith("image/") ? mimeFileType.subSequence(6, mimeFileType.length()).toString() : "jpeg";

            // Construct a new Photo object with file extension and signedInUser's object reference
            UserPhoto newPhoto = new UserPhoto(fileExtension, signedInUser);

            // Create a record for the new Photo object in the database
            userPhotoFacade.create(newPhoto);

            /*
            Obtain the object reference of the first Photo object of the signedInUser 
            whose primary key is signedInUser.getId()
             */
            UserPhoto photo = userPhotoFacade.findPhotosByUserPrimaryKey(signedInUser.getId()).getFirst();

            /*
            InputStream is an abstract class, which is the superclass of all classes representing
            an input stream of bytes. It is imported as: import java.io.InputStream;
            Convert the uploaded file into an input stream of bytes.
             */
            InputStream inputStream = file.getInputStream();

            // Write the uploaded file's input stream of bytes under the photo object's
            // filename using the inputStreamToFile method given below
            File uploadedFile = inputStreamToFile(inputStream, photo.getPhotoFilename());

            // Create and save the thumbnail version of the uploaded file
            saveThumbnail(uploadedFile, photo);

            Methods.showMessage("Information", "Success!",
                    "User's Photo File is Successfully Uploaded!");

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong while storing the user's photo file!",
                    "See: " + ex.getMessage());
        }
    }

    //======================================================================
    // Write given inputStream of bytes into a file with name targetFilename
    //======================================================================
    private File inputStreamToFile(InputStream inputStream, String targetFilename) throws IOException {

        File targetFile = null;

        try {
            /*
            inputStream.available() returns an estimate of the number of bytes that can be read from
            the inputStream without blocking by the next invocation of a method for this input stream.
            A memory buffer of bytes is created with the size of estimated number of bytes.
             */
            byte[] buffer = new byte[inputStream.available()];

            // Read the bytes of data from the inputStream into the created memory buffer. 
            inputStream.read(buffer);

            // Create a new empty file with name targetFilename in the UserPhotoStorage directory
            targetFile = new File(Constants.PHOTOS_ABSOLUTE_PATH, targetFilename);

            // A file OutputStream is an output stream for writing data to a file
            OutputStream outStream;

            /*
            FileOutputStream is intended for writing streams of raw bytes such as image data.
            Create a new FileOutputStream for writing to the empty targetFile
             */
            outStream = new FileOutputStream(targetFile);

            // Create the targetFile in the UserPhotoStorage directory with the inputStream given
            outStream.write(buffer);

            // Close the output stream and release any system resources associated with it. 
            outStream.close();

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong in input stream to file!",
                    "See: " + ex.getMessage());
        }

        // Return the created targetFile
        return targetFile;
    }

    //=============================================
    // Store Signed-In User's Thumbnail Photo Image
    //=============================================
    private void saveThumbnail(File inputFile, UserPhoto inputPhoto) {
        try {
            // Buffer the photo image from the uploaded inputFile
            BufferedImage uploadedPhoto = ImageIO.read(inputFile);

            /*
            The thumbnail photo image size is set to 200x200px in Constants.java as follows:
            public static final Integer THUMBNAIL_SIZE = 200;

            When signedInUser uploads a photo, a thumbnail (small) version of the photo image
            is created in this method by using the Scalr.resize method provided in the
            imgscalr (Java Image Scaling Library) imported as imgscalr-lib-4.2.jar

            The thumbnail file is named as "userId_thumbnail.fileExtension", 
            e.g., 5_thumbnail.jpg for signedInUser with id 5.
             */

            // Scale the uploaded photo image to the THUMBNAIL_SIZE using imgscalr-lib-4.2.jar.
            BufferedImage thumbnailPhoto = Scalr.resize(uploadedPhoto, Constants.THUMBNAIL_SIZE);

            // Create the thumbnail photo file in the UserPhotoStorage directory
            File thumbnailPhotoFile = new File(Constants.PHOTOS_ABSOLUTE_PATH, inputPhoto.getThumbnailFileName());

            /*
            NOTE: ImageIO is imported as: import javax.imageio.ImageIO;
            Write the thumbnailPhoto into thumbnailPhotoFile with the file extension.
             */
            ImageIO.write(thumbnailPhoto, inputPhoto.getExtension(), thumbnailPhotoFile);

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong while saving the thumbnail file!",
                    "See: " + ex.getMessage());
        }
    }

    //========================================================
    // Delete Signed-In User's Photo and Thumbnail Image Files
    //========================================================
    public void deletePhoto() {

        // Obtain the object reference of the signedInUser
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");

        // Obtain the id (primary key in the database) of the signedInUser object
        Integer primaryKey = signedInUser.getId();

        /*
        Obtain the list of Photo file objects that belong to the signedInUser whose
        database primary key is primaryKey. The list will contain only one photo or nothing.
        We allow only one profile photo for the user to upload.
         */
        List<UserPhoto> photoList = userPhotoFacade.findPhotosByUserPrimaryKey(primaryKey);

        // photoList.isEmpty implies that the user has never uploaded a photo file
        if (!photoList.isEmpty()) {

            // Obtain the object reference of the first Photo object in the list
            UserPhoto photo = photoList.getFirst();

            try {
                /*
                NOTE: Files and Paths are imported as
                        import java.nio.file.Files;
                        import java.nio.file.Paths;

                 Delete the user's photo if it exists.
                 getFilePath() is given in UserPhoto.java file.
                 */
                Files.deleteIfExists(Paths.get(photo.getPhotoFilePath()));

                /*
                 Delete the user's thumbnail image if it exists.
                 getThumbnailFilePath() is given in UserPhoto.java file.
                 */
                Files.deleteIfExists(Paths.get(photo.getThumbnailFilePath()));

                // Delete the photo file record from the database.
                // UserPhotoFacade inherits the remove() method from AbstractFacade.
                userPhotoFacade.remove(photo);

            } catch (IOException ex) {
                Methods.showMessage("Fatal Error",
                        "Something went wrong while deleting the user photo file!",
                        "See: " + ex.getMessage());
            }
        }

    }
}
