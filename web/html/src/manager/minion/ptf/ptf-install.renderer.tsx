import SpaRenderer from "core/spa/spa-renderer";

import { PackageListActionScheduler } from "components/package/PackageListActionScheduler";

import { PTF_COLUMN_ARCH, PTF_COLUMN_SUMMARY } from "./ptf-column-definition";

// See java/code/src/com/suse/manager/webui/templates/minion/ptf-install.jade
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
      selectionSet={`ptf_install${window.serverId}`}
      actionChains={window.actionChains}
      icon="fa-fire-extinguisher"
      listDataAPI={`/rhn/manager/api/systems/${window.serverId}/details/ptf/available`}
      scheduleActionAPI={`/rhn/manager/api/systems/${window.serverId}/details/ptf/scheduleAction`}
      actionType="packages.update"
      listTitle={t("Install Program Temporary Fixes (PTFs)")}
      listSummary={t(
        "The following Program Temporary Fixes (PTFs) are available for installation on this system. " +
          "These packages are only meant to address a specific open issue. " +
          "Please follow the instructions from customer support before proceeding with the installation."
      )}
      listEmptyText={t("No Program Temporary Fixes (PTFs) available.")}
      listActionLabel={t("Install PTFs")}
      listColumns={[PTF_COLUMN_SUMMARY, PTF_COLUMN_ARCH]}
      confirmTitle={t("Confirm Program Temporary Fixes (PTFs) Installation")}
    />,
    document.getElementById(id)
  );
