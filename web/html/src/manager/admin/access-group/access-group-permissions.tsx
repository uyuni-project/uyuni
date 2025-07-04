import * as React from "react";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

const dataTest = {
  items: [
    {
      id: 1000010000,
      name: "Content Management",
      description: "View image details, patches, packages, build log and cluster information",
      type: "Built-in",
      users: 0,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000020000,
      name: "Activation Key Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000030000,
      name: "Configuration Administrator",
      description: "View Configuration Administrator Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 4,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000040000,
      name: "System Group Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000050000,
      name: "Custom group1",
      description: "Custom group1 Activation Administrator, Configuration Administrator.",
      type: "Custome",
      users: 7,
      permissions: 25,
      selected: false,
      disabled: false,
    },
    {
      id: 1000060000,
      name: "Content Management",
      description: "View image details, patches, packages, build log and cluster information",
      type: "Built-in",
      users: 0,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000070000,
      name: "Activation Key Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000080000,
      name: "Configuration Administrator",
      description: "View Configuration Administrator Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 4,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000090000,
      name: "System Group Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000100000,
      name: "Custom group1",
      description: "Custom group1 Activation Administrator, Configuration Administrator.",
      type: "Custome",
      users: 7,
      permissions: 25,
      selected: false,
      disabled: false,
    },
    {
      id: 1000011000,
      name: "Content Management",
      description: "View image details, patches, packages, build log and cluster information",
      type: "Built-in",
      users: 0,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000012000,
      name: "Activation Key Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000013000,
      name: "Configuration Administrator",
      description: "View Configuration Administrator Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 4,
      permissions: 15,
      selected: false,
      disabled: false,
    },
    {
      id: 1000014000,
      name: "System Group Administrator",
      description: "View Activation Administrator, Configuration Administrator.",
      type: "Built-in",
      users: 2,
      permissions: 5,
      selected: false,
      disabled: false,
    },
    {
      id: 1000015000,
      name: "Custom group1",
      description: "Custom group1 Activation Administrator, Configuration Administrator.",
      type: "Custome",
      users: 7,
      permissions: 25,
      selected: false,
      disabled: false,
    },
  ],
  total: 19,
  selectedIds: [],
};

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
        data={dataTest.items}
        identifier={(item) => item.id}
        initialSortColumnKey="server_name"
        emptyText={t("No Permissions found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
      >
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Name")} cell={(item) => item.name} />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column
          columnKey="view"
          comparator={Utils.sortByText}
          header={t("View")}
          cell={<input name="view" type="checkbox" />}
        />

        <Column
          columnKey="modify"
          comparator={Utils.sortByText}
          header={t("Modify")}
          cell={<input name="Modify" type="checkbox" />}
        />
        <Column columnKey="count" comparator={Utils.sortByText} header={t("Count")} cell={(item) => item.permissions} />
      </Table>
    </div>
  );
};

export default AccessGroupPermissions;
