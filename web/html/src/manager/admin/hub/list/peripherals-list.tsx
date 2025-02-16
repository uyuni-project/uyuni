import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

import { PeripheralListData, PeripheralsListProp } from "../iss_data_props";

const IssPeripheralsList = (peripheralsList: PeripheralsListProp) => {
  const [peripherals] = useState(peripheralsList.peripherals);

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
      data={peripherals}
      identifier={(row: PeripheralListData) => row.id}
      selectable={false}
      initialSortColumnKey="fqdn"
      searchField={<SearchField filter={searchData} placeholder={t("Filter by FQDN")} />}
    >
      <Column
        columnKey="fqdn"
        comparator={Utils.sortByText}
        header={t("Peripherals FQDN")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/hub/peripherals/${row.id}`}>
            {row.fqdn}
          </a>
        )}
      />
      <Column
        columnKey="nChannelsSync"
        comparator={Utils.sortByNumber}
        header={t("N. of Sync Channels")}
        cell={(row: PeripheralListData) => <span>{row.nChannelsSync}</span>}
      />
      <Column
        columnKey="nOrgs"
        comparator={Utils.sortByNumber}
        header={t("N. of Sync Orgs")}
        cell={(row: PeripheralListData) => <span>{row.nSyncOrgs}</span>}
      />
      <Column
        columnKey="id"
        header={t("Download CA")}
        cell={(row: PeripheralListData) => {
          let rootCABlob = new Blob([row.rootCA], { type: "text/plain" });
          let dlUrl = URL.createObjectURL(rootCABlob);
          <a
            href={dlUrl}
            onClick={() => {
              URL.revokeObjectURL(dlUrl);
            }}
          >
            <i className="bi bi-download" />
          </a>;
        }}
      />
    </Table>
  );

  return componentContent;
};

export default hot(withPageWrapper(IssPeripheralsList));
