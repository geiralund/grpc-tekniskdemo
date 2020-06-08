package no.nav.tekniskdemo

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import no.nav.tekniskdemo.chat.Chat
import no.nav.tekniskdemo.chat.Chat.ChatMessageFromServer
import no.nav.tekniskdemo.chat.ChatServiceGrpc
import java.util.*


internal class ChatGrpcServer(
    port: Int
) {

    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ChatService())
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
                    // todo : do something!
                }

                override fun onCompleted() {
                    observers.remove(responseObserver);
                }

            }

        }

    }
}

fun main() {
    val chatServer = ChatGrpcServer(50051)
    chatServer.start()
}