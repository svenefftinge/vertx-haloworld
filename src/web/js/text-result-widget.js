// Widget that shows a simple text result and updates it when new text comes in

function TextResultWidget(resultId) {
    this.resultId = resultId;
}

TextResultWidget.constuctWidget = function(resultId) {
    console.log("Creating text result widget with ID: " + resultId);
    return new TextResultWidget(resultId);
};

TextResultWidget.prototype.createWithData = function(data) {
    console.log("Creating result with data: " + data);
    $('#feed-' + this.resultId).text(data.text);
};

TextResultWidget.prototype.updateWithData = function(data) {
    console.log("Updating result with data: " + data)
    $('#feed-' + this.resultId).text(data.text);
};

FeedResultManager.registerWidgetConstructor("SIMPLE_TEXT_RESULT", TextResultWidget.constuctWidget);