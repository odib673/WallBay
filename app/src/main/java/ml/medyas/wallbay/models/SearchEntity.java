package ml.medyas.wallbay.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ml.medyas.wallbay.models.pexels.PexelsEntity;
import ml.medyas.wallbay.models.pexels.Photo;
import ml.medyas.wallbay.models.pixabay.Hit;
import ml.medyas.wallbay.models.pixabay.PixabayEntity;
import ml.medyas.wallbay.models.unsplash.UnsplashPhotoEntity;
import ml.medyas.wallbay.models.unsplash.UnsplashSearchEntity;

import static ml.medyas.wallbay.utils.Utils.webSite;

public class SearchEntity {
    private PixabayEntity pixabayEntity;
    private UnsplashSearchEntity unsplashPhotoEntity;
    private PexelsEntity pexelsEntity;

    public SearchEntity(PixabayEntity pixabayEntity, UnsplashSearchEntity unsplashPhotoEntity, PexelsEntity pexelsEntity) {
        this.pixabayEntity = pixabayEntity;
        this.unsplashPhotoEntity = unsplashPhotoEntity;
        this.pexelsEntity = pexelsEntity;
    }

    public SearchEntity() {
    }

    public PixabayEntity getPixabayEntity() {
        return pixabayEntity;
    }

    public void setPixabayEntity(PixabayEntity pixabayEntity) {
        this.pixabayEntity = pixabayEntity;
    }

    public UnsplashSearchEntity getUnsplashPhotoEntity() {
        return unsplashPhotoEntity;
    }

    public void setUnsplashPhotoEntity(UnsplashSearchEntity unsplashPhotoEntity) {
        this.unsplashPhotoEntity = unsplashPhotoEntity;
    }

    public PexelsEntity getPexelsEntity() {
        return pexelsEntity;
    }

    public void setPexelsEntity(PexelsEntity pexelsEntity) {
        this.pexelsEntity = pexelsEntity;
    }

    private List<ImageEntity> getPixabayList() {
        List<ImageEntity> list = new ArrayList<>();
        webSite provider = webSite.PIXABAY;
        provider.setCode(1);
        for (Hit item : pixabayEntity.getHits()) {
            ImageEntity imageEntity = new ImageEntity(
                    String.valueOf(item.getId()),
                    item.getUser(),
                    item.getUserImageURL(),
                    provider,
                    item.getLikes(),
                    item.getViews(),
                    item.getDownloads(),
                    item.getImageWidth(),
                    item.getImageHeight(),
                    item.getPageURL(),
                    item.getImageURL(),
                    item.getImageURL(),
                    item.getWebformatURL(), // previewURL
                    item.getTags()
            );
            list.add(imageEntity);
        }

        return list;
    }

    private List<ImageEntity> getPexelsList() {
        List<ImageEntity> list = new ArrayList<>();
        webSite provider = webSite.PEXELS;
        provider.setCode(1);
        for (Photo item : pexelsEntity.getPhotos()) {
            ImageEntity imageEntity = new ImageEntity(
                    String.valueOf(item.getId()),
                    item.getPhotographer(),
                    null,
                    provider,
                    0,
                    0,
                    0,
                    item.getWidth(),
                    item.getHeight(),
                    item.getUrl(),
                    item.getSrc().getOriginal(),
                    item.getSrc().getOriginal(),
                    item.getSrc().getMedium(),
                    ""
            );
            list.add(imageEntity);
        }

        return list;
    }


    private List<ImageEntity> getUnsplahList() {
        List<ImageEntity> list = new ArrayList<>();
        webSite provider = webSite.UNSPLASH;
        provider.setCode(2);
        for (UnsplashPhotoEntity item : unsplashPhotoEntity.getUnsplashPhotoEntitys()) {
            ImageEntity imageEntity = new ImageEntity(
                    item.getId(),
                    item.getUser().getUsername(),
                    item.getUser().getProfileImage().getMedium(),
                    provider,
                    item.getLikes(),
                    0,
                    0,
                    item.getWidth(),
                    item.getHeight(),
                    item.getLinks().getHtml(),
                    item.getUrls().getRaw(),
                    item.getLinks().getDownloadLocation(),
                    item.getUrls().getSmall(),
                    "");
            list.add(imageEntity);
        }

        return list;
    }

    public List<ImageEntity> getAll() {
        List<ImageEntity> list = new ArrayList<>();
        list.addAll(getUnsplahList());
        list.addAll(getPixabayList());
        list.addAll(getPexelsList());

        Collections.shuffle(list);
        return list;
    }
}
