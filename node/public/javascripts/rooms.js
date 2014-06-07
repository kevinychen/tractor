function addRegisterFunctions() {
    $('#registerlink').on('click', function() {
        $('#registerpopup').show();
        $('#content').css('opacity', '0.5');
        $('#registerusername').focus();
    });
    $('#registercancel').on('click', function() {
        $('#registerpopup').hide();
        $('#content').css('opacity', 1);
    });
    $('#registerbutton').on('click', function() {
        if ($('#registerpassword').val() != $('#registerconfirm').val()) {
            $('#registeralert').text('Your passwords do not match.');
        } else {
            $('#registerstatus').hide();
            $('#registerloading').show();
            $.post('/register', {
                username: $('#registerusername').val(),
                email: $('#registeremail').val(),
                password: $('#registerpassword').val()
            }, function(data) {
                if (data.error) {
                    $('#registeralert').text(data.error);
                    $('#registerstatus').show();
                    $('#registerloading').hide();
                } else {
                    $('#registerloading').text('Registration successful.');
                    $('#registeralert').text('');
                    $('#registerbutton').hide();
                    $('#registercancel').text('Close');
                }
            });
        }
    });
}

function addProfileFunctions() {
    $('#profilelink').on('click', function() {
        $('#profilepopup').show();
        $('#content').css('opacity', '0.5');
    });
    $('#profileclose').on('click', function() {
        $('#profilepopup').hide();
        $('#content').css('opacity', 1);
    });
    $('#logout').on('click', function() {
        window.location.href='/logout';
    });
}

function joinRoom(room, callback) {
    roomname = room;
    $.post('/join', {roomname: room}, function(data) {
        $('#main').hide();
        $('#main').slideDown();
        $('#roomname').text(room);
        queryRoomsList();
        if (data.error) {
            showMsg($('#roomerror'), data.error);
        } else {
            setMainService(data.service, room, data.auth);
        }
        callback();
    });
}

function leaveRoom() {
    roomname = '';
    $('#main').slideUp();
    $.post('/leave');
    endMainService();
    queryRoomsList();
}

function addRoomFunctions() {
    $('#roomcreate').on('click', function() {
        $('#createpopup').show();
        $('#content').css('opacity', '0.5');
        $('#createroomname').focus();
    });
    $('#createclose').on('click', function() {
        $('#createpopup').hide();
        $('#content').css('opacity', 1);
    });
    $('#createbutton').on('click', function() {
        $('#createloading').show();
        joinRoom($('#createroomname').val(), function() {
            $('#createloading').hide();
            $('#createpopup').hide();
            $('#content').css('opacity', 1);
        });
    });
    $('#roomleave').on('click', function() {
        leaveRoom();
    });
}

function queryRoomsList() {
    $.get('/rooms', function(data) {
        if (data.error) {
            $('#roomsul').html('<li>Error with retrieving rooms</li>');
        } else {
            var html = '';
            for (var i = 0; i < data.rooms.length; i++) {
                var room = data.rooms[i];
                html += '<li id="room' + i + 'link" class="getroominfo link">' +
                        '<span>' + room.roomname + '</span>' +
                        '<span id="room' + i + 'info" class="info roominfo">' +
                                'Retrieving room status...' +
                        '</span>' +
                        '<style>' +
                        '#room' + i + 'link:hover #room' + i + 'info {' +
                            'display: block;' +
                        '}' +
                        '</style>' +
                        '</li>';
            }
            $('#roomsul').html(html);
            $('.getroominfo').mouseenter(function(e) {
                var i = parseInt(e.target.id.slice(4, -4));  // room??link
                var room = data.rooms[i];
                if (!room) {
                    return;  // TODO investigate this error
                }
                queryRoomStatus(room.roomname, room.service, function(err, status) {
                    if (err) {
                        $('#room' + i + 'info').text('Service error.');
                    } else {
                        var html = '';
                        html += '<b>' + status.roomname + '</b><br/>';
                        html += 'Members: ' + status.members.join() + '<br/>';
                        html += 'Num people: ' + status.members.length + '<br/>';
                        html += 'Status: ' + status.status + '<br/>';
                        if (roomname == status.roomname) {
                            html += '<span id="room' + i + 'leave" class="cancel button">leave</span>';
                        } else {
                            html += '<span id="room' + i + 'join" class="blue button">join</span>';
                        }
                        $('#room' + i + 'info').html(html);
                        $('#room' + i + 'join').on('click', function() {
                            joinRoom(room.roomname, function() {});
                        });
                        $('#room' + i + 'leave').on('click', function() {
                            leaveRoom();
                        });
                    }
                });
            });
        }
    });
}

$(document).ready(function() {
    if ($('#registerlink').length) {
        // if register link exists
        addRegisterFunctions();
    } else {
        // if profile link exists
        addProfileFunctions();
    }

    addRoomFunctions();

    var queryTimer = function() {
        queryRoomsList();
        setTimeout(queryTimer, 30000);
    };
    queryTimer();

    if (roomname) {
        joinRoom(roomname, function() {});
    }
});
