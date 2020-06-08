let PROTO_PATH = __dirname + '/../src/main/proto/Chat.proto';

let grpc = require('grpc');
let protoLoader = require('@grpc/proto-loader');
let packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {keepCase: true,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
    });
let chat_service = grpc.loadPackageDefinition(packageDefinition).no.nav.tekniskdemo.chat;


function main() {
    let user;
    if (process.argv.length >= 3) {
        user = process.argv[2];
    } else {
        user = 'Unamed Node';
    }
    let readline = require('readline');
    let rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
        terminal: true
    });

    let client = new chat_service.ChatService('localhost:50051',
        grpc.credentials.createInsecure());
    let call = client.chat()

    call.on('data',function(response){
        console.log(`Fra: '${response.message.from}' -> ${response.message.message} `)
    });

    call.on('end', function () {
        console.log('Chat ended');
    });

    rl.on('line', function(line){
        call.write({from: user, message: line})
    })

    rl.on('SIGINT', () => {
        rl.question('Are you sure you want to exit? ', (answer) => {
            if (answer.match(/^y(es)?$/i)) {
                rl.pause();
                call.end();
                process.exit(0);
            }
        });
    });

}

main();