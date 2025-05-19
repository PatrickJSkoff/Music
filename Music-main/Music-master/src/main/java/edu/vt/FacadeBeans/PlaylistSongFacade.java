package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.PlaylistSong;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class PlaylistSongFacade extends AbstractFacade<PlaylistSong> {
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
    public PlaylistSongFacade() {
        super(PlaylistSong.class);
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //===================================================================================
    // Return a list of object references of Playlist Song objects owned by user with id playlist_id
    //===================================================================================
    public List<PlaylistSong> findPlaylistSongByPlaylistDatabasePrimaryKey(Integer playlist_id) {
        /*
        The following @NamedQuery definition is given in Playlist entity class file:
        @NamedQuery(name = "PlaylistSong.findPlaylistSongByPlaylistDatabasePrimaryKey", query = "SELECT p FROM PlaylistSong p WHERE p.playlist_id.id = :primaryKey"),

        The following statement obtains the results from the named database query.
         */
        return entityManager.createNamedQuery("PlaylistSong.findPlaylistSongByPlaylistDatabasePrimaryKey")
                .setParameter("primaryKey", playlist_id)
                .getResultList();
    }
}
