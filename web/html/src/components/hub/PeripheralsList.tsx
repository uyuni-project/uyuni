import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { PeripheralListData } from "components/hub";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

type Props = {
  peripherals: PeripheralListData[];
};

export const PeripheralsList = hot(
  withPageWrapper((peripheralsList: Props) => {
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
          columnKey="nAllChannels"
          comparator={Utils.sortByNumber}
          header={t("N. of All Channels")}
          cell={(row: PeripheralListData) => <span>{row.nAllChannels}</span>}
        />
        <Column
          columnKey="nOrgs"
          comparator={Utils.sortByNumber}
          header={t("N. of All Organizzation")}
          cell={(row: PeripheralListData) => <span>{row.nOrgs}</span>}
        />
        <Column
          columnKey="id"
          header={t("Download CA")}
          cell={(row) => (
            <a className="js-spa" href="_blank">
              DL by {row.id}
            </a>
          )}
        />
      </Table>
    );

    return componentContent;
  })
);
