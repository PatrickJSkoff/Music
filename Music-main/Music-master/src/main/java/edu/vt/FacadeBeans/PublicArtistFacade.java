package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.PublicArtist;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class PublicArtistFacade extends AbstractFacade<PublicArtist> {
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
    public PublicArtistFacade() {
        super(PublicArtist.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the PublicArtist object whose primary key is id
    //===========================================================================
    public PublicArtist getPublicArtist(int id) {
        return entityManager.find(PublicArtist.class, id);
    }

    //======================================================================
    // Return a list of object references of PublicArtist objects with name
    //======================================================================
    public List<PublicArtist> findByName(String name) {
        /*
        The following @NamedQuery definition is given in PublicArtist entity class file:
        @NamedQuery(name = "PublicArtist.findByName", query = "SELECT c FROM PublicArtist c WHERE c.name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("PublicArtist.findByName")
                .setParameter("name", name)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of PublicArtist objects with spotify_id
    //======================================================================
    public List<PublicArtist> findByPublicArtistSpotifyId(String spotify_id) {
        /*
        The following @NamedQuery definition is given in PublicArtist entity class file:
        @NamedQuery(name = "PublicArtist.findBySpotifyId", query = "SELECT c FROM PublicArtist c WHERE c.spotify_id = :spotify_id")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("PublicArtist.findBySpotifyId")
                .setParameter("spotify_id", spotify_id)
                .getResultList();
    }

}
