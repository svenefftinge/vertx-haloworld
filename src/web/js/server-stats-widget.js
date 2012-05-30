// Widget that shows a simple text result and updates it when new text comes in

function ServerStatsWidget(resultId) {
    this.resultId = resultId;
    this.currentValues = []; // current heap usage percentage
    this.graphContainer = null;
}

ServerStatsWidget.constuctWidget = function(resultId) {
    console.log("Creating server stats result widget with ID: " + resultId);
    return new ServerStatsWidget(resultId);
};

ServerStatsWidget.prototype.createWithData = function(data) {
    console.log("Creating result with data: " + data);

    // create DIV for text status
    var htmlContent = "<div id='feed-" + this.resultId + "-text'></div>";
    $('#feed-' + this.resultId).html(htmlContent);

    // append SVG container for graph
    this.createGraphContainer(this.resultId);

    // set text info
    var currentInfo = this.createTextInfo(data);
    $('#feed-' + this.resultId + "-text").text(currentInfo);

    // set initial graph value
    this.currentValues.push(data.heapPercentageUsage);
    this.updateGraph();
};

ServerStatsWidget.prototype.updateWithData = function(data) {
    console.log("Updating result with data: " + data);
    var currentInfo = this.createTextInfo(data);
    $('#feed-' + this.resultId + "-text").text(currentInfo);

    this.currentValues.push(data.heapPercentageUsage);
    if(this.currentValues.length > ServerStatsWidget.MAX_COLUMNS) {
        this.currentValues.shift(); // remove left most column to make room for new value
    }
    this.updateGraph();
};

ServerStatsWidget.prototype.createTextInfo = function(data) {
    var info = "UPTIME: " + data.uptime + " HEAP USAGE: " + data.heapUsage + " ("+ data.heapPercentageUsageString + "%)";
    return info;
};

ServerStatsWidget.prototype.createGraphContainer = function(resultId) {
    var sel = "#feed-" + resultId;
    var feedDiv = d3.select(sel);
    this.graphContainer = feedDiv.append("svg")
        .attr("id", resultId + "-graph")
        .attr("width", ServerStatsWidget.GRAPH_WIDTH)
        .attr("height", ServerStatsWidget.GRAPH_HEIGHT);
};

ServerStatsWidget.prototype.updateGraph = function() {
    var width = ServerStatsWidget.COL_WIDTH + ServerStatsWidget.COL_SPACING;
    var xFn = function(data, index) { return index * width };
    var yFn = function(data) { return ServerStatsWidget.GRAPH_HEIGHT - ((data / 100) * ServerStatsWidget.GRAPH_HEIGHT) };
    var heightFn = function(data) { return (data / 100) * ServerStatsWidget.GRAPH_HEIGHT };
    var svg = this.graphContainer;

    // add any new nodes (if less than max columns shown)
    svg.selectAll("rect")
        .data(this.currentValues)
        .enter()
        .append("rect")
        .attr("x", xFn)
        .attr("y", yFn)
        .attr("width", ServerStatsWidget.COL_WIDTH)
        .attr("height", heightFn);

    // update any existing nodes
    svg.selectAll("rect")
        .data(this.currentValues)
        .attr("x", xFn)
        .attr("y", yFn)
        .attr("width", ServerStatsWidget.COL_WIDTH)
        .attr("height", heightFn);
};

ServerStatsWidget.MAX_COLUMNS = 20;
ServerStatsWidget.GRAPH_WIDTH = 640; // will fit 25 columns
ServerStatsWidget.GRAPH_HEIGHT = 150;
ServerStatsWidget.COL_WIDTH = 30;
ServerStatsWidget.COL_SPACING = 4;


FeedResultManager.registerWidgetConstructor("SERVER_STATS_RESULT", ServerStatsWidget.constuctWidget);

