package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.UserArtistAlbum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class UserArtistAlbumFacade extends AbstractFacade<UserArtistAlbum> {
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
    public UserArtistAlbumFacade() {
        super(UserArtistAlbum.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the UserArtist object whose primary key is id
    //===========================================================================
    public UserArtistAlbum getUserArtistAlbum(int id) {
        return entityManager.find(UserArtistAlbum.class, id);
    }

    //======================================================================
    // Return a list of object references of UserArtist objects with name
    //======================================================================
    public List<UserArtistAlbum> findByUserArtistId(Integer artist_id) {
        /*
        The following @NamedQuery definition is given in UserArtist entity class file:
        @NamedQuery(name = "UserArtist.findByUserArtistName", query = "SELECT c FROM UserArtist c WHERE c.name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserArtistAlbum.findByUserArtistId")
                .setParameter("userArtistId", artist_id)
                .getResultList();
    }

    //======================================================================
    // Return a list of object references of UserArtist objects with spotify_id
    //======================================================================
    public List<UserArtistAlbum> findByUserArtistAlbumSpotifyId(String spotify_id) {
        /*
        The following @NamedQuery definition is given in UserArtist entity class file:
        @NamedQuery(name = "UserArtist.findByUserArtistSpotifyId", query = "SELECT c FROM UserArtist c WHERE c.spotify_id = :spotify_id")

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("UserArtistAlbum.findByUserArtistAlbumSpotifyId")
                .setParameter("spotify_id", spotify_id)
                .getResultList();
    }

}
