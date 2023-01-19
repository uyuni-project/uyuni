import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { PackageListActionScheduler } from "components/package/PackageListActionScheduler";
import { Column } from "components/table/Column";

import { Utils } from "utils/functions";

// See java/code/src/com/suse/manager/webui/templates/minion/ptf-install.jade
declare global {
  interface Window {
    serverId?: any;
    actionChains?: any;
  }
}

function renderPackageArch(item) {
  return item.arch;
}

function renderPackageSummary(item) {
  return item.summary;
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
      listTitle={t("Install Product Temporary Fixes")}
      listSummary={t(
        "The following Product Temporary Fixes (PTF) are available for installation on this system. " +
          "These packages are only meant to address a specific open issue. " +
          "Please follow the instructions from customer support before proceeding with the installation."
      )}
      listEmptyText={t("No Product Temporary Fixes available.")}
      listActionLabel={t("Install PTF")}
      listColumns={[
        <Column
          key="extra-column-summary"
          columnKey="summary"
          comparator={Utils.sortByText}
          header={t("Summary")}
          cell={renderPackageSummary}
        />,
        <Column
          key="extra-column-arch"
          columnKey="arch"
          comparator={Utils.sortByText}
          header={t("Architecture")}
          cell={renderPackageArch}
        />,
      ]}
      confirmTitle={t("Confirm Product Temporary Fixes Installation")}
    />,
    document.getElementById(id)
  );
