import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { PackageListActionScheduler } from "components/package/PackageListActionScheduler";
import { Column } from "components/table/Column";

import { Utils } from "utils/functions";

// See java/code/src/com/suse/manager/webui/templates/minion/ptf-list-remove.jade
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

function renderPackageInstallDate(item) {
  return item.summary;
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
        <Column
          key="extra-column-installed"
          columnKey="installed"
          comparator={Utils.sortByDate}
          header={t("Installed")}
          cell={renderPackageInstallDate}
        />,
      ]}
      confirmTitle={t("Confirm Product Temporary Fixes Removal")}
    />,
    document.getElementById(id)
  );
