package me.ffranceschi.cliente.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ClienteServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Iniciando gRPC Server");
        final Server server = ServerBuilder.forPort(50051)
                .addService(new ClienteServiceImpl())
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Pedido de parada detectado");
            server.shutdown();
            System.out.println("Servidor parado com sucesso!");
        }));

        server.awaitTermination();
    }

}
