var model = require('./model').Model;

exports.rooms = function(req, res) {
    if (req.session.username) {
        res.render('rooms.ejs', {
            username: req.session.username, 
            isGuest: false
        });
    } else {
        res.render('rooms.ejs', {
            username: 'guest',
            isGuest: true
        });
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
        }
        res.redirect('/rooms');
    });
};

exports.logout = function(req, res) {
    req.session.regenerate(function() {
        res.redirect('/rooms');
    });
};

