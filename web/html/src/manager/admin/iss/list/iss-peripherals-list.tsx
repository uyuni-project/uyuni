import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

import { PeripheralsList } from "../iss_data_props";
import { Check, Radio } from "components/input";

const IssHubsList = (peripheralsList: PeripheralsList) => {
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
          <a className="js-spa" href={`/rhn/manager/admin/iss/peripheral/${row.id}`}>
            {row.fqdn}
          </a>
        )}
      />
      <Column
        columnKey="defaultHub"
        header={t("Is Default Hub?")}
        cell={(row) => <Radio items={[{ label: "is", value: row.defaultHub }]} />}
      />

      <Column columnKey="knownOrgs" header={t("Know Orgs")} cell={(row) => row.knownOrgs} />
      <Column
        columnKey="unmappedOrgs"
        comparator={Utils.sortByText}
        header={t("UnmappedOrgs")}
        cell={(row) => row.unmappedOrgs}
      />
    </Table>
  );

  return componentContent;
};

export default hot(withPageWrapper(IssHubsList));
