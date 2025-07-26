import * as React from "react";
import { useState, useRef } from "react";

import { Button, LinkButton } from "components/buttons";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";
import {Messages, MessageType, Utils as MessagesUtils} from "components/messages/messages";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";

type AccessGroupListItem = {
  id: number;
  name: string;
  description: string;
  orgName: string;
  numUsers: number;
  numPermissions: number;
};

const AccessGroupList = (props) => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [toDelete, setToDelete] = useState<AccessGroupListItem>();
  const tableRef = useRef(null);

  const onDelete = (item, tableRef) => {
    return Network.del("/rhn/manager/api/admin/access-group/delete/" + item.id)
      .then((_) => {
        setMessages(MessagesUtils.info("Access Group '" + item.name + "' has been deleted."));
        if (tableRef) {
          tableRef.current.refresh();
        }
      })
      .catch((error) => setMessages(Network.responseErrorMessage(error)));
  };

  const actionButtons = (item: AccessGroupListItem) => {
    if (item.orgName === "-") {
      return (
        <div className="btn-group">
          <Button className="btn-default btn-sm" icon="fa-user" />
        </div>
      );
    } else {
      return (
        <div className="btn-group">
          <Button className="btn-default btn-sm" icon="fa-user" />
          <LinkButton className="btn-default btn-sm" icon="fa-pencil" href={"/rhn/manager/admin/access-group/show/" + item.id} />
          <ModalButton
            className="btn-default btn-sm"
            title={t("Delete")}
            icon="fa-trash"
            target="delete-modal"
            item={item}
            onClick={(i) => setToDelete(i)}
          />
        </div>
      );
    }
  };

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
      <Messages items={messages} />
      <Table
        data={"/rhn/manager/api/admin/access-group/roles"}
        identifier={(item) => item.id}
        initialSortColumnKey="name"
        emptyText={t("No Access Group found.")}
        searchField={<SearchField placeholder={t("Filter by name")} />}
        ref={tableRef}
      >
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Name")} cell={(item) => item.name} />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />
        <Column columnKey="type" header={t("Type")} cell={(item) => item.orgName === "-" ? t("Built-In") : t("Custom")} />

        <Column columnKey="orgName" comparator={Utils.sortByText} header={t("Organization")} cell={(item) => item.orgName} />

        <Column columnKey="users" comparator={Utils.sortByText} header={t("Users")} cell={(item) => item.numUsers} />

        <Column
          columnKey="permissions"
          comparator={Utils.sortByText}
          header={t("permissions")}
          cell={(item) => item.numPermissions}
        />
        <Column
          columnKey="action"
          header={t("Actions")}
          cell={(item) => actionButtons(item)}
        />
      </Table>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete Access Group")}
        content={t("Are you sure you want to delete the selected Access Group?")}
        onConfirm={() => onDelete(toDelete, tableRef)}
        onClosePopUp={() => setToDelete(undefined)}
      />
    </TopPanel>
  );
}

export default AccessGroupList;
