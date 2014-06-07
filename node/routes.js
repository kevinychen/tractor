var model = require('./model').Model;
var fs = require('fs');

var adverbs = []
var adjectives = []
var animals = []
loadWords();

function loadWords() {
    var advStr = fs.readFileSync('misc/adverbs', {encoding: 'utf8'});
    adverbs = advStr.toString().split("\n");
    var adjStr = fs.readFileSync('misc/adjectives', {encoding: 'utf8'});
    adjectives = adjStr.toString().split("\n");
    var animalStr = fs.readFileSync('misc/animals', {encoding: 'utf8'});
    animals = animalStr.toString().split("\n");
}

function getRandomUsername() {
    var username = "";
    username += adverbs[Math.floor(Math.random() * adverbs.length)];
    username += adjectives[Math.floor(Math.random() * adjectives.length)];
    username += animals[Math.floor(Math.random() * animals.length)];
    return username;
}

exports.home = function(req, res) {
    var complete = function() {
        model.getUser(req.session.username, function(err, user) {
            req.session.room = user.room;
            res.render('home.ejs', req.session);
        });
    };
    // Create guest username, if not logged in
    if (!req.session.username) {
        var guestId = getRandomUsername();
        req.session.username = guestId;
        req.session.isGuest = true;
        model.register(guestId, '', complete);
    } else {
        complete();
    }
}

exports.register = function(req, res) {
    var username = req.body.username;
    var password = req.body.password;
    model.validateRegistration(username, password, function(err) {
        if (err) {
            res.json({error: err});
        } else {
            model.register(username, password, function(err) {
                if (err) {
                    res.json({error: 'System error.'});
                } else {
                    res.json({error: false});
                }
            });
        }
    });
};

exports.login = function(req, res) {
    var username = req.body.username;
    var password = req.body.password;
    model.login(username, password, function(err) {
        if (!err) {
            req.session.username = username;
            req.session.isGuest = false;
        }
        res.redirect('/');
    });
};

exports.logout = function(req, res) {
    req.session.regenerate(function() {
        res.redirect('/');
    });
};

exports.rooms = function(req, res) {
    model.getRooms(function(err, rooms) {
        res.json({error: err, rooms: rooms});
    });
};

exports.join = function(req, res) {
    var user = req.session.username;
    var room = req.body.roomname;
    model.joinRoom(user, room, function() {});
    model.getService(room, function(err, service, serviceKey) {
        var auth = model.encodeMD5(room, serviceKey, user);
        res.json({error: err, service: service, auth: auth});
    });
};

exports.leave = function(req, res) {
    model.joinRoom(req.session.username, '', function() {});
};

exports.service = function(req, res) {
    model.addService(req.body, function(err) {
        res.json({error: err});
    });
};
