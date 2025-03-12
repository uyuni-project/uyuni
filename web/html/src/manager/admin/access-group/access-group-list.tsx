import * as React from "react";

import { TopPanel } from "components/panels/TopPanel";
import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";
import { SearchField } from "components/table/SearchField";

const dataTest = {
  "items": [
    {
      "id": 1000010000,
      "name": "Content Management",
      "description": "View image details, patches, packages, build log and cluster information",
      "type": "Built-in",
      "users": 0,
      "permissions": 15,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000020000,
      "name": "Activation Key Administrator",
      "description": "View Activation Administrator, Configuration Administrator.",
      "type": "Built-in",
      "users": 2,
      "permissions": 5,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000030000,
      "name": "Configuration Administrator",
      "description": "View Configuration Administrator Administrator, Configuration Administrator.",
      "type": "Built-in",
      "users": 4,
      "permissions": 15,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000040000,
      "name": "System Group Administrator",
      "description": "View Activation Administrator, Configuration Administrator.",
      "type": "Built-in",
      "users": 2,
      "permissions": 5,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000050000,
      "name": "Custom group1",
      "description": "Custom group1 Activation Administrator, Configuration Administrator.",
      "type": "Custome",
      "users": 7,
      "permissions": 25,
      "selected": false,
      "disabled": false
    },
  ],
  "total": 19,
  "selectedIds": []
}

const actionButtons = (type) => {
  if (type === "Built-in") {
    return (
      <div className="btn-group">
        <Button className="btn-default btn-sm" icon="fa-user" />
      </div>
    )
  } else {
    return (<div className="btn-group">
      <Button className="btn-default btn-sm" icon="fa-user" />
      <Button className="btn-default btn-sm" icon="fa-pencil" />
      <Button className="btn-default btn-sm" icon="fa-trash" />
    </div>)
  }
}
export function AccessGroupList(props) {
  const [systemsData, setSystemsData] = useState(dataTest);
  const addAccessGroup = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/access-group/create`);
  };
  return (
    <TopPanel
      title={t("Access Group Management")}
      button={
        <div className="pull-right btn-group">
          <Button
            className="btn-primary"
            title={t("Delete")}
            text={t("Create Access Group")}
            handler={addAccessGroup}
          />
        </div>
      }
    >
      <Table
        data={systemsData.items}
        identifier={(item) => item.id}
        initialSortColumnKey="group_name"
        emptyText={t("No Access Group found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
      >
        <Column
          columnKey="group_name"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(item) => item.name}
        />
        <Column
          columnKey="group_description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column
          columnKey="group_type"
          comparator={Utils.sortByText}
          header={t("Type")}
          cell={(item) => item.type}
        />

        <Column
          columnKey="group_users"
          comparator={Utils.sortByText}
          header={t("Users")}
          cell={(item) => item.users}
        />

        <Column
          columnKey="group_permissions"
          comparator={Utils.sortByText}
          header={t("permissions")}
          cell={(item) => item.permissions}
        />
        <Column
          columnKey="action"
          comparator={Utils.sortByText}
          header={t("Actions")}
          cell={(item) => actionButtons(item.type)}
        />
      </Table>
    </TopPanel>
  );
};
