import * as React from "react";

import { Button, LinkButton } from "components/buttons";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

const actionButtons = (type) => {
  if (type === "Built-in") {
    return (
      <div className="btn-group">
        <Button className="btn-default btn-sm" icon="fa-user" />
      </div>
    );
  } else {
    return (
      <div className="btn-group">
        <Button className="btn-default btn-sm" icon="fa-user" />
        <Button className="btn-default btn-sm" icon="fa-pencil" />
        <Button className="btn-default btn-sm" icon="fa-trash" />
      </div>
    );
  }
};
export function AccessGroupList(props) {
  return (
    <TopPanel
      title={t("Access Group Management")}
      button={
        <div className="pull-right btn-group">
          <LinkButton
            className="btn-primary"
            title={t("Create Access Group")}
            text={t("Create Access Group")}
            href="/rhn/manager/admin/access-group/create"
          />
        </div>
      }
    >
      <Table
        data={"/rhn/manager/api/admin/access-group/roles"}
        identifier={(item) => item.id}
        initialSortColumnKey="name"
        emptyText={t("No Access Group found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
      >
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Name")} cell={(item) => item.name} />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column columnKey="type" comparator={Utils.sortByText} header={t("Type")} cell={(item) => item.type} />

        <Column columnKey="users" comparator={Utils.sortByText} header={t("Users")} cell={(item) => item.users} />

        <Column
          columnKey="permissions"
          comparator={Utils.sortByText}
          header={t("permissions")}
          cell={(item) => item.permissions}
        />
        <Column
          columnKey="action"
          header={t("Actions")}
          cell={(item) => actionButtons(item.type)}
        />
      </Table>
    </TopPanel>
  );
}
