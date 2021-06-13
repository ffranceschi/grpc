package me.ffranceschi.cliente.server;

import io.grpc.stub.StreamObserver;
import me.ffranceschi.cliente.*;

public class ClienteServiceImpl extends ClienteServiceGrpc.ClienteServiceImplBase {

    @Override
    public void cliente(ClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        Cliente cliente = request.getCliente();
        String nome = cliente.getNome();
        String result = "Cliente com nome: " + nome;
        ClienteResponse response = ClienteResponse.newBuilder().setResult(result).build();

        // Manda resposta
        responseObserver.onNext(response);

        // Termina requisicao RPC
        responseObserver.onCompleted();
    }

    // Server streaming
    @Override
    public void clienteMultiplasVezes(ClienteMultiplasVezesRequest request, StreamObserver<ClienteMultiplasVezesResponse> responseObserver) {
        String nome = request.getCliente().getNome();
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Nome: " + nome + "resposta: " + i;
                ClienteMultiplasVezesResponse response = ClienteMultiplasVezesResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    // Client Streaming
    @Override
    public StreamObserver<VariasChamadasClienteRequest> variasChamadasCliente(StreamObserver<VariasChamadasClienteResponse> responseObserver) {
        StreamObserver<VariasChamadasClienteRequest> requestStreamObserver = new StreamObserver<VariasChamadasClienteRequest>() {
            String result = "";
            @Override
            public void onNext(VariasChamadasClienteRequest variasChamadasClienteRequest) {
                result += "Essa mensagem eh do cliente: " + variasChamadasClienteRequest.getCliente().getNome() + "!\n";
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        VariasChamadasClienteResponse.newBuilder().setResult(result).build()
                );
                responseObserver.onCompleted();
            }
        };
        return requestStreamObserver;
    }

    @Override
    public StreamObserver<BiDiClienteRequest> biDiCliente(StreamObserver<BiDiClienteResponse> responseObserver) {
        StreamObserver<BiDiClienteRequest> requestStreamObserver = new StreamObserver<BiDiClienteRequest>() {
            @Override
            public void onNext(BiDiClienteRequest biDiClienteRequest) {
                String result = "Cliente eh: " + biDiClienteRequest.getCliente().getNome();
                BiDiClienteResponse biDiClienteResponse = BiDiClienteResponse.newBuilder()
                        .setResult(result).build();
                responseObserver.onNext(biDiClienteResponse);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
        return requestStreamObserver;
    }
}
