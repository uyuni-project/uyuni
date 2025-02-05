import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { PeripheralListData } from "../iss_data_props";

type DeletePeripheralProp = {
  id: number;
  fqdn: string;
};

const IssPeripheralsList = () => {
  const searchData = (row, criteria) => {
    const keysToSearch = ["fqdn"];
    if (criteria) {
      const needle = criteria.toLocaleLowerCase();
      return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
    }
    return true;
  };

  const handlePeripheralDelete = (event, peripheral: DeletePeripheralProp) => {
    Network.del("/rhn/manager/api/admin/hub/peripheral/" + peripheral.id, null)
      .catch((xhr) => Network.showResponseErrorToastr(xhr))
      .then((response) => {
        // On successfull deregister to peripheral
        showSuccessToastr(peripheral.fqdn + ": " + t("peripheral deregistration successfull."));
      })
      .finally(() => {
        //Reload peripherals table
      });
  };

  let componentContent = (
    <Table
      data="/rhn/manager/api/admin/hub/peripherals/list"
      identifier={(row: PeripheralListData) => row.id}
      initialSortColumnKey="fqdn"
      searchField={<SearchField filter={searchData} placeholder={t("Filter by FQDN")} />}
    >
      <Column
        columnKey="fqdn"
        comparator={Utils.sortByText}
        header={t("Peripherals FQDN")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/hub/peripherals/details/${row.id}`}>
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
      <Column
        columnKey="remove"
        header={t("Remove")}
        cell={(row: PeripheralListData) => (
          <i
            className="fa fa-trash"
            onClick={(e) =>
              handlePeripheralDelete(e, {
                id: parseInt(row.id, 10),
                fqdn: row.fqdn,
              })
            }
          />
        )}
      />
    </Table>
  );

  return componentContent;
};

export default hot(withPageWrapper(IssPeripheralsList));
