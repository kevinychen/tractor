
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

function emitMsg(arr) {
    gameSocket.send(arr.join('__'));
}

function declareStatus(roomname) {
    emitMsg(['STATUS', roomname, username,
            $('#gamestatusnumdecks').val(),
            $('#gamestatusfindafriend').prop('checked')]
           );
}

function declareBeginGame(roomname) {
    emitMsg(['BEGINGAME', roomname, username]);
}

function startService(params) {
    endService();
    gameSocket = new WebSocket('ws://' + params.service);
    if (gameSocket.readyState > 1) {  // CLOSED or CLOSING
        alert("Unable to connect to room server.");
        return;
    }
    gameSocket.onopen = function() {
        emitMsg(['HELLO', params.roomname, username]);
        var html = '';
        html += 'Num Decks: 1<input id="gamestatusnumdecks" type="range" min="1" max="4"" />4<br/>';
        html += 'Find a friend: <input id="gamestatusfindafriend" type="checkbox" /><br/>';
        html += 'Game status: <span id="gamestatusstatus"></span><br/>';
        html += '<ul id="gamestatusplayers"></ul>';
        html += '<span id="gamebegin" class="blue button">begin game</span>';
        $('#gameintro').html(html);
        $('#gamestatusnumdecks').bind('input', function() {
            declareStatus(params.roomname);
        });
        $('#gamestatusfindafriend').click('change', function() {
            declareStatus(params.roomname);
        });
        $('#gamebegin').on('click', function() {
            declareBeginGame(params.roomname);
        });
    };
    gameSocket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (data.error) {
            showMsg($('#roomerror'), data.error);
        } else if (data.notification) {
            showMsg($('#roomnotification'), data.notification);
        } else if (data.status) {
            $('#gameintro').show();
            $('#gameshow').hide();
            $('#gamestatusnumdecks').val(data.status.properties.numDecks);
            $('#gamestatusfindafriend').prop('checked', data.status.properties.find_a_friend);
            $('#gamestatusstatus').text(data.status.status);
            var playerList = '';
            for (var i = 0; i < data.status.members.length; i++) {
                playerList += '<li>Player ' + (i + 1) + ': ' + data.status.members[i] + '</li>';
            }
            $('#gamestatusplayers').html(playerList);
        } else if (data.begin) {
            $('#gameintro').hide();
            $('#gameshow').slideDown();
        }
    };
}

function endService() {
    if (gameSocket) {
        gameSocket.close();
    }
}
