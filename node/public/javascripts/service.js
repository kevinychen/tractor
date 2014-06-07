
var gameSocket;
var ctx;
var width, height;
var imgWidth = 0, imgHeight = 0;

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
    sendMsg(gameSocket, [
            'STATUS',
            $('#gamestatusnumdecks').val(),
            $('#gamestatusfindafriend').prop('checked')
            ].concat(playerOrdering)
            );
}

function declareBeginGame(roomname) {
    sendMsg(gameSocket, ['BEGINGAME']);
}

function attachControl(label, func) {
    $('#gamecontrol').show();
    $('#gamecontrol').off('click');
    $('#gamecontrol').text(label);
    $('#gamecontrol').on('click', func);
}

var gameSocket;
var players, playerOrdering;
var cardPlaces;
var selectedCards;
var cardImages = {};
var showPrev = false;
var drawingCanvas = false;
var cache;

function setMainService(service, roomname, auth) {
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
    html += '<button id="gamestatusmoveup" type="button">&#x25B2</button>';
    html += '<button id="gamestatusmovedown" type="button">&#x25BC</button>';
    $('#gameintro').html(html);
    $('#gamestatusnumdecks').bind('input', function() {
        declareStatus(roomname);
    });
    $('#gamestatusfindafriend').click('change', function() {
        declareStatus(roomname);
    });
    $('#gamestatusmoveup').on('click', function() {
        var player = $('input:radio[name=player]:checked').val();
        if (player) {
            var index = players.indexOf(player);
            var where = playerOrdering.indexOf(index);
            if (where != -1 && where != 0) {
                playerOrdering[where] = playerOrdering[where - 1];
                playerOrdering[where - 1] = index;
            }
            declareStatus(roomname);
        }
    });
    $('#gamestatusmovedown').on('click', function() {
        var player = $('input:radio[name=player]:checked').val();
        if (player) {
            var index = players.indexOf(player);
            var where = playerOrdering.indexOf(index);
            if (where != -1 && where != players.length - 1) {
                playerOrdering[where] = playerOrdering[where + 1];
                playerOrdering[where + 1] = index;
            }
            declareStatus(roomname);
        }
    });
    attachControl('begin game', function() {
        declareBeginGame(roomname);
    });

    cache = {};
    cardPlaces = {};
    selectedCards = {};

    gameSocket.onopen = function() {
        sendMsg(gameSocket, ['HELLO', roomname, username, auth]);
    };
    gameSocket.onmessage = function(msg) {
        var data = JSON.parse(msg.data);
        if (data.error) {
            showMsg($('#roomerror'), data.error);
        }
        if (data.notification) {
            showMsg($('#roomnotification'), data.notification);
        }
        if (data.gameStarted) {
            $('#gameintro').hide();
            if ($('#gamecanvas').is(':hidden')) {
                $('#gamecanvas').slideDown();
            }
            cache = data;
            if (cache.game.players) {
                cache.myIndex = $.inArray(username, cache.game.players);
                cache.baseAngle = 2 * Math.PI / cache.game.players.length;
            }
            drawGame();
        } else if (data.status) {
            $('#gameintro').show();
            $('#gamecanvas').hide();
            $('#gamestatusnumdecks').val(data.status.properties.numDecks);
            $('#gamestatusfindafriend').prop('checked', data.status.properties.find_a_friend);
            $('#gamestatusstatus').text(data.status.status);
            players = data.status.members;
            playerOrdering = data.status.playerOrdering;
            var playerList = '';
            for (var i = 0; i < players.length; i++) {
                playerList += '<li>' + (i + 1) + ': ';
                playerList += players[playerOrdering[i]];
                playerList += '<input type="radio" name="player" value="' +
                   players[playerOrdering[i]] + '" />';
            }
            var prevChosen = $('input:radio[name=player]:checked').val();
            $('#gamestatusplayers').html(playerList);
            $('input:radio[name=player][value=' + prevChosen + ']').attr('checked', true);
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
        cardImages[suitName] = {};
        for (var valueName in values) {
            cardImages[suitName][valueName] = new Image();
            cardImages[suitName][valueName].src =
                '/images/cards/' + suits[suitName] + values[valueName] + '.gif';
        }
    }
    cardImages.TRUMP = {};
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

const TABLE_PLACE = 0.36;
const HAND_PLACE = 0.71;
const NAME_PLACE = 0.95;
const OUT_PLACE = 1.6;
const BIG_SEP = 20;
const MEDIUM_SEP = 15;
const SMALL_SEP = 10;
const SELECT_DIFF = 20;
const CARD_JUMP = 0.5;
const END_TRICK_WAIT = 1000;

function addCardSelectionListener(canvas) {
    canvas.onmousedown = function() { return false; };
    canvas.addEventListener('mousedown', function(e) {
        var rect = canvas.getBoundingClientRect();
        var x = e.clientX - rect.left - width / 2;
        var y = e.clientY - rect.top - height / 2;
        if (onShowPrev(x, y)) {
            showPrev = true;
            updateCardPositions();
            return;
        }
        if (!cache.game || !cache.game.hands) {
            return;
        }
        var hand = cache.game.hands[cache.myIndex];
        if (!hand) {
            return;
        }
        var topCard = null;
        for (var i = 0; i < hand.length; i++) {
            if (hand[i] in cardPlaces) {
                var card = cardPlaces[hand[i]];
                if (inCard(x, y, card) &&
                    (!topCard || cardCompareFunction(card, topCard) > 0)) {
                        topCard = card;
                    }
            }
        }
        if (topCard) {
            if (topCard.id in selectedCards) {
                delete selectedCards[topCard.id];
            } else {
                selectedCards[topCard.id] = true;
            }
            updateCardPositions();
        }
    });
    canvas.addEventListener('mouseup', function(e) {
        if (showPrev) {
            showPrev = false;
            updateCardPositions();
        }
    });
}

function setCardsGoal(place, play, setWinner) {
    var distance = place == 'table' ? TABLE_PLACE :
        (place == 'hand' ? HAND_PLACE :
         (place == 'out' ? OUT_PLACE : 0));
    for (var playerID in play) {
        if (playerID == 'winner') {
            continue;  // TODO play might have a 'winner' field
        }
        var separation = place == 'table' ? BIG_SEP :
            (cache.myIndex != playerID && cache.myIndex != -1 ? SMALL_SEP :
             (place == 'deck' ? 0 : MEDIUM_SEP));
        var cards = play[playerID];
        var arrow = new Arrow();
        arrow.rotate(cache.baseAngle *
                (cache.myIndex - (setWinner ? play.winner : playerID)));
        arrow.translate(0, distance);
        arrow.scale(width / 2, height / 2);

        arrow.translate(-cards.length / 2 * separation, 0);
        for (var i = 0; i < cards.length; i++) {
            if (!(cards[i] in cardPlaces)) {
                cardPlaces[cards[i]] = {id: cards[i]};
            }
            cardPlaces[cards[i]].goal =
                jQuery.extend(true, {}, arrow);
            if (place == 'hand' && selectedCards[cards[i]]) {
                // move selected cards slightly upwards
                cardPlaces[cards[i]].goal.base.y -= SELECT_DIFF;
            }
            arrow.translate(separation, 0);
        }
    }
}

function sendSelectedCardsMsg(command) {
    var cards = Object.keys(selectedCards);
    selectedCards = {};
    updateCardPositions();
    sendMsg(gameSocket, [command, cards.length].concat(cards));
}

function drawGame() {
    var status = cache.status, game = cache.game;

    // update game control button
    if (!game.state || game.state == 'AWAITING_RESTART') {
        if (cache.myIndex != -1) {
            attachControl('new round', function() {
                sendMsg(gameSocket, ['NEWROUND']);
            });
        } else {
            $('#gamecontrol').hide();
        }
    } else if (game.state == 'AWAITING_SHOW') {
        if (cache.myIndex != -1) {
            attachControl('show', function() {
                sendSelectedCardsMsg('SHOW');
            });
        } else {
            $('#gamecontrol').hide();
        }
    } else if (game.state == 'AWAITING_KITTY') {
        if (cache.myIndex == game.master) {
            attachControl('make kitty', function() {
                sendSelectedCardsMsg('MAKEKITTY');
            });
        } else {
            $('#gamecontrol').hide();
        }
    } else if (game.state == 'AWAITING_PLAY') {
        if (cache.myIndex == game.currPlayer) {
            attachControl('play', function() {
                sendSelectedCardsMsg('PLAY');
            });
        } else {
            $('#gamecontrol').hide();
        }
    } else {
        $('#gamecontrol').hide();
    }

    // draw game information
    var trump = game.trumpVal ?
        game.trumpVal + ', ' + game.trumpSuit : 'undeclared';
    var playerScores = [];
    for (var playerID in game.gameScores) {
        playerScores.push(
                game.players[playerID] + ': ' + game.gameScores[playerID]);
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

    // draw cards, etc.
    if (game.endTrick) {
        showPrev = true;
        setTimeout(function() {
            showPrev = false;
            updateCardPositions();
        }, END_TRICK_WAIT);
    }
    updateCardPositions();
}

function updateCardPositions() {
    var status = cache.status, game = cache.game;
    if (game.deck) {
        var deck = {};
        deck[cache.myIndex] = game.deck;
        setCardsGoal('deck', deck);
    }
    if (game.kitty) {
        if (game.state == 'AWAITING_RESTART' && !showPrev) {
            setCardsGoal('table', game.kitty);
        } else {
            setCardsGoal('out', game.kitty);
        }
    }
    if (game.shown) {
        if (game.state == 'AWAITING_SHOW' || game.state == 'AWAITING_KITTY') {
            setCardsGoal('table', game.shown);
        } else {
            setCardsGoal('out', game.shown);
        }
    }
    if (game.hands) {
        setCardsGoal('hand', game.hands);
    }
    if (game.currTrick) {
        if (game.state == 'AWAITING_PLAY' || game.state == 'AWAITING_RESTART') {
            if (showPrev) {
                setCardsGoal('table', game.prevTrick);
                setCardsGoal('out', game.currTrick);
            } else {
                setCardsGoal('table', game.currTrick);
                setCardsGoal('out', game.prevTrick, true);
            }
            setCardsGoal('out', game.goneTrick, true);
        }
    }
    if (!drawingCanvas) {
        drawCanvas();
    }
}

function inCard(x, y, card) {
    return card.pos &&
        Math.abs(x - card.pos.base.x) < imgWidth / 2 &&
        Math.abs(y - card.pos.base.y) < imgHeight / 2;
}

function closeToCard(x, y, card) {
    return card.pos &&
        Math.abs(x - card.pos.base.x) + Math.abs(y - card.pos.base.y) < 10;
}

function onShowPrev(x, y) {
    return Math.abs(x) + Math.abs(y) < 40;
}

function cardCompareFunction(card1, card2) {
    return (2 * card1.pos.base.x + card1.pos.base.y) -
        (2 * card2.pos.base.x + card2.pos.base.y);
}

function drawCanvas() {
    // if images have not loaded, wait
    if (!imgWidth) {
        setTimeout(drawCanvas, 1000);
        return;
    }

    // prepare for drawing the canvas
    drawingCanvas = false;
    ctx.clearRect(-width / 2, -height / 2, width, height);
    var status = cache.status, game = cache.game, cards = cache.cards;

    // draw player names
    ctx.save();
    ctx.font = '18px Arial';
    ctx.fillStyle = 'white';
    for (var i = 0; i < game.players.length; i++) {
        var arrow = new Arrow();
        arrow.rotate(cache.baseAngle * (cache.myIndex - i));
        arrow.translate(0, NAME_PLACE);
        arrow.scale(width / 2, height / 2);

        ctx.save();
        arrow.setContext(ctx);
        var strWidth = ctx.measureText(game.players[i]).width;
        ctx.fillText(game.players[i], -strWidth / 2, 0);
        ctx.restore();
    }
    ctx.restore();

    // draw show prev button
    if (game.state == 'AWAITING_PLAY' || game.state == 'AWAITING_RESTART') {
        ctx.save();
        ctx.font = '12px Arial';
        ctx.fillStyle = 'white';
        var showPrevText = 'SHOW PREV';
        var strWidth = ctx.measureText(showPrevText).width;
        ctx.fillText(showPrevText, -strWidth / 2, 0);
        ctx.restore();
    }

    // draw all the cards
    var cardList = new Array();
    var redraw = false;
    for (var cardID in cardPlaces) {
        var card = cardPlaces[cardID];
        if (card.pos) {
            // move pos closer to goal
            card.pos.base.x = CARD_JUMP * card.goal.base.x +
                (1 - CARD_JUMP) * card.pos.base.x;
            card.pos.base.y = CARD_JUMP * card.goal.base.y +
                (1 - CARD_JUMP) * card.pos.base.y;
            card.pos.dir = CARD_JUMP * card.goal.dir +
                (1 - CARD_JUMP) * card.pos.dir;

            // if not close enough, keep drawing
            if (closeToCard(card.goal.base.x, card.goal.base.y, card)) {
                card.pos = jQuery.extend({}, card.goal);
            } else {
                redraw = true;
            }
        } else {
            card.pos = jQuery.extend({}, card.goal);
        }
        cardList.push(card);
    }
    cardList.sort(cardCompareFunction);
    for (var i = 0; i < cardList.length; i++) {
        ctx.save();
        cardList[i].pos.setContext(ctx);
        var card = cache.cards[cardList[i].id];
        var image = (card ?
                cardImages[card.suit][card.value] : cardImages.BACK);
        ctx.drawImage(image, -imgWidth / 2, -imgHeight / 2);
        ctx.restore();
    }
    if (redraw) {
        // some cards are not at their final position; keep drawing
        drawingCanvas = true;
        setTimeout(drawCanvas, 50);
    }
}
