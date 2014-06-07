/**
 * Module dependencies.
 */

var config = require('./config').config;
var express = require('express');
var http = require('http');
var model = require('./model').Model;
var routes = require('./routes');

var app = express();

app.set('port', config.port);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(express.cookieParser());
app.use(express.session({secret: 'secret'}));
app.use(express.static(__dirname + '/public'));
app.use(app.router);

app.configure('development', function(){
  app.use(express.errorHandler());
});

app.get('/', routes.home);
app.post('/register', routes.register);
app.post('/login', routes.login);
app.get('/logout', routes.logout);

app.get('/rooms', routes.rooms);
app.post('/join', routes.join);
app.post('/leave', routes.leave);

app.post('/service', routes.service);

var server = http.createServer(app).listen(app.get('port'), function(){
    console.log("Express server listening on port " + app.get('port'));
});
