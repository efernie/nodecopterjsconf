var socket;
var app = {
  newImage: '',
  takePicture: function () {
    app.pictureDefer = jQuery.Deferred();
    if ( typeof Android === 'object'  ) {
      Android.takePicture();
    } else {
      this.newImage = "data:image/jpeg;base64,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";
      this.pictureTaken();
    }
    return app.pictureDefer;
  },
  pictureTaken: function (picture) {
    app.pictureDefer.resolve();
    return app.pictureDefer.promise;
  }
};

// refreshButton.
if ( typeof Android === 'object' ) {
  socket = io.connect('http://192.168.1.2:3000');
} else {
  socket = io.connect('http://localhost:3000');
}

  socket.on('connected', function (data) {
    $('#socketInfo').text('connected');
    console.log(data);
    // socket.emit('my other event', { my: 'data' });
  });


$('#refresh').on('click', function(e){
  e.preventDefault();
  window.document.location.reload();
});

$('#takeoff').on('click', function(e){
  e.preventDefault();
  socket.emit('takeoff');
});

$('#land').on('click', function(e){
  e.preventDefault();
  socket.emit('landar');
});

$('#takePicture').on("click", function (e) {
  e.preventDefault();
  app.takePicture().then(function(){
    showPictures();
  });
})

showPictures = function showPictures() {
  var self = this;
  if ( typeof Android === 'object' ) {
    app.newImage = 'data:image/jpeg;base64,' + Android.getSelectedPicture();
  }

  $('#picture').append('<img src="' + app.newImage + '"/>');
      // var compiledHtml = self.compileImage();
      // $(compiledHtml).find('img').removeClass('hide');
      // $('#selectedPictures').prepend(compiledHtml);
}
