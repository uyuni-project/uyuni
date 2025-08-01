import * as React from "react";

import { AccessGroupState } from "manager/admin/access-group/access-group";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";

type Props = {
  state: AccessGroupState;
  onChange: Function;
  errors: any;
};

const AccessGroupPermissions = (props: Props) => {
  const isChecked = (item, type) => {
    const permission = props.state.permissions.filter((p) => p.namespace === item.namespace);
    if (permission.length > 0) {
      return type === "view" ? permission[0].view : permission[0].modify;
    }
    return type === "view" ? item.view : item.modify;
  };

  return (
    <div>
       {!props.state.id ? (
        <>
          <div className="d-flex">
            <div className="me-5">
              <strong className="me-1">Name:</strong>
              {props.state.name}
            </div>
            <div className="me-5">
              <strong className="me-1">Description:</strong>
              {props.state.description}
            </div>
            <div>
              <strong className="me-1">Organization:</strong>
              {props.state.orgName}
            </div>
          </div>
          <hr></hr>
        </>): null}
      <p>
        {t("Review and modify the permissions for this custom group as needed.")}
      </p>
      <div className="d-block mb-3">
        <Button className="btn-primary pull-right" text="Add Permissions" handler={() => {}} />
      </div>
      <Table
        data={"/rhn/manager/api/admin/access-group/namespaces"}
        identifier={(item) => item.id}
        initialSortColumnKey="namespace"
        emptyText={t("No Permissions found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
      >
        <Column
          columnKey="namespace"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(item) => item.namespace}
        />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => (
            <input
              name="view"
              type="checkbox"
              checked={isChecked(item, "view")}
              disabled={!item.accessMode.includes("R")}
              onChange={() => props.onChange(item, "view")}
            />
          )}
        />
        <Column
          columnKey="modify"
          header={t("Modify")}
          cell={(item) => (
            <input
              name="modify"
              type="checkbox"
              checked={isChecked(item, "modify")}
              disabled={!item.accessMode.includes("W")}
              onChange={() => props.onChange(item, "modify")}
            />
          )}
        />
        <Column columnKey="count" header={t("Count")} cell={(item) => item.permissions} />
      </Table>
    </div>
  );
};

export default AccessGroupPermissions;
