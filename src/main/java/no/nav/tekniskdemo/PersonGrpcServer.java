package no.nav.tekniskdemo;

import de.huxhorn.sulky.ulid.ULID;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import no.nav.tekniskdemo.person.Person;
import no.nav.tekniskdemo.person.PersonId;
import no.nav.tekniskdemo.person.PersonServiceGrpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public final class PersonGrpcServer {

    private static final ULID ulid = new ULID();

    private static class PersonService extends PersonServiceGrpc.PersonServiceImplBase {

        private Map<String, Person> personStore = new HashMap<>();

        public void createPerson(no.nav.tekniskdemo.person.PersonRequest request,
                                 io.grpc.stub.StreamObserver<no.nav.tekniskdemo.person.Person> responseObserver) {

            final Person person = personStore.computeIfAbsent(ulid.nextULID(), s -> {
                System.out.println("Created person '" + request.getName() + "'");
                return Person.newBuilder()
                        .setName(request.getName())
                        .setId(PersonId.newBuilder().setId(s).build())
                        .build();
            });

            responseObserver.onNext(person);
            responseObserver.onCompleted();
        }

        public void getPerson(no.nav.tekniskdemo.person.PersonId request,
                              io.grpc.stub.StreamObserver<no.nav.tekniskdemo.person.Person> responseObserver) {
            final Optional<Person> maybePerson = Optional.ofNullable(
                    personStore.get(request.getId()));

            if (maybePerson.isPresent()) {
                responseObserver.onNext(maybePerson.get());
            } else {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription(String.format("Person with id %s not found", request.getId()))));
            }
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final int port = 50051;
        PersonService service = new PersonService();
        final Server server = ServerBuilder.forPort(port).addService(service).build().start();
        System.out.println("Starting server");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown server");
                // try graceful shutdown
                server.shutdown();
                try {
                    // Wait for RPCs to complete processing
                    if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                        // That was plenty of time. Let's cancel the remaining RPCs
                        server.shutdownNow();
                        // shutdownNow isn't instantaneous, so give a bit of time to clean resources up
                        // gracefully. Normally this will be well under a second.
                        server.awaitTermination(5, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException ex) {
                    server.shutdownNow();
                }
            }
        });
        server.awaitTermination();
    }
}

