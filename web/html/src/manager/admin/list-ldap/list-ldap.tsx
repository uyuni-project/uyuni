import { useEffect } from "react";

import { Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { ServerMessageType } from "components/messages/messages";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr/toastr";

import { Utils } from "utils/functions";

import { LdapServerResume } from "../ldap-shared/ldap-types";
import { SetupHeader } from "../setup/setup-header";

type Props = {
  ldap_servers: LdapServerResume[];
  flashMessage?: ServerMessageType;
};

const ListLdap = (props: Props) => {
  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  const searchData = (row, criteria) => {
    const keysToSearch = ["label", "host", "serverType", "transport"];
    if (criteria) {
      const needle = criteria.toLocaleLowerCase();
      return keysToSearch
        .map((key) => String(row[key] ?? ""))
        .some((item) => item.toLocaleLowerCase().includes(needle));
    }
    return true;
  };

  const addLdap = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/ldap/create`);
  };

  return (
    <div className="responsive-wizard">
      <SetupHeader />
      <SectionToolbar>
        <div className="action-button-wrapper">
          <div className="btn-group">
            <Button
              id="addLdap"
              icon="fa-plus"
              className={"btn-primary"}
              text={t("Add LDAP Server")}
              handler={addLdap}
            />
          </div>
        </div>
      </SectionToolbar>
      <Table
        data={props.ldap_servers}
        identifier={(row) => row.id}
        selectable={false}
        initialSortColumnKey="priority"
        searchField={<SearchField filter={searchData} placeholder={t("Filter by any value")} />}
      >
        <Column
          columnKey="label"
          comparator={Utils.sortByText}
          header={t("Label")}
          cell={(row) => (
            <a className="js-spa" href={`/rhn/manager/admin/setup/ldap/${row.id}`}>
              {row.label}
            </a>
          )}
        />
        <Column columnKey="host" comparator={Utils.sortByText} header={t("Host")} cell={(row) => row.host} />
        <Column columnKey="port" comparator={Utils.sortByNumber} header={t("Port")} cell={(row) => row.port} />
        <Column
          columnKey="transport"
          comparator={Utils.sortByText}
          header={t("Transport")}
          cell={(row) => row.transport}
        />
        <Column
          columnKey="serverType"
          comparator={Utils.sortByText}
          header={t("Type")}
          cell={(row) => row.serverType}
        />
        <Column
          columnKey="enabled"
          comparator={Utils.sortByText}
          header={t("Enabled")}
          cell={(row) => (row.enabled ? t("Yes") : t("No"))}
        />
        <Column
          columnKey="priority"
          comparator={Utils.sortByNumber}
          header={t("Priority")}
          cell={(row) => row.priority}
        />
      </Table>
    </div>
  );
};

export default withPageWrapper(ListLdap);
