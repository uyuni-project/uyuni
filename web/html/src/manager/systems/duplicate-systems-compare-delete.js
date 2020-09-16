/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {Utils} = require("../../utils/functions");
const {DeleteSystem} = require("./delete-system");
const SpaRenderer  = require("core/spa/spa-renderer").default;

function postForm(serverId) {
  const form = jQuery("table[class='list compare-list']").closest("form");
  jQuery('<input>').attr({
      type: "hidden",
      id: "removedServerId",
      name: "removedServerId",
      value: serverId
  }).appendTo(form);
  form.submit();
}

export const renderer = () => {
  getServerIdsToDelete().forEach(serverId => {
    SpaRenderer.renderNavigationReact(
      <DeleteSystem serverId={serverId} onDeleteSuccess={() => postForm(serverId)}
                    buttonText={t("Confirm Deletion")} buttonClass="btn-danger"
      />,
      document.getElementById("div_confirm" + serverId)
    );
  });
}
