"use strict";

const React = require("react");
//const TableComponent = require("../components/table");
//const Table = TableComponent.Table;
//const TableCell = TableComponent.TableCell;
//const TableRow = TableComponent.TableRow;
const {Table, Column, SearchField, Highlight, SimpleTableDataModel} = require("../components/tableng.js");
const StatePersistedMixin = require("../components/util").StatePersistedMixin;
const CsvLink = require("./subscription-matching-util").CsvLink;

const Messages = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {tableModel: new SimpleTableDataModel(this.buildRows(this.props.messages, this.props.systems))};
  },

  rowComparator: function(aValue, bValue) {
    return aValue.message.toLowerCase().localeCompare(bValue.message.toLowerCase());
  },

  componentWillReceiveProps(nextProps) {
    if (this.props.subscriptions != nextProps.subscriptions) {
        console.log("componentWillReceiveProps:");
        this.setState({tableModel: new SimpleTableDataModel(this.buildRows(nextProps.props.messages, nextProps.props.systems))});
    }
  },

  buildRows: function(rawMessages, systems) {
    return rawMessages.map(function(rawMessage, index) {
      const data = rawMessage["data"];
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
      return {
        id: index,
        message: message,
        info: additionalInformation
      };
    });
  },

  render: function() {
    var body;
    if (this.props.messages.length > 0) {
      body = (
        <div>
          <p>{t("Please review warning and information messages below.")}</p>
          <Table
            dataModel={this.state.tableModel}
            rowKeyFn={(row) => row.id}
            initialSort="message"
            >
            <Column
                columnKey="message"
                sortFn={this.rowComparator}
                header={t("Message")}
                cell={ (row) => row.message }
                />
            <Column
                columnKey="info"
                header={t("Additional information")}
                cell={ (row) => row.info }
                />
          </Table>

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

module.exports = {
  Messages: Messages,
}
