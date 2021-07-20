package service.impl;

import com.vinsguru.io.ImageInfo;
import model.ImageMetaDataCustom;
import service.ImageStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ImageStoreImpl implements ImageStore {
  private String imageFolder;
  private ConcurrentMap<String, ImageMetaDataCustom> data;

  public ImageStoreImpl(String imageFolder) {
    this.imageFolder = imageFolder;
    this.data = new ConcurrentHashMap<>(0);
  }

  @Override
  public String save(String imageId, String imageType, ByteArrayOutputStream imageData) throws IOException {
    String id = UUID.randomUUID().toString();
    String imagePath = String.format("%s/%s%s", imageFolder, id, imageType);
    FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
    imageData.writeTo(fileOutputStream);
    fileOutputStream.close();

    ImageMetaDataCustom imageMetaDataCustom = new ImageMetaDataCustom(imageId, imageType, imagePath);
    data.put(imageId, imageMetaDataCustom);
    return imageId;
  }

  @Override
  public ImageMetaDataCustom Find(String id) {
    if (!data.containsKey(id)) {
      return null;
    }
    ImageMetaDataCustom imageMetaDataCustom = data.get(id);
    return imageMetaDataCustom;
  }
}
