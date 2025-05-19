package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.UserConcert;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class UserConcertFacade extends AbstractFacade<UserConcert> {
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
    public UserConcertFacade() {
        super(UserConcert.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the UserConcert object whose primary key is id
    //===========================================================================
    public UserConcert getUserConcert(int id) {
        return entityManager.find(UserConcert.class, id);
    }

    //======================================================================
    // Return a list of object references of UserConcert objects with name
    //======================================================================
    public List<UserConcert> findByUserConcertName(String event_name) {
        /*
        The following @NamedQuery definition is given in UserConcert entity class file:
        @NamedQuery(name = "UserConcert.findByUserConcertName", query = "SELECT c FROM UserConcert c WHERE c.event_name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserConcert.findByUserConcertName")
                .setParameter("event_name", event_name)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of UserConcert objects with artist_name
    //======================================================================
    public List<UserConcert> findByUserConcertArtistName(String artist_name) {
        /*
        The following @NamedQuery definition is given in UserConcert entity class file:
        @NamedQuery(name = "UserConcert.findByUserConcertArtistName", query = "SELECT c FROM UserConcert c WHERE c.artist_name = :artist_name")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserConcert.findByUserConcertArtistName")
                .setParameter("artist_name", artist_name)
                .getResultList();
    }

    //===================================================================================
    // Return a list of object references of UserConcert objects owned by user with id user_id
    //===================================================================================
    public List<UserConcert> findConcertsByUserDatabasePrimaryKey(Integer user_id) {
        /*
        The following @NamedQuery definition is given in UserConcert entity class file:
        @NamedQuery(name = "UserConcert.findConcertsByUserDatabasePrimaryKey", query = "SELECT p FROM UserConcert p WHERE p.userId.id = :primaryKey"),

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserConcert.findConcertsByUserDatabasePrimaryKey")
                .setParameter("primaryKey", user_id)
                .getResultList();
    }

}
