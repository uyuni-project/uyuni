/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {Utils} = require("../../utils/functions");
const {DeleteSystem} = require("./delete-system");

function postForm(serverId) {
  const form = $("table[class='list compare-list']").closest("form");
  $('<input>').attr({
      type: "hidden",
      id: "removedServerId",
      name: "removedServerId",
      value: serverId
  }).appendTo(form);
  form.submit();
}

getServerIdsToDelete().forEach(serverId => {
  ReactDOM.render(
    <DeleteSystem serverId={serverId} onDeleteSuccess={() => postForm(serverId)}
      buttonText={t("Confirm Deletion")} buttonClass="btn-danger"
      />,
      document.getElementById("div_confirm" + serverId)
  );
});
