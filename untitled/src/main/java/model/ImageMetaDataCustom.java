package model;

public class ImageMetaDataCustom {
  private String imageId;
  private String type;
  private String path;

  public ImageMetaDataCustom(String imageId, String type, String path) {
    this.imageId = imageId;
    this.type = type;
    this.path = path;
  }

  public String getImageId() {
    return imageId;
  }

  public String getType() {
    return type;
  }

  public String getPath() {
    return path;
  }
}

