import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { PackageListActionScheduler } from "components/package/PackageListActionScheduler";

import { PTF_COLUMN_ARCH, PTF_COLUMN_INSTALL_DATE, PTF_COLUMN_SUMMARY } from "./ptf-column-definition";

// See java/code/src/com/suse/manager/webui/templates/minion/ptf-list-remove.jade
declare global {
  interface Window {
    serverId?: any;
    actionChains?: any;
  }
}

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(
    <PackageListActionScheduler
      serverId={window.serverId}
      selectionSet={`ptf_list_remove${window.serverId}`}
      actionChains={window.actionChains}
      icon="fa-fire-extinguisher"
      listDataAPI={`/rhn/manager/api/systems/${window.serverId}/details/ptf/installed`}
      scheduleActionAPI={`/rhn/manager/api/systems/${window.serverId}/details/ptf/scheduleAction`}
      actionType="packages.remove"
      listTitle={t("Remove Product Temporary Fixes")}
      listSummary={t(
        "The following Product Temporary Fixes (PTF) are currently installed on this system. " +
          'These PTFs may be scheduled for removal by selecting them and clicking "Remove PTF" below. ' +
          "Please note that removing any non-obsolete PTF will result in the installation of the most recent " +
          "versions of the packages that are part of the PTF."
      )}
      listEmptyText={t("No Product Temporary Fixes installed.")}
      listActionLabel={t("Remove PTF")}
      listColumns={[PTF_COLUMN_SUMMARY, PTF_COLUMN_ARCH, PTF_COLUMN_INSTALL_DATE]}
      confirmTitle={t("Confirm Product Temporary Fixes Removal")}
    />,
    document.getElementById(id)
  );
