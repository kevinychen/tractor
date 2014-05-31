
var gameSocket;
var ctx;

$(document).ready(function() {
    ctx = $('#gameshow')[0].getContext('2d');
});

function showMsg(obj, err) {
    obj.text(err);
    setTimeout(function() {
        if (obj.text() == err) {
            obj.text('');
        }
    }, 3000);
}

function sendMsg(conn, arr) {
    conn.send(arr.join('__'));
}

function queryRoomStatus(roomname, service, callback) {
    var ws = new WebSocket('ws://' + service);
    if (ws.readyState > 1) {  // CLOSED or CLOSING
        callback('Service error');
        return;
    }
    ws.onopen = function() {
        sendMsg(ws, ['QUERYROOM', roomname]);
    };
    ws.onmessage = function(msg) {
        callback(false, JSON.parse(msg.data));
    };
}

function declareStatus(roomname) {
    sendMsg(gameSocket, ['STATUS', roomname, username,
            $('#gamestatusnumdecks').val(),
            $('#gamestatusfindafriend').prop('checked')]
           );
}

function declareBeginGame(roomname) {
    sendMsg(gameSocket, ['BEGINGAME', roomname, username]);
}

function attachControl(label, func) {
    $('#gamecontrol').off('click');
    $('#gamecontrol').text(label);
    $('#gamecontrol').on('click', func);
}

var gameSocket;
function setMainService(service, roomname) {
    endMainService();
    gameSocket = new WebSocket('ws://' + service);
    if (gameSocket.readyState > 1) {  // CLOSED or CLOSING
        alert('Upable to connect to room server.');
        return;
    }

    var html = '';
    html += 'Num Decks: 1<input id="gamestatusnumdecks" type="range" min="1" max="4"" />4<br/>';
    html += 'Find a friend: <input id="gamestatusfindafriend" type="checkbox" /><br/>';
    html += 'Game status: <span id="gamestatusstatus"></span><br/>';
    html += '<ul id="gamestatusplayers"></ul>';
    $('#gameintro').html(html);
    $('#gamestatusnumdecks').bind('input', function() {
        declareStatus(roomname);
    });
    $('#gamestatusfindafriend').click('change', function() {
        declareStatus(roomname);
    });
    attachControl('begin game', function() {
        declareBeginGame(roomname);
    });

    gameSocket.onopen = function() {
        sendMsg(gameSocket, ['HELLO', roomname, username]);
    };
    gameSocket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (data.error) {
            showMsg($('#roomerror'), data.error);
        }
        if (data.notification) {
            showMsg($('#roomnotification'), data.notification);
        }
        if (data.begin) {
            $('#gameintro').hide();
            $('#gamecanvas').slideDown();
        }
        if (data.gameStarted) {
            attachControl('new round', function() {
                sendMsg(gameSocket, ['NEWROUND', roomname, username]);
            });
        } else if (data.status) {
            $('#gameintro').show();
            $('#gamecanvas').hide();
            $('#gamestatusnumdecks').val(data.status.properties.numDecks);
            $('#gamestatusfindafriend').prop('checked', data.status.properties.find_a_friend);
            $('#gamestatusstatus').text(data.status.status);
            var playerList = '';
            for (var i = 0; i < data.status.members.length; i++) {
                playerList += '<li>Player ' + (i + 1) + ': ' + data.status.members[i] + '</li>';
            }
            $('#gamestatusplayers').html(playerList);
        }
    };
}

function endMainService() {
    if (gameSocket) {
        gameSocket.close();
    }
}

