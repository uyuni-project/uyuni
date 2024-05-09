import * as React from "react";

import { IconTag } from "components/icontag";
import * as Systems from "components/systems";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

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

export function AllSystems(props: Props) {
  const [selectedSystems, setSelectedSystems] = React.useState<string[]>([]);

  const handleSelectedSystems = (items: string[]) => {
    const removed = selectedSystems.filter((item) => !items.includes(item)).map((item) => [item, false]);
    const added = items.filter((item) => !selectedSystems.includes(item)).map((item) => [item, true]);
    const data = Object.assign({}, Object.fromEntries(added), Object.fromEntries(removed));
    Network.post("/rhn/manager/api/sets/system_list", data).catch(Network.showResponseErrorToastr);

    setSelectedSystems(items);
  };

  return (
    <>
      <h1>
        <IconTag type="header-system" />
        {t(" Systems ")}
        <a
          href={`/docs/${props.docsLocale}/reference/systems/systems-list.html`}
          target="_blank"
          rel="noopener noreferrer"
        >
          <IconTag type="header-help" />
        </a>

        <div className="pull-right btn-group">
          <a id="addsystem" title="Add a system" className="btn btn-link" href="/rhn/manager/systems/bootstrap">
            <i className="fa fa-plus"></i>
            {t("Add System")}
          </a>
        </div>
      </h1>
      <Table
        data="/rhn/manager/api/systems/list/all"
        identifier={(item) => item.id}
        initialSortColumnKey="server_name"
        selectable={(item) => item.hasOwnProperty("id")}
        selectedItems={selectedSystems}
        onSelect={handleSelectedSystems}
        searchField={<SystemsListFilter />}
        defaultSearchField={props.queryColumn || "server_name"}
        initialSearch={props.query}
        emptyText={t("No Systems.")}
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
            if (item.statusType == null) {
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
            let totalErrataCount = item.securityErrata + item.bugErrata + item.enhancementErrata;
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
            return item.outdatedPackages;
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
          columnKey="channel_labels"
          comparator={Utils.sortByText}
          header={t("Base Channel")}
          cell={(item) => {
            if (item.channelId != null) {
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

      <div className="spacewalk-csv-download">
        <a href="/rhn/manager/systems/csv/all" className="btn btn-link" data-senna-off="true">
          <IconTag type="item-download-csv" />
          Download CSV
        </a>
      </div>
    </>
  );
}
