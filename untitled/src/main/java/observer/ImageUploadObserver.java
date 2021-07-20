package observer;

import com.google.protobuf.ByteString;
import com.vinsguru.io.ImageInfo;
import com.vinsguru.io.UploadImageRequest;
import com.vinsguru.io.UploadImageResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import model.ImageMetaDataCustom;
import service.ImageStore;
import service.impl.ImageStoreImpl;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;


public class ImageUploadObserver implements StreamObserver<UploadImageRequest> {
  private static final Logger logger = Logger.getLogger(ImageUploadObserver.class.getName());

  private StreamObserver<UploadImageResponse> uploadImageResponseStreamObserver;
  private static final int maxImageSize = 1 << 20; // 1 megabyte
  private String imageId;
  private String imageType;
  private ByteArrayOutputStream imageData;
  private ImageStore imageStore;

  public ImageUploadObserver(StreamObserver<UploadImageResponse> uploadImageResponseStreamObserver, ImageStore imageStore) {
    this.imageStore = imageStore;
    this.uploadImageResponseStreamObserver = uploadImageResponseStreamObserver;
  }

  @Override
  public void onNext(UploadImageRequest request) {
    if (request.getDataCase() == UploadImageRequest.DataCase.INFO) {
      ImageInfo info = request.getInfo();
      logger.info("receive image info:\n" + info);

      imageId   = info.getId();
      imageType = info.getImageType();
      imageData = new ByteArrayOutputStream();

      ByteString chunkData = request.getChunkData();
      logger.info("receive image chunk with size: " + chunkData.size());

      ImageMetaDataCustom imageMetaDataCustom = imageStore.Find(imageId);
      if(imageMetaDataCustom != null) {
        logger.info("This image has existed");
        uploadImageResponseStreamObserver.onError(
                Status.ALREADY_EXISTS
                        .withDescription("This image has existed")
                        .asRuntimeException()
        );
        return;
      }

      if(imageId == null) {
          logger.info("image info wasn't sent");
          uploadImageResponseStreamObserver.onError(
                  Status.INVALID_ARGUMENT
                          .withDescription("image info wasn't sent")
                          .asRuntimeException()
          );
          return;
      }

      if (imageData == null) {
        logger.info("image info wasn't sent before");
        uploadImageResponseStreamObserver.onError(
                Status.INVALID_ARGUMENT
                        .withDescription("image info wasn't sent before")
                        .asRuntimeException()
        );
        return;
      }
      System.out.println("imageData " + imageData.size());
      int size = imageData.size() + chunkData.size();

      if (size > maxImageSize) {
        logger.info("image is too large: " + size);
        uploadImageResponseStreamObserver.onError(
                Status.INVALID_ARGUMENT
                        .withDescription("image is too large: " + size)
                        .asRuntimeException()
        );
        return;
      }

      try {
        chunkData.writeTo(imageData);
      } catch (IOException e) {
        uploadImageResponseStreamObserver.onError(
                Status.INTERNAL
                        .withDescription("cannot write chunk data: " + e.getMessage())
                        .asRuntimeException()
        );
//        return;
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    logger.warning(t.getMessage());
  }

  @Override
  public void onCompleted() {
    String imageID = "2";
    int imageSize = imageData.size();

    try {
      imageID = imageStore.save(imageID, imageType, imageData);
    } catch (IOException e) {
      uploadImageResponseStreamObserver.onError(
              Status.INTERNAL
                      .withDescription("cannot save image to the store: " + e.getMessage())
                      .asRuntimeException()
      );
      return;
    }

    UploadImageResponse response = UploadImageResponse.newBuilder()
            .setImageId(imageID)
            .setSize(imageSize)
            .build();
    uploadImageResponseStreamObserver.onNext(response);
    uploadImageResponseStreamObserver.onCompleted();
  }

}
