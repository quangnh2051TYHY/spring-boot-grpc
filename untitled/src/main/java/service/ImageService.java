package service;

import com.google.protobuf.ByteString;
import com.vinsguru.io.ImageInfo;
import com.vinsguru.io.ImageServiceGrpc;
import com.vinsguru.io.UploadImageRequest;
import com.vinsguru.io.UploadImageResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import model.ImageMetaDataCustom;
import observer.ImageUploadObserver;
import server.GRPCClient;
import service.impl.ImageStoreImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class ImageService extends ImageServiceGrpc.ImageServiceImplBase {
  private static final Logger logger = Logger.getLogger(ImageService.class.getName());

  @Override
  public StreamObserver<UploadImageRequest> uploadImage(StreamObserver<UploadImageResponse> responseObserver) {
    ImageStore imageStore = new ImageStoreImpl("img");
//    return new ImageUploadObserver(responseObserver, imageStore);
    return new StreamObserver<UploadImageRequest>() {
      private static final int maxImageSize = 1 << 20; // 1 megabyte
      private String laptopID;
      private String imageType;
      private ByteArrayOutputStream imageData;

      @Override
      public void onNext(UploadImageRequest request) {
        if (request.getDataCase() == UploadImageRequest.DataCase.INFO) {
          ImageInfo info = request.getInfo();
          logger.info("receive image info:\n" + info);

          laptopID = info.getId();
          imageType = info.getImageType();
          imageData = new ByteArrayOutputStream();

          // Check laptop exists
//          ImageMetaDataCustom found = imageStore.Find(laptopID);
//          if (found == null) {
//            responseObserver.onError(
//                    Status.NOT_FOUND
//                            .withDescription("laptop ID doesn't exist")
//                            .asRuntimeException()
//            );
//          }
//
//          return;
//        }

          ByteString chunkData = request.getChunkData();
          logger.info("receive image chunk with size: " + chunkData.size());

          if (imageData == null) {
            logger.info("image info wasn't sent before");
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("image info wasn't sent before")
                            .asRuntimeException()
            );
            return;
          }

          int size = imageData.size() + chunkData.size();

          if (size > maxImageSize) {
            logger.info("image is too large: " + size);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("image is too large: " + size)
                            .asRuntimeException()
            );
            return;
          }

          try {
            chunkData.writeTo(imageData);
          } catch (IOException e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("cannot write chunk data: " + e.getMessage())
                            .asRuntimeException()
            );
            return;
          }
        }
      }

      @Override
      public void onError(Throwable t) {
        logger.warning(t.getMessage());
      }

      @Override
      public void onCompleted() {
        String imageID = "";
        int imageSize = imageData.size();

        try {
          imageID = imageStore.save(laptopID, imageType, imageData);
        } catch (IOException e) {
          responseObserver.onError(
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
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    };
  }
}
