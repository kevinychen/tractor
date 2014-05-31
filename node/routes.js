var model = require('./model').Model;

exports.home = function(req, res) {
    var complete = function() {
        model.getUser(req.session.username, function(err, user) {
            req.session.room = user.room;
            res.render('home.ejs', req.session);
        });
    };
    // Create guest username, if not logged in
    if (!req.session.username) {
        var guestId = 'guest' + Math.floor(Math.random() * 1000000000);
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
    model.joinRoom(req.session.username, req.body.roomname, function() {});
    model.getService(req.body.roomname, function(err, service) {
        res.json({error: err, service: service});
    });
};

exports.leave = function(req, res) {
    model.joinRoom(req.session.username, '', function() {});
};
