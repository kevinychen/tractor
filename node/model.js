var config = require('./config').config;
var mysql = require('mysql2');
var bcrypt = require('bcrypt');
var url = 'mysql://' + config.db_host + ':3306/' + config.db_name +
        '?user=' + config.db_user + '&password=' + config.db_password;
var pool = mysql.createPool(url);

function Model() {
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
  execute('select * from `users` where username=?',
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
                execute('insert into `users` (username, password) values (?)',
                    [[username, hash]], callback);
            });
        }
    });
}

Model.prototype.login = function(username, password, callback) {
    this.getUser(username, function(err, user) {
        bcrypt.compare(password, user.password, function(err, authenticated) {
            callback(err || !authenticated);
        });
    });
}

module.exports.Model = new Model();
