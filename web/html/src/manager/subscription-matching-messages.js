/* eslint-disable */
"use strict";

const React = require("react");
const {Table, Column, SearchField, Highlight} = require("../components/table");
const StatePersistedMixin = require("../components/util").StatePersistedMixin;
const CsvLink = require("./subscription-matching-util").CsvLink;
const Functions = require("../utils/functions");
const Utils = Functions.Utils;

const Messages = React.createClass({
  mixins: [StatePersistedMixin],

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
          // we do not know the shape of the data, it could even be a complex nested object (bsc#1125600)
          additionalInformation = JSON.stringify(data);
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
            data={this.buildRows(this.props.messages, this.props.systems)}
            identifier={(row) => row.id}
            loadState={this.props.loadState}
            saveState={this.props.saveState}
            initialSortColumnKey="message"
            initialItemsPerPage={userPrefPageSize}
          >
            <Column
                columnKey="message"
                comparator={Utils.sortByText}
                header={t("Message")}
                cell={ (row) => row.message }
            />
            <Column
                columnKey="info"
                comparator={Utils.sortByText}
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
