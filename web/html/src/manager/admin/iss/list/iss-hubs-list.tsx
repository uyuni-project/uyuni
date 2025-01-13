import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

import { HubListData } from "./iss-list-data-props";

const IssHubsList = (hubsList: HubListData) => {
  const [hubs] = useState(hubsList);

  const searchData = (row, criteria) => {
    const keysToSearch = ["fqdn"];
    if (criteria) {
      const needle = criteria.toLocaleLowerCase();
      return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
    }
    return true;
  };

  let componentContent = (
    <Table
      data={hubs.hubs}
      identifier={(row) => row.fqdn}
      selectable={false}
      initialSortColumnKey="fqdn"
      searchField={<SearchField filter={searchData} placeholder={t("Filter by FQDN")} />}
    >
      <Column
        columnKey="fqdn"
        comparator={Utils.sortByText}
        header={t("Peripherals FQDN")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/iss/hub/${row.id}`}>
            {row.fqdn}
          </a>
        )}
      />
      <Column columnKey="allowSync" header={t("Allow Peripheral to Sync?")} cell={(row) => row.allowSync} />
      <Column columnKey="syncOrgs" header={t("Sync all Orgs to Peripheral?")} cell={(row) => row.syncOrgs} />
      <Column
        columnKey="numberOfOrgs"
        comparator={Utils.sortByText}
        header={t("Number of Orgs Exported")}
        cell={(row) => row.numberOfOrgs}
      />
    </Table>
  );

  return componentContent;
};

export default hot(withPageWrapper(IssHubsList));
