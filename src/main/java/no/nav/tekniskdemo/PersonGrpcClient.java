package no.nav.tekniskdemo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import no.nav.tekniskdemo.person.Person;
import no.nav.tekniskdemo.person.PersonId;
import no.nav.tekniskdemo.person.PersonRequest;
import no.nav.tekniskdemo.person.PersonServiceGrpc;

public final class PersonGrpcClient {

    private final PersonServiceGrpc.PersonServiceBlockingStub blockingStub;

    public PersonGrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    private PersonGrpcClient(ManagedChannel channel) {
        this.blockingStub = PersonServiceGrpc.newBlockingStub(channel);
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

    public static void main(String[] args) {
        final String host = "localhost";
        final int port = 50051;
        PersonGrpcClient client = new PersonGrpcClient(host, port);
        final PersonId id = client.createPerson("Navn Navnesen");

        System.out.println(client.getPerson(id));
    }

}
