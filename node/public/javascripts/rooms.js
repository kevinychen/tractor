function joinRoomFinish(roomname) {
    $('#main').hide();
    $('#main').slideDown();
    $('#roomname').text(roomname);
}

$(document).ready(function() {
    // Login/register functions
    if ($('#registerlink').length) {
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
    } else {
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

    // Create/remove room functions
    var socket = io.connect('http://' + window.location.host);
    socket.emit('hello', {username: username});
    socket.on('rooms', function(data) {
        var html = '';
        for (var i = 0; i < data.length; i++) {
            var id = data[i].id;
            var roomname = data[i].roomname;
            var members = data[i].usernames ? data[i].usernames.split(',') : [];
            var numMembers = members ? members.length : 0;
            html += '<li id="room' + id + 'link" class="link">' +
                '<span id="room' + id + 'name">' + roomname + '</span>' +
                '<span id="room' + id + 'info" class="info roominfo">' +
                '<b>' + roomname + '</b><br/>' +
                'Members: ' + members.join() + '<br/>' +
                'Num people: ' + numMembers + '<br/>' +
                'Status: ' + data[i].status + '<br/>' +
                ($.inArray(username, members) == -1 ?
                '<span id="room' + id + 'join" class="blue button join">join</span>' :
                '<span class="cancel button leave">leave</span>') +
                '</span>' +
                '<style>' +
                '#room' + id + 'link:hover #room' + id + 'info {' +
                    'display: block;' +
                '}' +
                '</style>' +
                '</li>';
        }
        $('#roomsul').html(html);
        $('.join').on('click', function(e) {
            var id = e.target.id;
            var roomname = $('#' + id.slice(0, -4) + 'name').text();
            $.post('/joinroom', {roomname: roomname});
        });
        $('.leave').on('click', function(e) {
            $.post('/leaveroom');
        });
    });
    socket.on('removeroom', function(data) {
        $('#room' + data.id + 'link').remove();
    });
    socket.on('joinroom', function(data) {
        joinRoomFinish(data.roomname);
        $('#roomname').text(data.roomname);
        startService(data);
    });
    socket.on('leaveroom', function() {
        $('#main').slideUp();
        endService();
    });

    $('#roomcreate').on('click', function() {
        $('#createpopup').show();
        $('#content').css('opacity', '0.5');
        $('#createclose').on('click', function() {
            $('#createpopup').hide();
            $('#content').css('opacity', 1);
        });
        $('#createbutton').on('click', function() {
            $('#createloading').show();
            $.post('/joinroom', {roomname: $('#createroomname').val()},
                function(data) {
                    if (data.error) {
                        $('#createalert').text(data.error);
                    } else {
                        joinRoomFinish(data.roomname);
                        $('#createloading').hide();
                        $('#createpopup').hide();
                        $('#content').css('opacity', 1);
                    }
                });
        });
        $('#createroomname').focus();
    });
    $('#roomleave').on('click', function() {
        $.post('/leaveroom');
    });
});
