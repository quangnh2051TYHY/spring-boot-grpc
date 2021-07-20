package server;

import com.google.protobuf.ByteString;
import com.vinsguru.io.ImageInfo;
import com.vinsguru.io.ImageServiceGrpc;
import com.vinsguru.io.UploadImageRequest;
import com.vinsguru.io.UploadImageResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GRPCClient {

  private static final Logger logger = Logger.getLogger(GRPCClient.class.getName());

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();

    ImageServiceGrpc.ImageServiceStub asyncStub = ImageServiceGrpc.newStub(channel);

    String imagePath = "tmp/laptop.jpg";

    final CountDownLatch finishLatch = new CountDownLatch(1);

    StreamObserver<UploadImageRequest> requestObserver = asyncStub.withDeadlineAfter(30, TimeUnit.SECONDS)
            .uploadImage(new StreamObserver<UploadImageResponse>() {
              @Override
              public void onNext(UploadImageResponse response) {
                logger.info("receive response:\n" + response);
              }

              @Override
              public void onError(Throwable t) {
                logger.log(Level.SEVERE, "upload failed: " + t);
                finishLatch.countDown();
              }

              @Override
              public void onCompleted() {
                logger.info("image uploaded");
                finishLatch.countDown();
              }
            });

    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(imagePath);
    } catch (FileNotFoundException e) {
      logger.log(Level.SEVERE, "cannot read image file: " + e.getMessage());
      return;
    }

    String imageType = imagePath.substring(imagePath.lastIndexOf("."));
    ImageInfo info = ImageInfo.newBuilder().setId("2").setImageType(imageType).build();
    UploadImageRequest request = UploadImageRequest.newBuilder().setInfo(info).build();

    try {
      requestObserver.onNext(request);
      logger.info("sent image info:\n" + info);

      byte[] buffer = new byte[1024];
      while (true) {
        int n = fileInputStream.read(buffer);
        if (n <= 0) {
          break;
        }

        if (finishLatch.getCount() == 0) {
          return;
        }

        request = UploadImageRequest.newBuilder()
                .setChunkData(ByteString.copyFrom(buffer, 0, n))
                .build();
        requestObserver.onNext(request);
        logger.info("sent image chunk with size: " + n);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "unexpected error: " + e.getMessage());
      requestObserver.onError(e);
      return;
    }

    requestObserver.onCompleted();

    if (!finishLatch.await(1, TimeUnit.MINUTES)) {
      logger.warning("request cannot finish within 1 minute");
    }
  }
  }

