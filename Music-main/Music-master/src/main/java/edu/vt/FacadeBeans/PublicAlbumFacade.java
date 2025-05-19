package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.PublicAlbum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class PublicAlbumFacade extends AbstractFacade<PublicAlbum> {
    @PersistenceContext(unitName = "MusicPU")
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public PublicAlbumFacade() {
        super(PublicAlbum.class);
    }

    public PublicAlbum getPublicAlbum(int id) {
        return entityManager.find(PublicAlbum.class, id);
    }

    public List<PublicAlbum> findByName(String name) {
        return entityManager.createNamedQuery("PublicAlbum.findByName")
                .setParameter("name", name)
                .getResultList();
    }

    public List<PublicAlbum> findBySpotifyId(String spotify_id) {
        return entityManager.createNamedQuery("PublicAlbum.findBySpotifyId")
                .setParameter("spotify_id", spotify_id)
                .getResultList();
    }
}
