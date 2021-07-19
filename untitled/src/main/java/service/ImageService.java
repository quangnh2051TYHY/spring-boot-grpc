package service;

import com.vinsguru.io.ImageServiceGrpc;
import com.vinsguru.io.UploadImageRequest;
import com.vinsguru.io.UploadImageResponse;
import io.grpc.stub.StreamObserver;

public class ImageService extends ImageServiceGrpc.ImageServiceImplBase {
  @Override
  public StreamObserver<UploadImageRequest> uploadImage(StreamObserver<UploadImageResponse> responseObserver) {
    return super.uploadImage(responseObserver);
  }
}
