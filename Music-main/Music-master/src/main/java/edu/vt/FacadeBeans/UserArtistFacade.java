package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.UserArtist;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class UserArtistFacade extends AbstractFacade<UserArtist> {
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
    public UserArtistFacade() {
        super(UserArtist.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the UserArtist object whose primary key is id
    //===========================================================================
    public UserArtist getUserArtist(int id) {
        return entityManager.find(UserArtist.class, id);
    }

    //======================================================================
    // Return a list of object references of UserArtist objects with name
    //======================================================================
    public List<UserArtist> findByUserArtistName(String name) {
        /*
        The following @NamedQuery definition is given in UserArtist entity class file:
        @NamedQuery(name = "UserArtist.findByUserArtistName", query = "SELECT c FROM UserArtist c WHERE c.name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserArtist.findByUserArtistName")
                .setParameter("name", name)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of UserArtist objects with spotify_id
    //======================================================================
    public List<UserArtist> findByUserArtistSpotifyId(String spotify_id) {
        /*
        The following @NamedQuery definition is given in UserArtist entity class file:
        @NamedQuery(name = "UserArtist.findByUserArtistSpotifyId", query = "SELECT c FROM UserArtist c WHERE c.spotify_id = :spotify_id")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserArtist.findByUserArtistSpotifyId")
                .setParameter("spotify_id", spotify_id)
                .getResultList();
    }

    //===================================================================================
    // Return a list of object references of UserArtist objects owned by user with id user_id
    //===================================================================================
    public List<UserArtist> findArtistsByUserDatabasePrimaryKey(Integer user_id) {
        /*
        The following @NamedQuery definition is given in UserArtist entity class file:
        @NamedQuery(name = "UserArtist.findArtistsByUserDatabasePrimaryKey", query = "SELECT p FROM UserArtist p WHERE p.userId.id = :primaryKey"),

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserArtist.findArtistsByUserDatabasePrimaryKey")
                .setParameter("primaryKey", user_id)
                .getResultList();
    }

}
