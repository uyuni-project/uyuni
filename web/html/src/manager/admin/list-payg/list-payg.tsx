import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useEffect } from "react";

import _truncate from "lodash/truncate";

import PaygStatus from "manager/admin/payg-shared/common/payg-status";

import { Button } from "components/buttons";
import { FromNow } from "components/datetime/FromNow";
import withPageWrapper from "components/general/with-page-wrapper";
import { ServerMessageType } from "components/messages/messages";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr/toastr";

import { Utils } from "utils/functions";

import { SetupHeader } from "../setup/setup-header";

type PaygOverviewType = {
  id: String;
  host: String;
  description: String;
  status: String;
  statusMessage: String;
  lastChange: moment.Moment;
};

type Props = {
  payg_instances: Array<PaygOverviewType>;
  flashMessage?: ServerMessageType;
};
const ListPayg = (props: Props) => {
  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  const searchData = (row, criteria) => {
    const keysToSearch = ["host", "description", "statusMessage"];
    if (criteria) {
      const needle = criteria.toLocaleLowerCase();
      return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
    }
    return true;
  };

  let pageContent = (
    <Table
      data={props.payg_instances}
      identifier={(row) => row.host}
      selectable={false}
      initialSortColumnKey="host"
      searchField={<SearchField filter={searchData} placeholder={t("Filter by any value")} />}
    >
      <Column
        columnKey="host"
        comparator={Utils.sortByText}
        header={t("Host")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/setup/payg/${row.id}`}>
            {row.host}
          </a>
        )}
      />
      <Column
        columnKey="description"
        comparator={Utils.sortByText}
        header={t("Description")}
        cell={(row) => _truncate(row.description, { length: 120 })}
      />
      <Column
        columnKey="statusMessage"
        comparator={Utils.sortByText}
        header={t("Status")}
        cell={(row) => (
          <PaygStatus status={row.status} statusMessage={_truncate(row.statusMessage || "-", { length: 120 })} />
        )}
      />
      <Column
        columnKey="lastBuildDate"
        comparator={Utils.sortByDate}
        header={t("Last Update Status")}
        cell={(row) => (row.lastChange ? <FromNow value={row.lastChange} /> : t("never"))}
      />
    </Table>
  );

  const addPayg = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/payg/create`);
  };
  return (
    <div className="responsive-wizard">
      <SetupHeader />
      <SectionToolbar>
        <div className="action-button-wrapper">
          <div className="btn-group">
            <Button
              id="addPAYG"
              icon="fa-plus"
              className={"btn-primary"}
              text={t("Add PAYG Connection")}
              handler={addPayg}
            />
          </div>
        </div>
      </SectionToolbar>
      {pageContent}
    </div>
  );
};

export default hot(withPageWrapper(ListPayg));
