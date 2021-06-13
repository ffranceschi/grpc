package me.ffranceschi.cliente.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.ffranceschi.cliente.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClienteClient {

    public static void main(String[] args) {
        System.out.println("Iniciando client gRPC");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        ClienteServiceGrpc.ClienteServiceBlockingStub clienteClient = ClienteServiceGrpc.newBlockingStub(channel);
//        unary(channel);
//        clientSincronoEServerStream(channel);
//        clientStreamEServerSincrono(channel);
        bidirecionalStream(channel);
        channel.shutdown();
    }

    private static void clientSincronoEServerStream(ManagedChannel channel) {
        ClienteServiceGrpc.ClienteServiceBlockingStub clienteClient = ClienteServiceGrpc.newBlockingStub(channel);
        ClienteMultiplasVezesRequest clienteMultiplasVezesRequest = ClienteMultiplasVezesRequest.newBuilder()
                .setCliente(Cliente.newBuilder().setNome("Fernando")).build();
        clienteClient.clienteMultiplasVezes(clienteMultiplasVezesRequest)
                .forEachRemaining(clienteMultiplasVezesResponse -> {
                    System.out.println(clienteMultiplasVezesResponse.getResult());
                });
    }

    // Cliente sincrono para Server sincrono
    private static void unary(ManagedChannel channel) {
        ClienteServiceGrpc.ClienteServiceBlockingStub clienteClient = ClienteServiceGrpc.newBlockingStub(channel);
        Cliente cliente = Cliente.newBuilder()
                .setNome("Fernando")
                .setCpf("99999999")
                .setTelefone(123123123)
                .build();
        ClienteRequest clienteRequest = ClienteRequest.newBuilder()
                .setCliente(cliente)
                .build();
        ClienteResponse response = clienteClient.cliente(clienteRequest);

        System.out.println("Resposta eh: " + response.getResult());
    }

    private static void clientStreamEServerSincrono(ManagedChannel channel) {
//        Sincrono Client
//        ClienteServiceGrpc.ClienteServiceBlockingStub clienteClient = ClienteServiceGrpc.newBlockingStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        // Assincronico client
        ClienteServiceGrpc.ClienteServiceStub clienteClientAssincrono = ClienteServiceGrpc.newStub(channel);

        StreamObserver<VariasChamadasClienteRequest> requestStreamObserver = clienteClientAssincrono.variasChamadasCliente(new StreamObserver<VariasChamadasClienteResponse>() {
            @Override
            public void onNext(VariasChamadasClienteResponse variasChamadasClienteResponse) {
                System.out.println("Recebida resposta do servidor: \n" + variasChamadasClienteResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("completa resposta do servidor!");
                latch.countDown();
            }
        });
        requestStreamObserver.onNext(VariasChamadasClienteRequest.newBuilder()
                .setCliente(Cliente.newBuilder()
                        .setNome("Fernando").build())
                .build());
        requestStreamObserver.onNext(VariasChamadasClienteRequest.newBuilder()
                .setCliente(Cliente.newBuilder()
                        .setNome("Priscila").build())
                .build());
        requestStreamObserver.onNext(VariasChamadasClienteRequest.newBuilder()
                .setCliente(Cliente.newBuilder()
                        .setNome("Katherine").build())
                .build());

        // Avisa que o servidor acabou de enviar todas as mensagens
        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void bidirecionalStream(ManagedChannel channel) {
        CountDownLatch latch = new CountDownLatch(1);

        // Assincronico client
        ClienteServiceGrpc.ClienteServiceStub biDiAssincrono = ClienteServiceGrpc.newStub(channel);

        StreamObserver<BiDiClienteRequest> requestStreamObserver = biDiAssincrono.biDiCliente(new StreamObserver<BiDiClienteResponse>() {
            @Override
            public void onNext(BiDiClienteResponse biDiClienteResponse) {
                System.out.println("Resposta do servidor: " + biDiClienteResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("completa resposta do servidor!");
                latch.countDown();
            }
        });

        Arrays.asList("Fernando", "Priscila", "Katherine").forEach(
                name -> requestStreamObserver.onNext(BiDiClienteRequest.newBuilder()
                        .setCliente(Cliente.newBuilder().setNome(name)).build())
        );
        requestStreamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
