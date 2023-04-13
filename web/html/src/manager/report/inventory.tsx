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
  queryColumn?: string;
  query?: string;
};

const listOptions = [
  { value: "profile_name", label: t("System") },
  { value: "total_errata_count", label: t("Patches") },
  { value: "packages_out_of_date", label: t("Packages") },
];

export function SystemsInventory(props: Props) {
  const [selectedSystems, setSelectedSystems] = React.useState<string[]>([]);

  return (
    <>
      <h1>
        <IconTag type="header-system" />
        {t(" System Inventory")}
        <a
          href={`/docs/${props.docsLocale}/reference/systems/systems-list.html`}
          target="_blank"
          rel="noopener noreferrer"
        >
          <IconTag type="header-help" />
        </a>
      </h1>

      <Table
        data="/rhn/manager/api/report/inventory"
        identifier={(item) => item.mgmId + item.systemId}
        initialSortColumnKey="profile_name"
        selectable={false}
        searchField={<SearchField options={listOptions} name="criteria" />}
        defaultSearchField={props.queryColumn || "profile_name"}
        initialSearch={props.query}
        emptyText={t("No Systems.")}
      >
        <Column
          columnKey="profile_name"
          comparator={Utils.sortByText}
          header={t("System")}
          cell={(item) => item.hostname}
        />
        <Column
          columnKey="kernelVersion"
          comparator={Utils.sortByText}
          header={t("Kernel Version")}
          cell={(item) => item.kernelVersion || "-"}
        />
        <Column
          columnKey="totalErrataCount"
          comparator={Utils.sortByText}
          header={t("Patches")}
          cell={(item) => {
            let totalErrataCount = item.errataOutOfDate;
            return totalErrataCount;
          }}
        />

        <Column
          columnKey="outdatedPackages"
          comparator={Utils.sortByText}
          header={t("Packages")}
          cell={(item) => {
            return item.packagesOutOfDate;
          }}
        />
        <Column
          columnKey="lastCheckin"
          comparator={Utils.sortByText}
          header={t("Last Checkin")}
          cell={(item) => item.lastCheckinTime}
        />
        <Column
        columnKey="syncedDate"
        comparator={Utils.sortByText}
                header={t("Data Update at")}
                cell={(item) => item.syncedDate}
                />
      </Table>

    </>
  );
}
