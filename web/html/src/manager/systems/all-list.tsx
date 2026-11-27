/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import { LinkButton } from "components/buttons";
import * as Systems from "components/systems";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { TopPanel } from "components/panels/TopPanel";
import { Utils } from "utils/functions";
import Network from "utils/network";

import { SystemsListFilter } from "./list-filter";

type Props = {
  /** Locale of the help links */
  docsLocale: string;
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

const DownloadCSVButton = ({ search }) => {
  let url = "/rhn/manager/systems/csv/all";
  if (search?.field && search?.criteria) {
    const searchParams = new URLSearchParams({
      qc: search.field,
      q: search.criteria,
    });
    url += `?${searchParams.toString()}`;
  }
  return (
    <LinkButton
      text={t("Download CSV")}
      href={url}
      download="systems.csv"
      className="btn btn-default"
      icon="spacewalk-icon-download-csv"
      disableSenna
    />
  );
};

export function AllSystems(props: Props) {
  const [selectedSystems, setSelectedSystems] = useState<string[]>([]);

  const handleSelectedSystems = (items: string[]) => {
    const removed = selectedSystems.filter((item) => !items.includes(item)).map((item) => [item, false]);
    const added = items.filter((item) => !selectedSystems.includes(item)).map((item) => [item, true]);
    const data = Object.assign({}, Object.fromEntries(added), Object.fromEntries(removed));
    Network.post("/rhn/manager/api/sets/system_list", data).catch(Network.showResponseErrorToastr);

    setSelectedSystems(items);
  };

  return (
    <TopPanel
      title={t("Systems")}
      iconType="header-system"
      helpUrl={`/reference/systems/systems-list.html`}
      button={
        <LinkButton
          id="addsystem"
          icon="fa-plus"
          className="btn btn-primary"
          text={t("Add system")}
          href="/rhn/manager/systems/bootstrap"
        />
      }
    >
      <Table
        data="/rhn/manager/api/systems/list/all"
        identifier={(item) => item.id}
        initialSortColumnKey="server_name"
        selectable={(item) => "id" in item}
        selectedItems={selectedSystems}
        onSelect={handleSelectedSystems}
        searchField={<SystemsListFilter />}
        defaultSearchField={props.queryColumn || "server_name"}
        initialSearch={props.query}
        emptyText={t("No Systems.")}
        titleButtons={[<DownloadCSVButton key="download-csv-button" search={{}} />]}
      >
        <Column
          columnKey="server_name"
          comparator={Utils.sortByText}
          header={t("System")}
          cell={(item) => Systems.iconAndName(item)}
        />
        <Column
          columnKey="status_type"
          comparator={Utils.sortByText}
          header={t("Updates")}
          cell={(item) => {
            if (!item.statusType) {
              return "";
            }
            return Systems.statusDisplay(item, props.isAdmin);
          }}
        />
        <Column
          columnKey="totalErrataCount"
          comparator={Utils.sortByText}
          header={t("Patches")}
          cell={(item) => {
            const totalErrataCount = item.securityErrata + item.bugErrata + item.enhancementErrata;
            if (totalErrataCount !== 0) {
              return <a href={`/rhn/systems/details/ErrataList.do?sid=${item.id}`}>{totalErrataCount}</a>;
            }
            return totalErrataCount;
          }}
        />

        <Column
          columnKey="outdated_packages"
          comparator={Utils.sortByText}
          header={t("Packages")}
          cell={(item) => {
            if (item.outdatedPackages !== 0) {
              return (
                <a href={`/rhn/systems/details/packages/UpgradableList.do?sid=${item.id}`}>{item.outdatedPackages}</a>
              );
            }
            return item.outdatedPackages;
          }}
        />

        <Column
          columnKey="extra_pkg_count"
          comparator={Utils.sortByText}
          header={t("Extra Packages")}
          cell={(item) => {
            if (item.extraPkgCount !== 0) {
              return (
                <a href={`/rhn/systems/details/packages/ExtraPackagesList.do?sid=${item.id}`}>{item.extraPkgCount}</a>
              );
            }
            return item.extraPkgCount;
          }}
        />

        <Column
          columnKey="config_files_with_differences"
          comparator={Utils.sortByText}
          header={t("Config Diffs")}
          cell={(item) => {
            if (item.configFilesWithDifferences !== 0) {
              return (
                <a href={`/rhn/systems/details/configuration/Overview.do?sid=${item.id}`}>
                  {item.configFilesWithDifferences}
                </a>
              );
            }
            return 0;
          }}
        />
        <Column
          columnKey="last_checkin"
          comparator={Utils.sortByText}
          header={t("Last Checked In")}
          cell={(item) => item.lastCheckin}
        />
        <Column
          columnKey="channel_labels"
          comparator={Utils.sortByText}
          header={t("Base Channel")}
          cell={(item) => {
            if (item.channelId) {
              return <a href={`/rhn/channels/ChannelDetail.do?cid=${item.channelId}`}>{item.channelLabels}</a>;
            }
            return item.channelLabels;
          }}
        />
        <Column
          columnKey="entitlement_level"
          comparator={Utils.sortByText}
          header={t("System Type")}
          cell={(item) => item.entitlementLevel}
        />
      </Table>
    </TopPanel>
  );
}
