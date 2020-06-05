package no.nav.tekniskdemo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import no.nav.tekniskdemo.greeting.*;

import java.util.concurrent.TimeUnit;

public final class PersonGrpcClient {

    private final PersonServiceGrpc.PersonServiceBlockingStub blockingStub;
    private final PersonServiceGrpc.PersonServiceStub asyncStub;
    private final ManagedChannel channel;
    private final StreamObserver<no.nav.tekniskdemo.greeting.Note> requestObserver;

    public PersonGrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    private PersonGrpcClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = PersonServiceGrpc.newBlockingStub(channel);
        asyncStub = PersonServiceGrpc.newStub(channel);
        requestObserver = asyncStub.note(new StreamObserver<Note>() {
            @Override
            public void onNext(Note value) {

                System.out.println("Got note " + value);

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

    }

    public PersonId createPerson(final String name) {



        try {
            Person person = blockingStub.createPerson(PersonRequest.newBuilder().setName(name).build());
            return person.getId();
        } catch (StatusRuntimeException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    public Person getPerson(final PersonId id) {
        try {
            return blockingStub.getPerson(id);
        } catch (StatusRuntimeException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    public void note(PersonId id, String message) {
        requestObserver.onNext(Note.newBuilder().setId(id).setMessage(message).build());
    }


    public static void main(String[] args) throws InterruptedException {
        final String host = "localhost";
        final int port = 50051;
        PersonGrpcClient client = new PersonGrpcClient(host, port);
        final PersonId id = client.createPerson("Geir");

        System.out.println(client.getPerson(id));
        client.note(id, "HEI SVEIS ");
        TimeUnit.SECONDS.sleep(2);
        for (int i = 0;  i < 100; i++) {
            client.note(id, "HEI SVEIS " + i);
        }







    }

}
