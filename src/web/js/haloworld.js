$(document).ready(setup);

var eb = null;
var showLogs = true;
var memberName;
var CHAT_ADDRESS = 'chat.msg';
var PING_ADDRESS = "chat.manager.ping";
var UPDATES_ADDRESS = "chat.manager.updates";

function setup() {
    // disable autocomplete on the message box
    $('#message-box').attr('autocomplete', 'OFF');

    // when the send button is clicked send the message to chat participants via the server
    $('#send-button').click(sendMessage);

    $('#clear-chat-button').click(clearChat);

    $('#show-log-messages-btn').click(toggleLogButtons);
    $('#hide-log-messages-btn').click(toggleLogButtons);

    $('#show-log-messages-btn').hide();

    $('#join-btn').click(handleLogin);

    $("#login-name").focus();

    $('#loginModal').on('shown', function () {
        this.focus();
    })

    $('#loginModal').modal({
        keyboard: false,
        backdrop: 'static'
    });

    $("#login-name").keypress(function(e){
        if (e.which == 13) {
            handleLogin();
            return false;
        }
        return true;
    });

    $("#message-box").keypress(function(e){
        if (e.which == 13) {
            sendMessage();
            return false;
        }
        return true;
    });



}

function handleLogin() {
    memberName = $('#login-name').val();
    if(memberName != null && memberName.length > 0) {
        $('#loginModal').modal('hide');
        connectToServer();
    }
    $('#message-box').focus();
    return false;
}

function sendMessage() {
    var message = $('#message-box').val();
    $('#message-box').val('');
    $('#message-box').focus();
    console.log("send button clicked: " + message);
    if(message) {
        console.log("Sending message: " + message);

        send(CHAT_ADDRESS, createChatMessage(message));
    }
    return false;
}

function createChatMessage(message) {
    return {
        messageType: 'CHAT',
        messageText: message,
        sender: memberName
    }
}

function clearChat() {
    $('#messages').empty();
    $('#message-box').focus();
    return false;
}

function toggleLogButtons() {
    $('#hide-log-messages-btn').toggle();
    $('#show-log-messages-btn').toggle();
    $('#feed .log').fadeToggle('fast', 'linear');
    showLogs = !showLogs;
    return false;
}

var LEVEL_TO_LABEL_MAP = {
    "DEBUG" : "inverse",
    "INFO" : "info",
    "WARNING" : "warning",
    "ERROR" : "important"
};

function addLogMessage(message) {
    var display = showLogs ? 'block' : 'none';

    var htmlContent = '<div class="result log" style="display:' + display + '"><span class="label label-' + LEVEL_TO_LABEL_MAP[message.level] + '">' +
        message.level + '</span> ' + message.messageText + '</div>';

    $('#feed').append(htmlContent);
}

function addChatMessage(message) {
    $('#messages').append("<div class='message'><b>" + message.sender + ": </b>" + message.messageText + "</div>");
}

function feedExists(resultId) {
    var container = $('#feed-' + resultId);
    return (container.length > 0);
}

function addFeedResult(message) {
    addFeedContainer(message.resultId);

    // create the result within the container
    FeedResultManager.createResult(message.resultType, message.resultId, message.data);

    $('#feed-' + message.resultId).effect("highlight", {}, 5000);
}

function addFeedContainer(resultId) {
    // create the feed container that the widget will render the result into
    var htmlContent = "<div id='feed-" + resultId + "' class='result feed'></div>"
    $('#feed').prepend(htmlContent);
}

function updateFeedResult(feedResultId, updateData) {
    FeedResultManager.updateResult(feedResultId, updateData);
}

function handleMessage(msg) {

    switch(msg.messageType) {
        case "LOG" :
            addLogMessage(msg);
            break;
        case "CHAT" :
            addChatMessage(msg);
            break;
        case "FEED_RESULT" :
            addFeedResult(msg);
            break;
        case "FEED_RESULT_UPDATE" :
            feedExists(msg.resultId) ? updateFeedResult(msg.resultId, msg.data) : addFeedResult(msg);
            break;
    }
}

function connectToServer() {

    if (!eb) {
        eb = new vertx.EventBus("http://localhost:8081/chat");

        eb.onopen = function() {
            console.log("Connected to chat");

            // subscribe to chat address
            setupSubscriptions();

            // setup a ping message to let the chat manager know we are still in the chat
            setupPing();
        };

        eb.onclose = function() {
            console.log("Disconnected from chat");
            eb = null;
        };

    }
}

function setupPing() {
    setInterval(function() {
        if(eb) {
            eb.send(PING_ADDRESS, {
                name: memberName
            })
        }
    }, 1000);
}

function setupSubscriptions() {
    if (eb) {

        // chat messages
        eb.registerHandler(CHAT_ADDRESS, function(msg, replyTo) {
            handleMessage(msg)
        });

        // member updates
        eb.registerHandler(UPDATES_ADDRESS, function(msg, replyTo) {
           handleMemberListUpdate(msg);
        });
    }
}

function handleMemberListUpdate(msg) {
    $('#chatMembers').text("Chatting with: " + msg.names.join(', '));
}

function send(address, message) {
    if (eb) {
        eb.send(address, message);
    }
}


var FeedResultManager = {

    resultWidgetConstructors: {},

    resultWidgets: {},

    registerWidgetConstructor: function(resultType, widgetConstructorFunction) {
        FeedResultManager.resultWidgetConstructors[resultType] = widgetConstructorFunction;
    },

    createResult: function(resultType, resultId, resultData) {
        var construct = FeedResultManager.resultWidgetConstructors[resultType];
        if(construct != null) {

            // create the widget that will render the result and tell it to render the initial result
            // store a reference to it so that we can feed it updates if/when they arrive.
            var resultWidget = construct(resultId);
            FeedResultManager.resultWidgets[resultId] = resultWidget;
            resultWidget.createWithData(resultData);
        } else {
            console.error("Could not create result of type '" + resultType + "' because no constructor function was found");
        }
    },

    updateResult: function(resultId, resultData) {
        var resultWidget = FeedResultManager.resultWidgets[resultId];
        if(resultWidget != null) {
            resultWidget.updateWithData(resultData);
        } else {
            console.error("Could not update widget with result ID '" + resultId + "' because it does not exist");
        }
    }
};
