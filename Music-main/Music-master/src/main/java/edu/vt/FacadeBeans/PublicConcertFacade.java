package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.PublicConcert;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class PublicConcertFacade extends AbstractFacade<PublicConcert> {
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
    public PublicConcertFacade() {
        super(PublicConcert.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the PublicConcert object whose primary key is id
    //===========================================================================
    public PublicConcert getPublicConcert(int id) {
        return entityManager.find(PublicConcert.class, id);
    }

    //======================================================================
    // Return a list of object references of PublicConcert objects with name
    //======================================================================
    public List<PublicConcert> findByPublicConcertName(String name) {
        /*
        The following @NamedQuery definition is given in PublicConcert entity class file:
        @NamedQuery(name = "PublicConcert.findByPublicConcertName", query = "SELECT c FROM PublicConcert c WHERE c.event_name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("PublicConcert.findByPublicConcertName")
                .setParameter("name", name)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of PublicConcert objects with artist_name
    //======================================================================
    public List<PublicConcert> findByPublicConcertArtistName(String artist_name) {
        /*
        The following @NamedQuery definition is given in PublicConcert entity class file:
        @NamedQuery(name = "PublicConcert.findByPublicConcertArtistName", query = "SELECT c FROM PublicConcert c WHERE c.artist_name = :artist_name")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("PublicConcert.findByPublicConcertArtistName")
                .setParameter("artist_name", artist_name)
                .getResultList();
    }
}
