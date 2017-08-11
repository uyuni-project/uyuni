"use strict";

const React = require("react");
const Network = require("../utils/network");

const AddSelectedToSSMLink = React.createClass({
  propTypes: {
    selectedItems: React.PropTypes.arrayOf(React.PropTypes.number).isRequired, // ids to add to SSM
  },

  onClick: function() {
    Network.post(
      "/rhn/manager/api/ssm/add",
      JSON.stringify(this.props.selectedItems),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        $("#spacewalk-set-system_list-counter").text(data.data);
      }
    });
  },

  render: function() {
    return (
      <a href="#" onClick={this.onClick}>{t("Add Selected to SSM")}</a>
    );
  }
});

module.exports = {
    AddSelectedToSSMLink : AddSelectedToSSMLink
}
