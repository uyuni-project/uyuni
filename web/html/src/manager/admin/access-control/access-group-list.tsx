import * as React from "react";
import { useEffect, useState } from "react";

import { Button, LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type AccessGroupListItem = {
  id: number;
  name: string;
  description: string;
  orgName: string;
  numUsers: number;
  numPermissions: number;
};

const AccessGroupList = (props) => {
  const [accessGroups, setAccessGroups] = useState<AccessGroupListItem[]>([]);
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [toDelete, setToDelete] = useState<AccessGroupListItem>();

  const searchData = (item, criteria) => {
    if (!criteria) {
      return true;
    }
    const lowerCaseCriteria = criteria.trim().toLowerCase();
    return (
      item.name?.toLowerCase().includes(lowerCaseCriteria) ||
      item.description?.toLowerCase().includes(lowerCaseCriteria)
    );
  };

  useEffect(() => {
    getRoles();
  }, []);
  const getRoles = () => {
    const endpoint = "/rhn/manager/api/admin/access-control/access-group/list_custom";
    Network.get(endpoint)
      .then((groups) => {
        setAccessGroups(groups);
      })
      .catch(() => {
        setMessages(
          messages.concat(MessagesUtils.error(t("An unexpected error occurred while fetching access groups.")))
        );
      });
  };

  const onDelete = (item) => {
    return Network.del("/rhn/manager/api/admin/access-control/access-group/delete/" + item.id)
      .then((_) => {
        setMessages(MessagesUtils.info("Access Group '" + item.name + "' has been deleted."));
        setAccessGroups(accessGroups.filter((g) => g.id !== item.id));
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
          <LinkButton
            className="btn-default btn-sm"
            icon="fa-pencil"
            href={`/rhn/manager/admin/access-control/show-access-group/${item.id}`}
          />
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
            href="/rhn/manager/admin/access-control/create-access-group"
          />
        </div>
      }
    >
      <Messages items={messages} />
      <Table
        data={accessGroups}
        identifier={(item) => item.id}
        initialSortColumnKey="name"
        emptyText={t("No Access Group found.")}
        searchField={<SearchField filter={searchData} placeholder={t("Filter by name or description")} />}
      >
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Name")} cell={(item) => item.name} />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(item) => item.description}
        />

        <Column
          columnKey="orgName"
          comparator={Utils.sortByText}
          header={t("Organization")}
          cell={(item) => item.orgName}
        />

        <Column columnKey="users" comparator={Utils.sortByText} header={t("Users")} cell={(item) => item.numUsers} />

        <Column
          columnKey="permissions"
          comparator={Utils.sortByText}
          header={t("permissions")}
          cell={(item) => item.numPermissions}
        />
        <Column columnKey="action" header={t("Actions")} cell={(item) => actionButtons(item)} />
      </Table>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete Access Group")}
        content={t("Are you sure you want to delete the selected Access Group?")}
        onConfirm={() => onDelete(toDelete)}
        onClosePopUp={() => setToDelete(undefined)}
      />
    </TopPanel>
  );
};

export default AccessGroupList;
