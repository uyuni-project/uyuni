import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Utils } from "utils/functions";

import { DeleteSystem } from "./delete-system";

// See java/code/webapp/WEB-INF/pages/systems/sdc/delete_confirm.jsp
declare global {
  interface Window {
    getServerIdToDelete: () => any;
  }
}

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(
    <DeleteSystem
      serverId={window.getServerIdToDelete()}
      onDeleteSuccess={() => Utils.urlBounce("/rhn/systems/Overview.do")}
      buttonText={t("Delete Profile")}
      buttonClass="btn-danger"
    />,
    document.getElementById(id)
  );
