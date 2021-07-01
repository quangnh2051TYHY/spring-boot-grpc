package org.example.userservice.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.proto.common.Genre;
import org.example.proto.user.UserGenreUpdateRequest;
import org.example.proto.user.UserResponse;
import org.example.proto.user.UserSearchRequest;
import org.example.proto.user.UserServiceGrpc;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {

  @Autowired
  private UserRepository userRepository;

  @Override
  public void getUser(UserSearchRequest request, StreamObserver<UserResponse> responseObserver) {
    String id = request.getId();

    User user = checkUser(id);
    UserResponse userResponse = UserResponse.newBuilder()
        .setGenre(Genre.valueOf(user.getGenre()))
        .setId(user.getId())
        .setName(user.getName())
        .build();

    responseObserver.onNext(userResponse);
    responseObserver.onCompleted();
  }

  private User checkUser(String id)  {
    Optional<User> userOptional = userRepository.findById(id);
    if (!userOptional.isPresent()) {
//      throw new Exception("User has not existed");
    }
    return userOptional.get();
  }

  @Override
  public void updateUserGenre(UserGenreUpdateRequest request, StreamObserver<UserResponse> responseObserver) {
    String id = request.getId();
    User user = checkUser(id);
    System.out.println(request.getGenre().name());
    user.setGenre(request.getGenre().name());

    userRepository.save(user);

    UserResponse userResponse = UserResponse.newBuilder()
        .setGenre(Genre.valueOf(user.getGenre()))
        .setId(user.getId())
        .setName(user.getName())
        .build();
    responseObserver.onNext(userResponse);
    responseObserver.onCompleted();
  }
}
