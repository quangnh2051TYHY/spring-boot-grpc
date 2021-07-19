package service;

import com.google.protobuf.ByteString;
import com.vinsguru.io.ImageInfo;
import com.vinsguru.io.UploadImageRequest;
import com.vinsguru.io.UploadImageResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import service.impl.ImageStoreImpl;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUploadObserver implements StreamObserver<UploadImageRequest> {
  private StreamObserver<UploadImageResponse> uploadImageResponseStreamObserver;
  private String imageId;
  private String imageType;
  private ByteArrayOutputStream imageData;

  @Override
  public void onNext(UploadImageRequest uploadImageRequest) {
    if(uploadImageRequest.getInfo() != null) {
      ImageInfo imageInfo = uploadImageRequest.getInfo();
      System.out.println("Receive image" + imageInfo);

      imageId = imageInfo.getId();
      imageType = imageInfo.getImageType();
      imageData = new ByteArrayOutputStream();
      return;
    }
    ByteString chunkData = uploadImageRequest.getChunkData();
    System.out.println("Receive image size" + chunkData.size());
    if( imageData == null) {
      System.out.println("image info wasn't sent before");
      uploadImageResponseStreamObserver.onError(Status.INVALID_ARGUMENT
              .withDescription("image info wasn't sent before")
              .asRuntimeException());
      return;
    }
    try {
      chunkData.writeTo(imageData);
    } catch (IOException e) {
      uploadImageResponseStreamObserver
              .onError(Status.INTERNAL
              .withDescription("cannot write chunk data")
              .asRuntimeException());
      return;
    }
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println(throwable.getMessage());
  }

  @Override
  public void onCompleted() {
    String imageId = "";
    int imageSize = imageData.size();
    ImageStore imageStore = new ImageStoreImpl("img");
    try {
      imageStore.save(imageId, imageType,imageData);
    } catch (IOException e) {
      uploadImageResponseStreamObserver
              .onError(Status.INTERNAL
                      .withDescription("cannot write chunk data")
                      .asRuntimeException());
    }
    UploadImageResponse response = UploadImageResponse.newBuilder()
            .setImageId(imageId)
            .setSize(imageSize)
            .build();
    uploadImageResponseStreamObserver.onNext(response);
    uploadImageResponseStreamObserver.onCompleted();
  }
}
