package edu.vt.EntityBeans;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

/*
The @Entity annotation designates this class as a Jakarta Persistence Entity POJO class
representing the Playlist table in the MusicDB database.
 */
@Entity

// Name of the database table represented by this class
@Table(name = "Playlist")

@NamedQueries({
        // This named query is used in PlaylistFacade
        @NamedQuery(name = "Playlist.findByPlaylistName", query = "SELECT c FROM Playlist c WHERE c.name = :name"),
        @NamedQuery(name = "Playlist.findPlaylistByUserDatabasePrimaryKey", query = "SELECT p FROM Playlist p WHERE p.userId.id = :primaryKey"),
})

public class Playlist implements Serializable {
    /*
    ========================================================
    Instance variables representing the attributes (columns)
    of the UserSong table in the MusicDB database.

    This is a concert added to users personal list
    Because there can be a limitless number of artists featured in the concert we just store the primary artist info and then list the other artists in the description
    Ticketmaster ids vary in length
    CREATE TABLE UserSong
    (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NOT NULL,
    image_url VARCHAR(256) NOT NULL,
    user_id INT UNSIGNED,
    FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE CASCADE
    );
    ========================================================
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /*
    Primary Key id is auto generated by the system as an Integer value
    starting with 1 and incremented by 1, i.e., 1,2,3,...
    A deleted entity object's primary key number is not reused.
     */
    // id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // name VARCHAR(128) NOT NULL
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "name")
    private String name;

    // description VARCHAR(512) NOT NULL
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 512)
    @Column(name = "description")
    private String description;

    // image_url VARCHAR(256) NOT NULL
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "image_url")
    private String image_url;

    // user_id INT UNSIGNED NOT NULL,
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User userId;


    //===================
    // Constructor Method
    //===================

    // Used in PrepareCreate method
    public Playlist() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    /*
     ***************************************
     *   Instance Methods Used Internally  *
     ***************************************
     */

    // Generate and return a hash code value for the object with database primary key id
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    /*
     Checks if the UserSong object identified by 'object' is the same as the UserSong
     object identified by 'id' Parameter object = UserSong object identified by 'object'.
     Returns True if the UserSong 'object' and 'id' are the same; otherwise, return False
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Playlist)) {
            return false;
        }
        Playlist other = (Playlist) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    // Return String representation of database primary key id
    @Override
    public String toString() {
        return id.toString();
    }
}
