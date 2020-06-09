package no.nav.tekniskdemo;




import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import no.nav.tekniskdemo.chat.Chat;
import no.nav.tekniskdemo.chat.ChatServiceGrpc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("restriction")
public class ChatClient extends Application {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;
    private static final String FX_FONT_24_ARIAL = "-fx-font: 24 arial;";

    private ObservableList<String> messages = FXCollections.observableArrayList();
    private ListView<String> messagesView = new ListView<>();
    private TextField name = new TextField("Unamed Java");
    private TextField message = new TextField();
    private Button send = new Button();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        setupAndShowPrimaryStage(primaryStage);

        // Create a channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        // Create an async stub with the channel
        ChatServiceGrpc.ChatServiceStub chatService = ChatServiceGrpc.newStub(channel);

        // Open a connection to the server
        StreamObserver<Chat.ChatMessage> chat = chatService.chat(new StreamObserver<>() {

            @Override
            public void onNext(Chat.ChatMessageFromServer value) {
                // Display the message
                Platform.runLater(() -> {
                    LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(value.getTimestamp().getSeconds(), value.getTimestamp().getNanos()), ZoneId.systemDefault());
                    String tt = time.format(DateTimeFormatter.ofPattern("HH:mm"));
                    messages.add(tt + " " + value.getMessage().getFrom() + ": " + value.getMessage().getMessage());
                    messagesView.scrollTo(messages.size());
                });
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(System.err);
                Platform.runLater(() -> {
                    messages.add("System error: " + t.getMessage());
                    messagesView.scrollTo(messages.size());
                });
                System.out.println("Disconnected");
            }

            @Override
            public void onCompleted() {
                System.out.println("Disconnected");
            }
        });

        // Send button handler, create a message and send.
        send.setOnAction(e -> {

            // Create a message
            Chat.ChatMessage chatMessage = Chat.ChatMessage.newBuilder()
                    .setFrom(name.getText())
                    .setMessage(message.getText())
                    .build();

            // Send the message
            chat.onNext(chatMessage);

            message.setText("");
        });

        primaryStage.setOnCloseRequest(e -> {
            chat.onCompleted();
            channel.shutdown();
        });
    }

    private void setGlobalEventHandler(Node root) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                send.fire();
                ev.consume();
            }
        });
    }

    private void setupAndShowPrimaryStage(Stage primaryStage) {
        messagesView.setItems(messages);

        name.setStyle(FX_FONT_24_ARIAL);
        message.setStyle(FX_FONT_24_ARIAL);
        send.setStyle(FX_FONT_24_ARIAL);
        messagesView.setStyle(FX_FONT_24_ARIAL);

        send.setText("Send");

        BorderPane pane = new BorderPane();
        pane.setLeft(name);
        pane.setCenter(message);
        pane.setRight(send);

        BorderPane root = new BorderPane();
        root.setCenter(messagesView);
        root.setBottom(pane);
        setGlobalEventHandler(root);

        primaryStage.setTitle("gRPC Chat i Arena 🙃");
        primaryStage.setScene(new Scene(root, 700, 520));

        primaryStage.show();
    }
}