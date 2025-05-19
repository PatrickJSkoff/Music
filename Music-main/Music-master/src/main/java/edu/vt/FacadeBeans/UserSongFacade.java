package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.UserSong;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class UserSongFacade extends AbstractFacade<UserSong> {
    /*
    ---------------------------------------------------------------------------------------------
    The EntityManager is an API that enables database CRUD (Create Read Update Delete) operations
    and complex database searches. An EntityManager instance is created to manage entities
    that are defined by a persistence unit. The @PersistenceContext annotation below associates
    the entityManager instance with the persistence unitName identified below.
    ---------------------------------------------------------------------------------------------
     */
    @PersistenceContext(unitName = "MusicPU")
    private EntityManager entityManager;

    // Obtain the object reference of the EntityManager instance in charge of
    // managing the entities in the persistence context identified above.
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    //====================================================================================
    // This constructor method invokes its parent AbstractFacade's constructor method,
    // which in turn initializes its entity class type T and entityClass instance variable.
    //=====================================================================================
    public UserSongFacade() {
        super(UserSong.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the UserSong object whose primary key is id
    //===========================================================================
    public UserSong getUserSong(int id) {
        return entityManager.find(UserSong.class, id);
    }

    //======================================================================
    // Return a list of object references of UserSong objects with name
    //======================================================================
    public List<UserSong> findByUserSongName(String name) {
        /*
        The following @NamedQuery definition is given in UserSong entity class file:
        @NamedQuery(name = "UserSong.findByUserSongName", query = "SELECT c FROM UserSong c WHERE c.event_name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserSong.findByUserSongName")
                .setParameter("name", name)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of UserSong objects with artist_spotify_id
    //======================================================================
    public List<UserSong> findByUserSongArtistName(String artist_spotify_id) {
        /*
        The following @NamedQuery definition is given in UserSong entity class file:
        @NamedQuery(name = "UserSong.findByUserSongArtistSpotifyId", query = "SELECT c FROM UserSong c WHERE c.artist_spotify_id = :artist_spotify_id")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserSong.findByUserSongArtistSpotifyId")
                .setParameter("artist_spotify_id", artist_spotify_id)
                .getResultList();
    }

    //===================================================================================
    // Return a list of object references of UserSong objects owned by user with id user_id
    //===================================================================================
    public List<UserSong> findSongsByUserDatabasePrimaryKey(Integer user_id) {
        /*
        The following @NamedQuery definition is given in UserSong entity class file:
        @NamedQuery(name = "UserSong.findSongsByUserDatabasePrimaryKey", query = "SELECT p FROM UserSong p WHERE p.userId.id = :primaryKey"),

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserSong.findSongsByUserDatabasePrimaryKey")
                .setParameter("primaryKey", user_id)
                .getResultList();
    }

    /**
     * Find all songs that belong to a User whose database primary key is dbPrimaryKey
     *
     * @param dbPrimaryKey is the Primary Key of the user entity in the database
     * @return a list of object references of userSong that belong to the user whose database Primary Key = dbPrimaryKey
     */
    public List<UserSong> findUserQuestionnairesByUserPrimaryKey(Integer dbPrimaryKey) {
        /*
        The following @NamedQuery is defined in UserSong.java entity class file:
        @NamedQuery(name = "UserSong.findQuestionnairesByUserPrimaryKey",
            query = "SELECT u FROM UserSong u WHERE u.userId.id = :primaryKey")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserSong.findQuestionnairesByUserPrimaryKey")
                .setParameter("primaryKey", dbPrimaryKey)
                .getResultList();
    }

}
