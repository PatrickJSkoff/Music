package edu.vt.EntityBeans;

import edu.vt.globals.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

// The @Entity annotation designates this class as a Jakarta Persistence Entity POJO class representing the database table UserPhoto.
@Entity

// Name of the database table represented
@Table(name = "UserPhoto")

@NamedQueries({
    /*
    private User userId;    --> userId is the object reference of the User object.
    userId.id               --> User object's database primary key
     */
    @NamedQuery(name = "UserPhoto.findPhotosByUserDatabasePrimaryKey", query = "SELECT p FROM UserPhoto p WHERE p.userId.id = :primaryKey")
    , @NamedQuery(name = "UserPhoto.findAll", query = "SELECT u FROM UserPhoto u")
    , @NamedQuery(name = "UserPhoto.findById", query = "SELECT u FROM UserPhoto u WHERE u.id = :id")
    , @NamedQuery(name = "UserPhoto.findByExtension", query = "SELECT u FROM UserPhoto u WHERE u.extension = :extension")
})

public class UserPhoto implements Serializable {
    /*
    ========================================================
    Instance variables representing the attributes (columns)
    of the database table UserPhoto.

    CREATE TABLE UserPhoto
    (
        id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
        extension ENUM('jpeg', 'jpg', 'png', 'gif') NOT NULL,
        user_id INT UNSIGNED NOT NULL,
        FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
    );
    ========================================================
     */
    @Serial
    private static final long serialVersionUID = 1L;

    // id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // extension ENUM('jpeg', 'jpg', 'png', 'gif') NOT NULL
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 5)
    @Column(name = "extension")
    private String extension;

    // user_id INT UNSIGNED NOT NULL,
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User userId;

    //====================
    // Constructor Methods
    //====================
    public UserPhoto() {
    }

    // Called from UserPhotoController
    public UserPhoto(String fileExtension, User id) {
        this.extension = fileExtension;
        userId = id;
    }

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExtension() {
        return extension;
    }

    public User getUserId() {
        return userId;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    /*
    User's profile photo image file is named as "userId.fileExtension", e.g., 5.jpeg for user with id 5.
    Since the user can have only one profile photo, this makes sense.
     */
    public String getPhotoFilename() {
        return getUserId() + "." + getExtension();
    }

    /*
    --------------------------------------------------------------------------------
    The thumbnail photo image size is set to 200x200px in Constants.java as follows:
    public static final Integer THUMBNAIL_SIZE = 200;

    If the user uploads a large photo file, we will scale it down to THUMBNAIL_SIZE
    and use it so that the size is reasonable for performance reasons.

    The photo image scaling is properly done by using the imgscalr-lib-4.2.jar file.

    The thumbnail file is named as "userId_thumbnail.fileExtension",
    e.g., 5_thumbnail.jpeg for user with id 5.
    --------------------------------------------------------------------------------
     */
    public String getThumbnailFileName() {
        return getUserId() + "_thumbnail." + getExtension();
    }

    public String getPhotoFilePath() {
        return Constants.PHOTOS_ABSOLUTE_PATH + getPhotoFilename();
    }

    public String getThumbnailFilePath() {
        return Constants.PHOTOS_ABSOLUTE_PATH + getThumbnailFileName();
    }

    /*
     ****************************************
     *   Instance Methods Used Internally   *
     ****************************************
     */

    // Generate and return a hash code value for the object with database primary key id
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    /*
     Checks if the UserPhoto object identified by 'object' is the same as the UserPhoto object identified by 'id'
     Parameter object = UserPhoto object identified by 'object'
     Returns True if the UserPhoto 'object' and 'id' are the same; otherwise, return False
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserPhoto other)) {
            return false;
        }
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    // Return String representation of database primary key id
    @Override
    public String toString() {
        return id.toString();
    }

}
