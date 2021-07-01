package org.example.aggreagator.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.catalina.User;
import org.example.aggreagator.dto.RecommendedMovie;
import org.example.aggreagator.dto.UserGenre;
import org.example.proto.common.Genre;
import org.example.proto.movie.MovieSearchRequest;
import org.example.proto.movie.MovieSearchResponse;
import org.example.proto.movie.MovieServiceGrpc;
import org.example.proto.user.UserGenreUpdateRequest;
import org.example.proto.user.UserResponse;
import org.example.proto.user.UserSearchRequest;
import org.example.proto.user.UserServiceGrpc;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregatorService {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userStub;

  @GrpcClient("movie-service")
  private MovieServiceGrpc.MovieServiceBlockingStub movieStub;
  //set up api gateway
//
//  ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8585)
//      .usePlaintext()
//      .build();
//  private UserServiceGrpc.UserServiceBlockingStub userStub = UserServiceGrpc.newBlockingStub(channel);
//  private MovieServiceGrpc.MovieServiceBlockingStub movieStub = MovieServiceGrpc.newBlockingStub(channel);

  public List<RecommendedMovie> getUserMovieSuggestions(String id) {
    UserSearchRequest userSearchRequest = UserSearchRequest.newBuilder()
        .setId(id)
        .build();

    UserResponse userResponse = userStub.getUser(userSearchRequest);
    MovieSearchRequest movieSearchRequest = MovieSearchRequest.newBuilder()
        .setGenre(userResponse.getGenre())
        .build();

    MovieSearchResponse movieSearchResponse = movieStub.getMovies(movieSearchRequest);

    return movieSearchResponse.getMovieList()
        .stream().map(movieDto -> new RecommendedMovie(movieDto.getTitle(), movieDto.getYear(), movieDto.getRating(),movieDto.getGenre().toString()))
        .collect(Collectors.toList());
  }

  public void setUserGenre(UserGenre userGenre) {
    UserGenreUpdateRequest userGenreUpdateRequest = UserGenreUpdateRequest.newBuilder()
        .setId(userGenre.getId())
        .setGenre(Genre.valueOf(userGenre.getGenre()))
        .build();

    userStub.updateUserGenre(userGenreUpdateRequest);
  }
}
