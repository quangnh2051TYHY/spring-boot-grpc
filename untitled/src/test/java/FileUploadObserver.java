import com.vinsguru.io.FileUploadResponse;
import io.grpc.stub.StreamObserver;

public class FileUploadObserver implements StreamObserver<FileUploadResponse> {
  @Override
  public void onNext(FileUploadResponse fileUploadResponse) {
    System.out.println(
            "File upload status :: " + fileUploadResponse.getStatus()
    );
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println(throwable.getCause());
  }

  @Override
  public void onCompleted() {
    System.out.println(
            "Complete"
    );
  }
}