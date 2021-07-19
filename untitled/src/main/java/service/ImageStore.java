package service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface ImageStore {
  String save(String imageId, String imageType, ByteArrayOutputStream imageData) throws IOException;
}
