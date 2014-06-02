
var gameSocket;
var ctx;
var width, height;
var imgWidth, imgHeight;

$(document).ready(function() {
    var canvas = $('#gameshow')[0];
    ctx = canvas.getContext('2d');
    width = canvas.width;
    height = canvas.height;
    ctx.translate(width / 2, height / 2);
    loadCardImages();
    addCardSelectionListener(canvas);
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

function sendConv(command, args) {
    // convenience method
    sendMsg(gameSocket, [command, roomname, username].concat(args));
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
    sendConv('STATUS', [
            $('#gamestatusnumdecks').val(),
            $('#gamestatusfindafriend').prop('checked')
            ]);
}

function declareBeginGame(roomname) {
    sendConv('BEGINGAME', []);
}

function attachControl(label, func) {
    $('#gamecontrol').off('click');
    $('#gamecontrol').text(label);
    $('#gamecontrol').on('click', func);
}

var gameSocket;
var cardValues = new Object();
var selectedCards = new Object();
var cardImages = new Object();
var showPrev = false;
var cache;

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
        sendConv('HELLO', []);
    };
    gameSocket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (data.error) {
            showMsg($('#roomerror'), data.error);
        }
        if (data.notification) {
            showMsg($('#roomnotification'), data.notification);
        }
        if (data.card) {
            cardValues[data.card.id] = data.card;
        }
        if (data.gameStarted) {
            $('#gameintro').hide();
            if ($('#gamecanvas').is(':hidden')) {
                $('#gamecanvas').slideDown();
            }
            drawGame(data.status, data.game);
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

function loadCardImages() {
    var suits = {SPADE: 's', HEART: 'h', DIAMOND: 'd', CLUB: 'c'};
    var values = {TWO: '2', THREE: '3', FOUR: '4', FIVE: '5',
        SIX: '6', SEVEN: '7', EIGHT: '8', NINE: '9', TEN: '10',
        JACK: 'j', QUEEN: 'q', KING: 'k', ACE: '1'};
    for (var suitName in suits) {
        cardImages[suitName] = new Object();
        for (var valueName in values) {
            cardImages[suitName][valueName] = new Image();
            cardImages[suitName][valueName].src =
                '/images/cards/' + suits[suitName] + values[valueName] + '.gif';
        }
    }
    cardImages.TRUMP = new Object();
    cardImages.TRUMP.SMALL_JOKER = new Image();
    cardImages.TRUMP.SMALL_JOKER.src = '/images/cards/jb.gif';
    cardImages.TRUMP.BIG_JOKER = new Image();
    cardImages.TRUMP.BIG_JOKER.src = '/images/cards/jr.gif';
    cardImages.BACK = new Image();
    cardImages.BACK.src = '/images/cards/b1fv.gif';
    cardImages.BACK.onload = function() {
        imgWidth = cardImages.BACK.width;
        imgHeight = cardImages.BACK.height;
    };
}

const TABLE_PLACE = 0.4;
const HAND_PLACE = 0.7;
const OUT_PLACE = 2.0;
const BIG_SEP = 20;
const MEDIUM_SEP = 15;
const SMALL_SEP = 10;
const SELECT_DIFF = 20;

function addCardSelectionListener(canvas) {
    canvas.onmousedown = function() { return false; };
    canvas.addEventListener('mousedown', function(e) {
        if (!cache.cards) {
            return;
        }
        var rect = canvas.getBoundingClientRect();
        var x = e.clientX - rect.left - width / 2;
        var y = e.clientY - rect.top - height / 2;
        var myIndex = $.inArray(username, cache.status.members);
        var hand = cache.game.hands[myIndex];
        var topCard = null;
        for (var i = 0; i < hand.length; i++) {
            if (hand[i] in cardValues) {
                var card = cardValues[hand[i]];
                if (card.goal &&
                    Math.abs(x - card.goal.base.x) < imgWidth / 2 &&
                    Math.abs(y - card.goal.base.y) < imgHeight / 2 &&
                    (!topCard || cardCompareFunction(card, topCard) > 0)) {
                        topCard = card;
                    }
            }
        }
        if (topCard) {
            selectedCards[topCard.id] ^= 1;
            updateCardPositions();
        }
    });
}

function setCardsGoal(myIndex, place, play, baseAngle) {
    // myIndex: index of myself in the status.members array
    // place: 'table', 'hand', 'out'
    var distance = place == 'table' ? TABLE_PLACE :
        (place == 'hand' ? HAND_PLACE : OUT_PLACE);
    for (var playerID in play) {
        var separation = place == 'table' ? BIG_SEP :
            (myIndex == playerID ? MEDIUM_SEP : SMALL_SEP);
        var cards = play[playerID];
        var arrow = new Arrow();
        arrow.rotate(baseAngle * (myIndex - playerID));
        arrow.translate(0, distance);
        arrow.scale(width / 2, height / 2);

        arrow.translate(-cards.length / 2 * separation, 0);
        for (var i = 0; i < cards.length; i++) {
            if (!(cards[i] in cardValues)) {
                cardValues[cards[i]] = {id: cards[i]};
            }
            cardValues[cards[i]].goal = jQuery.extend(true, {}, arrow);
            if (place == 'hand' && selectedCards[cards[i]]) {
                // move selected cards slightly upwards
                cardValues[cards[i]].goal.base.y -= SELECT_DIFF;
            }
            arrow.translate(separation, 0);
        }
    }
}

function drawGame(status, game) {
    // update game control button
    if (!game.state || game.state == 'AWAITING_RESTART') {
        attachControl('new round', function() {
            sendConv('NEWROUND', []);
        });
    } else if (game.state == 'AWAITING_SHOW') {
        attachControl('show', function() {
            var cards = Object.keys(selectedCards);
            sendConv('SHOW', [cards.length] + cards);
        });
    } else if (game.state == 'AWAITING_KITTY') {
        attachControl('make kitty', function() {
            var cards = Object.keys(selectedCards);
            sendConv('MAKEKITTY', [cards.length] + cards);
        });
    } else if (game.state == 'AWAITING_PLAY') {
        attachControl('play', function() {
            var cards = Object.keys(selectedCards);
            sendConv('PLAY', [cards.length] + cards);
        });
    }

    // draw game information
    var trump = game.trumpValue ?
        game.trumpValue + ', ' + game.trumpSuit : 'undeclared';
    var playerScores = [];
    for (var playerID in game.gameScores) {
        playerScores.push(
                status.members[playerID] + ': ' + game.gameScores[playerID]);
    }
    var roundScores = [];
    for (var team in game.roundScores) {
        roundScores.push(team + ': ' + game.roundScores[team]);
    }
    $('#gameinfo').html(
            'TRUMP: ' + trump + '<br/>' +
            'MASTER: ' + status.members[game.master] + '<br/>' +
            'GAME SCORES: ' + playerScores.join(', ') + '<br/>' +
            'ROUND SCORES: ' + roundScores.join(', ') + '<br/>'
            );
    cache = {status: status, game: game};
    updateCardPositions();
}

function updateCardPositions() {
    var status = cache.status, game = cache.game;
    var myIndex = $.inArray(username, status.members);
    var baseAngle = 2 * Math.PI / status.members.length;
    if (game.state == 'AWAITING_RESTART' && game.kitty) {
        setCardsGoal(myIndex, 'table', game.kitty, baseAngle);
    }
    if ((game.state == 'AWAITING_SHOW' || game.state == 'AWAITING_KITTY') &&
            game.shown) {
                setCardsGoal(myIndex, 'table', game.shown, baseAngle);
            }
    if (game.state != 'AWAITING_RESTART' && game.hands) {
        setCardsGoal(myIndex, 'hand', game.hands, baseAngle);
    }
    if (game.state == 'AWAITING_PLAY') {
        if (showPrev && game.currTrick) {
            setCardsGoal(myIndex, 'table', game.currTrick, baseAngle);
        }
    }
    drawCanvas();
}

function cardCompareFunction(card1, card2) {
    return (2 * card1.goal.base.x + card1.goal.base.y) -
        (2 * card2.goal.base.x + card2.goal.base.y);
}

function drawCanvas() {
    // prepare for drawing the canvas
    ctx.clearRect(-width / 2, -height / 2, width, height);
    var status = cache.status, game = cache.game;
    var myIndex = $.inArray(username, status.members);
    var baseAngle = 2 * Math.PI / status.members.length;

    // draw player names
    ctx.save();
    ctx.font = '18px Arial';
    ctx.fillStyle = 'white';
    for (var i = 0; i < status.members.length; i++) {
        var arrow = new Arrow();
        arrow.rotate(baseAngle * (myIndex - i));
        arrow.translate(0, 0.9);
        arrow.scale(width / 2, height / 2);

        ctx.save();
        arrow.setContext(ctx);
        var strWidth = ctx.measureText(status.members[i]).width;
        ctx.fillText(status.members[i], -strWidth / 2, 0);
        ctx.restore();
    }
    ctx.restore();

    // draw deck if cards remain
    if (game.deck) {
        ctx.drawImage(cardImages.BACK, -imgWidth / 2, -imgHeight / 2);
    }

    // draw all the cards
    cache.cards = new Array();
    for (var cardID in cardValues) {
        cache.cards.push(cardValues[cardID]);
    }
    cache.cards.sort(cardCompareFunction);
    for (var i = 0; i < cache.cards.length; i++) {
        var card = cache.cards[i];
        ctx.save();
        card.goal.setContext(ctx);
        var image = (card.suit && card.value ?
                cardImages[card.suit][card.value] : cardImages.BACK);
        ctx.drawImage(image, -imgWidth / 2, -imgHeight / 2);
        ctx.restore();
    }
}
