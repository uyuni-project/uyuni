"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var UtilComponent = require("./subscription-matching-util");
var StrongText = UtilComponent.StrongText;
var ToolTip = UtilComponent.ToolTip;
var CsvLink = UtilComponent.CsvLink;
var humanReadablePolicy = UtilComponent.humanReadablePolicy;

var Subscriptions = React.createClass({
  mixins: [StatePersistedMixin],

  rowComparator: function(a, b, columnIndex, ascending) {
    var columnKeyInRawData=["partNumber", "description", "policy", "quantity", "startDate", "endDate"];
    var columnKey = columnKeyInRawData[columnIndex];
    var orderCondition = ascending ? 1 : -1;
    var aRaw = a.props["rawData"];
    var bRaw = b.props["rawData"];
    var result = 0;
    if (columnKey == "policy") {
      var aValue = humanReadablePolicy(aRaw[columnKey]);
      var bValue = humanReadablePolicy(bRaw[columnKey]);
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }
    else if (columnKey == "quantity") {
      var aMatched = aRaw["matchedQuantity"];
      var aTotal = aRaw["totalQuantity"];
      var bMatched = bRaw["matchedQuantity"];
      var bTotal = bRaw["totalQuantity"];
      var aValue =  aMatched / aTotal;
      var bValue =  bMatched / bTotal;
      result = aValue > bValue ? 1 : (aValue < bValue ? -1 : 0);
    }
    else {
      var aValue = aRaw[columnKey];
      var bValue = bRaw[columnKey];
      result = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
    }

    if (result == 0) {
      var aId = aRaw["id"];
      var bId = bRaw["id"];
      result = aId > bId ? 1 : (aId < bId ? -1 : 0);
    }
    return result * orderCondition;
  },

  render: function() {
    var body;
    if (Object.keys(this.props.subscriptions).length > 0) {
      body = (
        <div>
          <Table headers={[t("Part number"), t("Description"), t("Policy"), t("Matched/Total"), t("Start date"), t("End date")]}
            rows={subscriptionsToRows(this.props.subscriptions)}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            rowFilter={(tableRow, searchValue) => tableRow.props["rawData"]["description"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
            filterPlaceholder={t("Filter by description")}
            rowComparator={this.rowComparator}
            sortableColumnIndexes={[0,1,2,3,4,5]}
          />
          <CsvLink name="subscription_report.csv" />
        </div>
      );
    }
    else {
      body = <p>{t("No subscriptions found.")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Your subscriptions")}</h2>
        {body}
      </div>
    );
  }
});

function subscriptionsToRows(subscriptions) {
  return Object.keys(subscriptions).map((k) => {
    var s = subscriptions[k];
    var now = moment();
    var className = moment(s.endDate).isBefore(now) ?
      "text-muted" :
        moment(s.endDate).isBefore(now.add(3, "months")) ?
        "text-danger" :
        null;

    var columns = [
      <TableCell content={s.partNumber} />,
      <TableCell content={s.description} />,
      <TableCell content={humanReadablePolicy(s.policy)} />,
      <QuantityCell matched={s.matchedQuantity} total={s.totalQuantity} />,
      <TableCell content={
        <ToolTip content={moment(s.startDate).fromNow()}
          title={moment(s.startDate).format("LL")} />}
      />,
      <TableCell content={
        <ToolTip content={moment(s.endDate).fromNow()}
          title={moment(s.endDate).format("LL")} />}
      />,
    ];

    return <TableRow className={className} columns={columns} rawData={s} />
  });
}

var QuantityCell = (props) => {
  var matched = props.matched;
  var total = props.total;
  var content = matched + "/" + total;

  if (matched == total) {
    return <TableCell content={<StrongText className="bg-danger" content={content} />} />;
  }
  return <TableCell content={content} />;
}

module.exports = {
  Subscriptions: Subscriptions,
}
