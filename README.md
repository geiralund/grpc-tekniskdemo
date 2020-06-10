# grpc-tekniskdemo
Kodesnutter for teknisk demo 9. januar 2020

## PersonService

[Person.proto](src/main/proto/Person.proto) tjenestedefinisjon (IDL) for PersonService  

Se [PersonGrpcServer.java](src/main/java/PersonGrpcServer.java) serverimplementasjon av tjenestedefinisjonen (Java)  
Se [PersonGrpcClient.java](src/main/java/PersonGrpcClient.java) clientimplementasjon av tjenestedefinisjonen (Java)  
Se [GrpcServer.kt](src/main/kotlin/GrpcServer.java) serverimplementasjon av tjenestedefinisjonen (Kotlin)  
Se [person-client.js](node-grpc/person-client.js) clientimplementasjon av tjenestedefinisjonen (Node.js)  

## ChatService

[Chat.proto](src/main/proto/Chat.proto) tjenestedefinisjon (IDL) for ChatService  
Se [GrpcServer.kt](src/main/kotlin/GrpcServer.java) serverimplementasjon av tjenestedefinisjonen (Kotlin)  
Se [person-client.js](node-grpc/person-client.js) clientimplementasjon av tjenestedefinisjonen (Node.js)  
Se [ChatClient.java](src/main/java/ChatClient.java) clientimplementasjon av tjenestedefinisjonen (Java)  


## Howto - BUILD AND RUN  

### jvm  
Bygg jvm prosjektene med `./gradlew clean build`
Kjør [ChatClient.java](src/main/java/ChatClient.java) med `./gradlew run`
Kjør serverimplementasjonen fra IDE

### node.js
Gå til `node-grpc` mappa 
`npm install`

Kjør [person-client.js](node-grpc/person-client.js) via `node person-client.js 'navn på person '`
Kjør [chat-client.js](node-grpc/chat-client.js) via `node chat-client.js 'chat-avatar-navn'`




