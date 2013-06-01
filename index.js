var express = require('express'),
    cons = require('consolidate'),
    http = require('http'),
    app = express(),
    httpServer = http.Server(app),
    io = require('socket.io').listen(httpServer);

var arDrone = require('ar-drone');
var client = arDrone.createClient({
  // ip: '192.168.1.2'
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

// client.takeoff(function() {
//   console.log('take off??');
// });

// client
//   .after(3000, function() {
//     this.stop();
//     this.land();
//   });
  // .after(5000, function() {
  //   // this.clockwise(0.5);
  // })

io.configure('development', function(){
  io.set('log level', 1);
});

io.sockets.on('connection', function (socket) {
  console.log('connected');
  socket.emit('connected', { hello: 'world' });

  socket.on('takeoff', function (data) {
    console.log('Take off');
    client.takeoff();
  });

  socket.on('upar', function (data) {
    console.log('up off');
    client.takeoff();
  });

  socket.on('downar', function (data) {
    console.log('down off');
    // client.takeoff();
  });

  socket.on('deltaX', function (data) {
    var normalized = (Math.abs(data.x) / 20 ).toFixed(2);
    // console.log('deltaX', data.x);
    // console.log('deltaX', (Math.abs(data.x) / 20 ).toFixed(2) );
    if ( normalized > 0.50 ) {
      // console.log('left');
      // client.left(normalized);
    } else {
      // client.right(normalized);
      // console.log('right');
    }

  });

  socket.on('deltaY', function (data) {
    var normalized = (Math.abs(data.y) / 20 ).toFixed(2);
    console.log('deltay', normalized );
    if ( normalized > 0.50 ) {
      // console.log('up?');
      client.up(normalized);
    } else {
      client.down(normalized);
      // console.log('down?');
    }
  });

  socket.on('deltaZ', function (data) {
    var normalized = (Math.abs(data.z) / 20 ).toFixed(2);
    // console.log('deltaZ', normalized );
    if ( normalized > 0.50 ) {
      // console.log('up');
    } else {
      // console.log('down');
    }
  });

  socket.on('landar', function(data) {
    console.log('land');
    client.stop();
    client.land();
  });


});
