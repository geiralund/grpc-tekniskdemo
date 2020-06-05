package no.nav.tekniskdemo;

import de.huxhorn.sulky.ulid.ULID;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import no.nav.tekniskdemo.greeting.Note;
import no.nav.tekniskdemo.greeting.Person;
import no.nav.tekniskdemo.greeting.PersonId;
import no.nav.tekniskdemo.greeting.PersonServiceGrpc;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public final class PersonGrpcServer {

    private static final ULID ulid = new ULID();

    private static class PersonService extends PersonServiceGrpc.PersonServiceImplBase {

        private Map<String, Person> personStore = new HashMap<>();
        private Set<Note> notes = new HashSet<>();

        public void createPerson(no.nav.tekniskdemo.greeting.PersonRequest request,
                                 io.grpc.stub.StreamObserver<no.nav.tekniskdemo.greeting.Person> responseObserver) {

            final Person person = personStore.computeIfAbsent(ulid.nextULID(), id -> Person.newBuilder()
                    .setName(request.getName())
                    .setId(PersonId.newBuilder().setId(id).build())
                    .build());

            System.out.println("Created person");

            responseObserver.onNext(person);
            responseObserver.onCompleted();
        }

        public void getPerson(no.nav.tekniskdemo.greeting.PersonId request,
                              io.grpc.stub.StreamObserver<no.nav.tekniskdemo.greeting.Person> responseObserver) {

            final Optional<Person> maybePerson = Optional.ofNullable(
                    personStore.get(request.getId()));

            if (maybePerson.isPresent()) {
                responseObserver.onNext(maybePerson.get());
            } else {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription(String.format("Person with id %s not found", request.getId()))));
            }
            responseObserver.onCompleted();


        }

        public StreamObserver<no.nav.tekniskdemo.greeting.Note> note(
                StreamObserver<no.nav.tekniskdemo.greeting.Note> responseObserver) {

            return new StreamObserver<Note>() {
                @Override
                public void onNext(Note note) {
                    final Optional<Person> maybePerson = Optional.ofNullable(personStore.get(note.getId().getId()));
                    maybePerson.ifPresent(person -> {
                                System.out.println("Got note " + note);
                                notes.add(note);
                                responseObserver.onNext(note);
                            }

                    );

                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace(System.err);
                    System.out.println("Note stream shutdown");
                    responseObserver.onError(t);

                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
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

