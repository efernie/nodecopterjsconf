var express = require('express'),
    cons = require('consolidate'),
    http = require('http'),
    app = express(),
    httpServer = http.Server(app),
    io = require('socket.io').listen(httpServer);

var arDrone = require('ar-drone');
var client = arDrone.createClient({
  ip: '192.168.1.2'
});
// console.log(client);
app.configure(function(){
  app.set('view engine','html');
  app.set('views', __dirname + '/views');

  app.use(express.compress());
  app.use(express.bodyParser());
  app.use(express.cookieParser());
  app.use(express.favicon());

  app.use(express.methodOverride());

  app.use(express.static(__dirname + '/views'));
  app.use(app.router);
});

httpServer.listen(3000, function () {
  console.log( 'app listening on port 3000 for http - ENV:');
});

io.configure('development', function(){
  io.set('log level', 1);
});

io.sockets.on('connection', function (socket) {
  socket.emit('connected', { hello: 'world' });

  socket.on('takeoff', function (data) {
    console.log('Take off');
    client.takeoff();
  });
  socket.on('landar', function(data) {
    console.log('land');
    client.stop();
    client.land();
  })
});



// client.takeoff();

// client
//   .after(5000, function() {
//     // this.clockwise(0.5);
//   })
//   .after(3000, function() {
//     this.stop();
//     this.land();
//   });