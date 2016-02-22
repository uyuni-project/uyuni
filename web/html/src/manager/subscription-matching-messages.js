"use strict";

var React = require("react");
var TableComponent = require("../components/table");
var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var StatePersistedMixin = require("../components/util").StatePersistedMixin;
var CsvLink = require("./subscription-matching-util").CsvLink;

var Messages = React.createClass({
  mixins: [StatePersistedMixin],

  rowComparator: function(a, b, columnIndex, ascending) {
    const orderCondition = ascending ? 1 : -1;
    const aValue = a.props.message;
    const bValue = b.props.message;
    return aValue.toLowerCase().localeCompare(bValue.toLowerCase()) * orderCondition;
  },

  render: function() {
    var body;
    if (this.props.messages.length > 0) {
      body = (
        <div>
          <p>{t("Please review warning and information messages below.")}</p>
          <Table
            headers={[t("Message"), t("Additional information")]}
            rows={messagesToRows(this.props.messages, this.props.systems)}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            rowComparator={this.rowComparator}
            sortableColumnIndexes={[0]}
          />
          <CsvLink name="message_report.csv" />
        </div>
      );
    }
    else {
      body = <p>{t("No messages from the last match run.")}</p>
    }

    return (
      <div className="row col-md-12">
        <h2>{t("Messages")}</h2>
        {body}
      </div>
    );
  }
});

function messagesToRows(rawMessages, systems) {
  var result= rawMessages.map(function(rawMessage) {
    var data = rawMessage["data"];
    var message;
    var additionalInformation;
    switch(rawMessage["type"]) {
      case "unknownPartNumber" :
        message = t("Unsupported part number detected");
        additionalInformation = data["partNumber"];
        break;
      case "physicalGuest" :
        message = t("Physical system is reported as virtual guest, please check hardware data");
        additionalInformation = systems[data["id"]].name;
        break;
      case "guestWithUnknownHost" :
        message = t("Virtual guest has unknown host, assuming it is a physical system");
        additionalInformation = systems[data["id"]].name;
        break;
      case "unknownCpuCount" :
        message = t("System has an unknown number of sockets, assuming 16");
        additionalInformation = systems[data["id"]].name;
        break;
      default:
        message = rawMessage["type"];
        additionalInformation = data;
    }
    var columns = [
      <TableCell content={message} />,
      <TableCell content={additionalInformation} />
    ];
    return <TableRow columns={columns} message={message}/>;
  });
  return result;
}

module.exports = {
  Messages: Messages,
}
