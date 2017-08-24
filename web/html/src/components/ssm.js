"use strict";

const React = require("react");
const Network = require("../utils/network");

/** Takes a list of system IDs and adds them to the SSM. */
const addToSSM = function(ids) {
  Network.post(
    "/rhn/manager/api/ssm/add",
    JSON.stringify(ids),
    "application/json"
  ).promise.then(data => {
    if (data.success) {
      $("#spacewalk-set-system_list-counter").text(data.data);
    }
  });
}

/** Takes a list of system IDs and removes them from the SSM. */
const removeFromSSM = function(ids) {
  Network.post(
    "/rhn/manager/api/ssm/remove",
    JSON.stringify(ids),
    "application/json"
  ).promise.then(data => {
    if (data.success) {
      $("#spacewalk-set-system_list-counter").text(data.data);
    }
  });
}

const AddSelectedToSSMLink = React.createClass({
  propTypes: {
    selectedItems: React.PropTypes.arrayOf(React.PropTypes.number).isRequired, // ids to add to SSM
  },

  render: function() {
    return (
      <a href="#" onClick={addToSSM(this.props.selectedItems)}>{t("Add Selected to SSM")}</a>
    );
  }
});

module.exports = {
    AddSelectedToSSMLink : AddSelectedToSSMLink,
    addToSSM : addToSSM,
    removeFromSSM : removeFromSSM
}
