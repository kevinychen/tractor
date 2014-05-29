
var gameSocket;
//var ctx = $('#gameshow')[0].getContext('2d');

function emitMsg(arr) {
    gameSocket.send(arr.join('__'));
}

function declareStatus(roomname) {
    emitMsg(['STATUS', roomname, username,
            $('#gamestatusnumdecks').val(),
            $('#gamestatusfindafriend').prop('checked')]
           );
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
        html += 'Num Decks: <input id="gamestatusnumdecks" type="text" /><br/>';
        html += 'Find a friend: <input id="gamestatusfindafriend" type="checkbox" /><br/>';
        html += 'Game status: <span id="gamestatusstatus"></span><br/>';
        html += 'Player list: <ul id="gamestatusplayers"></ul>';
        html += '<span id="gamebegin" class="blue button">begin game</span>';
        $('#gameintro').html(html);
        $('#gamestatusnumdecks').bind('input', function() {
            declareStatus(params.roomname);
        });
        $('#gamestatusfindafriend').click('change', function() {
            declareStatus(params.roomname);
        });
    };
    gameSocket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (data.status) {
            $('#gameintro').show();
            $('#gameshow').hide();
            $('#gamestatusnumdecks').val(data.status.properties.numDecks);
            $('#gamestatusfindafriend').prop('checked', data.status.properties.find_a_friend);
            $('#gamestatusstatus').text(data.status.status ? 'in-game' : 'awaiting players');
            var playerList = '';
            for (var i = 0; i < data.status.members.length; i++) {
                playerList += '<li>' + (i + 1) + ': ' + data.status.members[i] + '</li>';
            }
            $('#gamestatusplayers').html(playerList);
        }
    };
}

function endService() {
    if (gameSocket) {
        gameSocket.close();
    }
}
