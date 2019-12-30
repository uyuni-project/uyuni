/* eslint-disable */
"use strict";

const React = require("react");
const createReactClass = require('create-react-class');
const {Table, Column, SearchField, Highlight} = require("components/table");
const {StatePersistedMixin} = require("components/utils/StatePersistedMixin");
const CsvLink = require("./subscription-matching-util").CsvLink;
const Functions = require("utils/functions");
const Utils = Functions.Utils;

const Messages = createReactClass({
  displayName: 'Messages',
  mixins: [StatePersistedMixin],

  buildRows: function(rawMessages, systems, subscriptions) {
    return rawMessages.map(function(rawMessage, index) {
      const data = rawMessage["data"];
      var message;
      var additionalInformation;
      switch(rawMessage["type"]) {
        case "unknown_part_number" :
          message = t("Unsupported part number detected");
          additionalInformation = data["part_number"];
          break;
        case "physical_guest" :
          message = t("Physical system is reported as virtual guest, please check hardware data");
          additionalInformation = systems[data["id"]].name;
          break;
        case "guest_with_unknown_host" :
          message = t("Virtual guest has unknown host, assuming it is a physical system");
          additionalInformation = systems[data["id"]].name;
          break;
        case "unknown_cpu_count" :
          message = t("System has an unknown number of sockets, assuming 16");
          additionalInformation = systems[data["id"]].name;
          break;
        case "hb_merge_subscriptions" :
          message = t("Two subscriptions with the same part number are in a bundle - merged into a single one");
          additionalInformation = subscriptions[data["new_subscription_id"]].partNumber;
          break;
        case "merge_subscriptions" :
          message = t("Two subscriptions with the same part number (and other properties) have been merged together - start/end dates might be indicative");
          additionalInformation = data["part_number"];
          break;
        case "adjust_pinned_match" :
          message = t("Pinned match adjusted due to merged subscriptions");
          additionalInformation = t("Subscription IDs") + ": " +
              data["new_subscription_id"] + ", " + data["old_subscription_id"] + ", " +
              t("System") + ": " + systems[data["system_id"]].name;
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
            data={this.buildRows(this.props.messages, this.props.systems, this.props.subscriptions)}
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
  },
});

module.exports = {
  Messages: Messages,
}
