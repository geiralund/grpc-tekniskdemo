let PROTO_PATH = __dirname + '/../src/main/proto/Person.proto';

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
let person = grpc.loadPackageDefinition(packageDefinition).person;

function main() {

    let client = new person.PersonService('localhost:50051',
        grpc.credentials.createInsecure());
    let user;
    if (process.argv.length >= 3) {
        user = process.argv[2];
    } else {
        user = 'Unamed...';
    }


    client.createPerson({name: user}, function (err, response) {
        if (response) {
            console.log('Created person:', response.name);
        }
        if (err) {
            console.error('Failed to create person');
            throw err;
        }
    });
}

main();
