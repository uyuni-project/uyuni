import * as React from "react";
import { DeleteSystem } from "./delete-system";
import SpaRenderer from "core/spa/spa-renderer";

// See java/code/webapp/WEB-INF/pages/systems/duplicate/duplicatesystemscompare.jsp
declare global {
  interface Window {
    getServerIdsToDelete: () => any[];
  }
}

function postForm(serverId) {
  const form = jQuery("table[class='list compare-list']").closest("form");
  jQuery("<input>")
    .attr({
      type: "hidden",
      id: "removedServerId",
      name: "removedServerId",
      value: serverId,
    })
    .appendTo(form);
  form.submit();
}

export const renderer = () => {
  window.getServerIdsToDelete().forEach((serverId) => {
    SpaRenderer.renderNavigationReact(
      <DeleteSystem
        serverId={serverId}
        onDeleteSuccess={() => postForm(serverId)}
        buttonText={t("Confirm Deletion")}
        buttonClass="btn-danger"
      />,
      document.getElementById("div_confirm" + serverId)
    );
  });
};
