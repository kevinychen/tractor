var config = require('./config').config;
var mysql = require('mysql2');
var bcrypt = require('bcrypt');
var url = 'mysql://' + config.db_host + ':3306/' + config.db_name +
        '?user=' + config.db_user + '&password=' + config.db_password;
var pool = mysql.createPool(url);

function Model() {
    var c1 = function() {
        execute('drop table if exists users', [], c2);
    }, c2 = function() {
        execute('create table users (' +
                    'id int auto_increment primary key, ' +
                    'username varchar(16), ' +
                    'password varchar(64), ' +
                    'room varchar(64), ' +
                    'online int' +
                    ')', [], c3);
    }, c3 = function() {
        execute('drop table if exists rooms', [], c4);
    }, c4 = function() {
        execute('create table rooms (' +
                'id int auto_increment primary key, ' +
                'roomname varchar(64) unique, ' +
                'service varchar(64)' +
                ')', [], c5);
    }, c5 = function() {
    };
    c1();
}

function execute(query, args, callback) {
  pool.getConnection(function(err, connection) {
    if (err) {
      callback(err);
    } else {
      connection.query(query, args, function(err, rows, fields) {
        connection.release();
        callback(err, rows, fields);
      });
    }
  });
}

Model.prototype.getUser = function(username, callback) {
  execute('select * from users where username=?',
      [username], function(err, rows) {
        if (err || rows.length == 0) {
          callback('Error obtaining user.');
        } else {
          callback(err, rows[0]);
        };
      });
}

Model.prototype.validateRegistration = function(username, password, callback) {
    if (!username.match(/^[A-Za-z0-9_]+$/)) {
        callback('Invalid characters.');
        return;
    }
    this.getUser(username, function(err, user) {
        if (user) {
            callback('Duplicate username.');
        } else {
            callback();
        }
    });
}

Model.prototype.register = function(username, password, callback) {
    bcrypt.genSalt(10, function(err, salt) {
        if (err) {
            callback(err);
        } else {
            bcrypt.hash(password, salt, function(err, hash) {
                execute('insert into users (username, password) values (?)',
                    [[username, hash]], callback);
            });
        }
    });
}

Model.prototype.login = function(username, password, callback) {
    this.getUser(username, function(err, user) {
        if (err) {
            callback(err);
            return;
        }
        bcrypt.compare(password, user.password, function(err, authenticated) {
            callback(err || !authenticated);
        });
    });
}

Model.prototype.getRooms = function(callback) {
    execute('select roomname, service from rooms', [], callback);
}

Model.prototype.getService = function(roomname, callback) {
    execute('select service from rooms where roomname=?',
            [roomname], function(err, room) {
                if (err) {
                    callback(err);
                } else if (room.length == 0) {
                    var index = Math.floor(Math.random() * config.services.length);
                    var service = config.services[index];
                    execute('insert into rooms (roomname, service) values (?)',
                        [[roomname, service]], function(err) {
                            callback(err, service);
                        });
                } else {
                    callback(false, room[0].service);
                }
            });
}

Model.prototype.joinRoom = function(username, roomname, callback) {
    execute('update users set room=? where username=?',
            [roomname, username], callback);
}

Model.prototype.emit = function(title, data) {
    var clients = thisio.sockets.clients();
    for (var i = 0; i < clients.length; i++) {
        clients[i].emit(title, data);
    }
}

module.exports.Model = new Model();
