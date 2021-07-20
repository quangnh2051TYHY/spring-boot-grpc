package server;

import calculator.UnaryCalculatorService;
import io.FileUploadService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import service.ImageService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        ImageService imageService = new ImageService();

        Server server = ServerBuilder
                .forPort(8080)
                .addService(imageService)
                .build();
        // start
        server.start();
        System.out.println("Start server at port " + server.getPort());
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("gRPC server is shutting down!");
            server.shutdown();
        }));

        // wait for 1 hr
        server.awaitTermination(1, TimeUnit.HOURS);

    }

}
