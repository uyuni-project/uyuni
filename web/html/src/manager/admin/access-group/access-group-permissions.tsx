import * as React from "react";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

type Props = {
  state: any;
  onChange: Function;
  errors: any;
};

const AccessGroupPermissions = (props: Props) => {
  return (
    <div>
      <div className="d-flex">
        <div className="me-5">
          <strong className="me-1">Name:</strong>
          {props.state.detailsproperties.name}
        </div>
        <div>
          <strong className="me-1">Description:</strong>
          {props.state.detailsproperties.description}
        </div>
      </div>
      <hr></hr>
      <p>
        Click <strong>Add Permissions</strong> to select the permissions you want to add to this custom Access group.
      </p>
      <div className="d-block mb-3">
        <Button className="btn-primary pull-right" text="Add Permissions" />
      </div>
      <Table
        data={"/rhn/manager/api/admin/access-group/namespaces"}
        identifier={(item) => item.id}
        initialSortColumnKey="namespace"
        emptyText={t("No Permissions found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
      >
        <Column columnKey="namespace" comparator={Utils.sortByText} header={t("Name")} cell={(item) => item.namespace} />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => <input name="view" type="checkbox" disabled={!item.accessMode.includes("R")} checked={false} />}
        />

        <Column
          columnKey="modify"
          header={t("Modify")}
          cell={(item) => <input name="modify" type="checkbox" disabled={!item.accessMode.includes("W")} checked={false} />}
        />
        <Column
          columnKey="count"
          header={t("Count")}
          cell={(item) => item.permissions} />
      </Table>
    </div>
  );
};

export default AccessGroupPermissions;
