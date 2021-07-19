package org.example.movie.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.movie.entity.Movie;
import org.example.movie.repository.MovieRepository;
import org.example.proto.common.Genre;
import org.example.proto.movie.MovieDto;
import org.example.proto.movie.MovieSearchRequest;
import org.example.proto.movie.MovieSearchResponse;
import org.example.proto.movie.MovieServiceGrpc;
import org.example.proto.user.UserResponse;
import org.example.proto.user.UserSearchRequest;
import org.example.proto.user.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class MovieService extends MovieServiceGrpc.MovieServiceImplBase {

    @Autowired
    private MovieRepository movieRepository;

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1998)
            .usePlaintext()
            .build();
    private UserServiceGrpc.UserServiceBlockingStub userStub = UserServiceGrpc.newBlockingStub(channel);

    @Override
    public void getMovies(MovieSearchRequest request, StreamObserver<MovieSearchResponse> responseObserver) {
//    String genre = request.getGenre().toString();
//    List<Movie> movieList = movieRepository.findByGenre(genre);
//    List<MovieDto> movieDtoList = new ArrayList<>();
//    movieList.forEach(movie -> {
//      MovieDto movieDto = MovieDto.newBuilder()
//          .setRating(movie.getRating())
//          .setTitle(movie.getTitle())
//          .setYear(movie.getYear())
//          .build();
//      movieDtoList.add(movieDto);
//    });
//
//    MovieSearchResponse movieSearchResponse = MovieSearchResponse
//        .newBuilder()
//        .addAllMovie(movieDtoList)
//        .build();
//
        UserSearchRequest userSearchRequest = UserSearchRequest.newBuilder().setId("1")
                .build();
        UserResponse userResponse =  userStub.getUser(userSearchRequest);
        System.out.println(userResponse);

        System.out.println(request);
        System.out.println(request.getGenre());
        List<MovieDto> movieDtoList = movieRepository.findByGenre(request.getGenre().toString()).stream()
                .map(movie ->
                        MovieDto.newBuilder()
                                .setRating(movie.getRating())
                                .setTitle(movie.getTitle())
                                .setYear(movie.getYear())
                                .setGenre(Genre.valueOf(movie.getGenre()))
                                .build()
                ).collect(Collectors.toList());

        MovieSearchResponse movieSearchResponse = MovieSearchResponse
                .newBuilder()
                .addAllMovie(movieDtoList)
                .build();

        responseObserver.onNext(movieSearchResponse);
        responseObserver.onCompleted();
    }
}
