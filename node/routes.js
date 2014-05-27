var model = require('./model').Model;

exports.rooms = function(req, res) {
    var complete = function() {
        res.render('rooms.ejs', req.session);
    };
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
        res.redirect('/rooms');
    });
};

exports.logout = function(req, res) {
    req.session.regenerate(function() {
        res.redirect('/rooms');
    });
};

exports.joinroom = function(req, res) {
    model.joinRoom(req.session.username, req.body.roomname, function(err) {
        if (err) {
            res.json({error: 'System error'});
        } else {
            model.getRooms(function(err, rooms) {
                model.emit('rooms', rooms);
                res.json({error: false});
            });
        }
    });
}

