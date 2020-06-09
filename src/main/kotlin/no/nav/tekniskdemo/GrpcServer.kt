package no.nav.tekniskdemo

import de.huxhorn.sulky.ulid.ULID
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import no.nav.tekniskdemo.chat.Chat
import no.nav.tekniskdemo.chat.Chat.ChatMessageFromServer
import no.nav.tekniskdemo.chat.ChatServiceGrpc
import no.nav.tekniskdemo.person.Person
import no.nav.tekniskdemo.person.PersonId
import no.nav.tekniskdemo.person.PersonRequest
import no.nav.tekniskdemo.person.PersonServiceGrpcKt

internal class GrpcServer(
    port: Int
) {

    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ChatService())
        .addService(PersonService())
        .build()


    fun start() {
        server.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                stop()
            }
        )
        println("Starting chatserver")
        server.awaitTermination()
    }

    fun stop() {
        server.shutdown()
    }

    internal class PersonService : PersonServiceGrpcKt.PersonServiceCoroutineImplBase() {

        private val ulid = ULID()
        private val personStore = mutableMapOf<String, Person>()

        override suspend fun createPerson(request: PersonRequest): Person {
            return personStore.computeIfAbsent(ulid.nextULID()) { personId ->
                Person.newBuilder().apply {
                    name = request.name
                    id = PersonId.newBuilder().apply {
                        id = personId
                    }.build()
                }.build()
            }
        }

        override suspend fun getPerson(request: PersonId): Person {
            return personStore[request.id]
                ?: throw StatusException(Status.NOT_FOUND.withDescription("Person with id ${request.id} not found"))
        }

    }

    internal class ChatService : ChatServiceGrpc.ChatServiceImplBase() {
        private val observers =
            LinkedHashSet<StreamObserver<ChatMessageFromServer>>()


        override fun chat(responseObserver: StreamObserver<ChatMessageFromServer>): StreamObserver<Chat.ChatMessage> {
            observers.add(responseObserver)
            return object : StreamObserver<Chat.ChatMessage> {
                override fun onNext(value: Chat.ChatMessage) {
                    val message = ChatMessageFromServer
                        .newBuilder()
                        .setMessage(value)
                        .build()

                    for (observer in observers) {
                        observer.onNext(message)
                    }
                }

                override fun onError(t: Throwable?) {
                    // todo : something!
                }

                override fun onCompleted() {
                    observers.remove(responseObserver);
                }

            }

        }

    }
}

fun main() {
    val server = GrpcServer(50051)
    server.start()
}