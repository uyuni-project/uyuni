import * as React from "react";

import { IconTag } from "components/icontag";
import * as Systems from "components/systems";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type Props = {
  /** Locale of the help links */
  docsLocale: string;
  isAdmin: boolean;
};

export function AllSystems(props: Props) {
  const [selectedSystems, setSelectedSystems] = React.useState<String[]>([]);

  const handleSelectedSystems = (items: String[]) => {
    const removed = selectedSystems.filter((item) => !items.includes(item)).map((item) => [item, false]);
    const added = items.filter((item) => !selectedSystems.includes(item)).map((item) => [item, true]);
    const data = Object.assign({}, Object.fromEntries(added), Object.fromEntries(removed));
    Network.post("/rhn/manager/api/sets/system_list", data);

    setSelectedSystems(items);
  };

  const searchData = (datum, criteria) => {
    if (criteria) {
      return datum.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());
    }
    return true;
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
      </h1>

      <Table
        data="/rhn/manager/api/systems/list/all"
        identifier={(item) => item.id}
        initialSortColumnKey="name"
        selectable={(item) => item.hasOwnProperty("id")}
        selectedItems={selectedSystems}
        onSelect={handleSelectedSystems}
        searchField={<SearchField filter={searchData} placeholder={t("Filter by System Name")} />}
        emptyText={t("No Systems.")}
      >
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("System")}
          cell={(item) => {
            if (item.id != null) {
              return <a href={`/rhn/systems/details/Overview.do?sid=${item.id}`}>{item.name}</a>;
            }
            return item.name;
          }}
        />

        <Column
          columnKey="statusType"
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
            if (totalErrataCount != 0) {
              return <a href={`/rhn/systems/details/ErrataList.do?sid=${item.id}`}>{totalErrataCount}</a>;
            }
            return totalErrataCount;
          }}
        />

        <Column
          columnKey="outdatedPackages"
          comparator={Utils.sortByText}
          header={t("Packages")}
          cell={(item) => {
            if (item.outdatedPackages != 0) {
              return (
                <a href={`/rhn/systems/details/packages/UpgradableList.do?sid=${item.id}`}>{item.outdatedPackages}</a>
              );
            }
            return item.outdatedPackages;
          }}
        />

        <Column
          columnKey="extraPkgCount"
          comparator={Utils.sortByText}
          header={t("Extra Packages")}
          cell={(item) => {
            if (item.extraPkgCount != 0) {
              return (
                <a href={`/rhn/systems/details/packages/ExtraPackagesList.do?sid=${item.id}`}>{item.extraPkgCount}</a>
              );
            }
            return item.outdatedPackages;
          }}
        />

        <Column
          columnKey="configFilesWithDifferences"
          comparator={Utils.sortByText}
          header={t("Config Diffs")}
          cell={(item) => {
            if (item.configFilesWithDifferences != (0 || null)) {
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
          columnKey="groupCount"
          comparator={Utils.sortByText}
          header={t("Groups")}
          cell={(item) => {
            if (item.groupCount != 0) {
              return (
                <a href={`/rhn/systems/details/packages/ExtraPackagesList.do?sid=${item.id}`}>{item.groupCount}</a>
              );
            }
            return 0;
          }}
        />

        <Column
          columnKey="channelLabels"
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
          columnKey="entitlementLevel"
          comparator={Utils.sortByText}
          header={t("System Type")}
          cell={(item) => item.entitlementLevel}
        />
      </Table>
    </>
  );
}
