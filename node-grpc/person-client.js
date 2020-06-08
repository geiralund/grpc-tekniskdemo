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
let person_service = grpc.loadPackageDefinition(packageDefinition).no.nav.tekniskdemo.person;

function main() {

    let client = new person_service.PersonService('localhost:50051',
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
            // let call = client.note()
            // call.write({id: response.id, message: "Hei"})
            //
            // call.on('data',function(response){
            //     console.log(response.message);
            // });
            //
            // call.on('end',function(){
            //     console.log('Ferdig å føre notater');
            // });
            // call.end()

        }
        if (err) {
            console.error('Failed to create person');
            throw err;
        }
    });
}

main();
