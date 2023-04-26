import * as React from "react";

import { Utils } from "utils/functions";
import { useState } from "react";
import { AsyncButton } from "components/buttons";
import { TextField } from "components/fields";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import Network from "utils/network";

type Props = {
  /** Locale of the help links */
  docsLocale: string;
  query?: string;
};

export function CveSearch(props: Props) {

const [isLoading, setIsLoading] = useState(false);
const [query, setQuery] = useState({query: props.query})
const [data, setData] = useState([])

const onSearchChange = (event) => {
  setQuery({
    query: event.target.value,
  });
};

const search = () => {
  return Promise.resolve().then(() => {
console.log(query)
    Network.get(`/rhn/manager/api/report/inventory?`+query.query).then((data) => {
        setData(data.items)
      });
  });
};

  return (
    <>
      <h1>
        {t(" System CVE Search")}
      </h1>
      <div className="row">
        <div className={"col-md-5"}>
          <TextField
                id="search-field"
                value={props.query}
        placeholder={t("Search for affected systems")}
        onChange={onSearchChange}
        onPressEnter={search}
        />
        </div>
        <div className={"col-md-5"}>
          <AsyncButton
                  id="search"
                  className="btn-primary"
                  title={t("Search")}
          text={t("Search")}
          action={search}
          />
        </div>
      </div>
      <hr/>
      <div className="row">
        <div className={"col-md-12"}>
        <Table
                data={data}
                identifier={(item) => item.mgmId + item.systemId}
        initialSortColumnKey="profile_name"
        selectable={false}
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
      </div>
      </div>
    </>
  );
}
