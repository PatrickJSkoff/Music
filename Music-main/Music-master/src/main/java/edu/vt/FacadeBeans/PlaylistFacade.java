package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.Playlist;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class PlaylistFacade extends AbstractFacade<Playlist> {
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
    public PlaylistFacade() {
        super(Playlist.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===========================================================================
    // Return the object reference of the Playlist object whose primary key is id
    //===========================================================================
    public Playlist getPlaylist(int id) {
        return entityManager.find(Playlist.class, id);
    }

    //======================================================================
    // Return a list of object references of Playlist objects with name
    //======================================================================
    public List<Playlist> findByPlaylistName(String name) {
        /*
        The following @NamedQuery definition is given in Playlist entity class file:
        @NamedQuery(name = "UserSong.findByPlaylistName", query = "SELECT c FROM Playlist c WHERE c.name = :name"),
        
        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("Playlist.findByPlaylistName")
                .setParameter("name", name)
                .getResultList();
    }

    //===================================================================================
    // Return a list of object references of Playlist objects owned by user with id user_id
    //===================================================================================
    public List<Playlist> findPlaylistByUserDatabasePrimaryKey(Integer user_id) {
        /*
        The following @NamedQuery definition is given in Playlist entity class file:
        @NamedQuery(name = "Playlist.findPlaylistByUserDatabasePrimaryKey", query = "SELECT p FROM Playlist p WHERE p.userId.id = :primaryKey"),

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("Playlist.findPlaylistByUserDatabasePrimaryKey")
                .setParameter("primaryKey", user_id)
                .getResultList();
    }
}
