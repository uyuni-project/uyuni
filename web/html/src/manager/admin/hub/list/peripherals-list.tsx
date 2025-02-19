import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

import { PeripheralListData } from "../iss_data_props";

const IssPeripheralsList = () => {
  let componentContent = (
    <Table
      data="/rhn/manager/api/admin/hub/peripherals/list"
      identifier={(row: PeripheralListData) => row.id}
      initialSortColumnKey="fqdn"
    >
      <Column
        columnKey="fqdn"
        comparator={Utils.sortByText}
        header={t("Peripherals FQDN")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/hub/peripheral/${row.id}`}>
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
          return (
            <a
              href={dlUrl}
              download={row.fqdn + "_CA.pem"}
              onClick={() => {
                URL.revokeObjectURL(dlUrl);
              }}
            >
              <i className="fa fa-download" />
            </a>
          );
        }}
      />
    </Table>
  );

  return componentContent;
};

export default hot(withPageWrapper(IssPeripheralsList));
