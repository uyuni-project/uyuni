import { useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

const ENDPOINTS = {
  DELETE: "/rhn/manager/api/audit/scap/content/delete",
} as const;

interface ScapContentData {
  id: number;
  name: string;
  fileName: string;
  dataStreamFileName: string;
  description?: string;
}

declare global {
  interface Window {
    scapContent?: ScapContentData[];
  }
}

const ScapContent = (): JSX.Element => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [selectedItems, setSelectedItems] = useState<number[]>([]);
  const [selected, setSelected] = useState<ScapContentData | null>(null);
  const [scapContent, setScapContent] = useState<ScapContentData[]>(window.scapContent || []);

  const deleteScapContent = async (idList: number[]) => {
    const msgMap: Record<string, string> = {
      delete_success: t("SCAP content has been deleted."),
      delete_success_p: t("SCAP content items have been deleted."),
    };

    try {
      const response = await Network.post(ENDPOINTS.DELETE, idList);

      if (response.success) {
        const key = idList.length > 1 ? "delete_success_p" : "delete_success";
        const successMessage = MessageUtils.success(msgMap[key]);

        setMessages(successMessage);
        setScapContent((prev) => prev.filter((c) => !idList.includes(c.id)));
        setSelectedItems((prev) => prev.filter((id) => !idList.includes(id)));
        setSelected(null);
      } else {
        // Handle server-returned error messages
        setMessages(MessageUtils.error(response.messages || [t("Failed to delete SCAP content")]));
      }
    } catch (error: any) {
      const errorMessage = `${t("An unexpected error occurred while deleting")}: ${idList.join(", ")}`;
      setMessages(MessageUtils.error(errorMessage));
    }
  };

  const searchFilter = (item: ScapContentData, criteria?: string): boolean => {
    if (!criteria) return true;
    const search = criteria.toLowerCase();
    return (
      (item.name?.toLowerCase().includes(search) ?? false) ||
      (item.fileName?.toLowerCase().includes(search) ?? false) ||
      (item.description?.toLowerCase().includes(search) ?? false)
    );
  };

  const ActionButtons = () => (
    <div className="pull-right btn-group">
      <LinkButton
        id="upload"
        icon="fa-upload"
        className="btn-default"
        title={t("Upload")}
        text={t("Upload")}
        href="/rhn/manager/audit/scap/content/create"
      />
    </div>
  );

  const renderActions = (row: ScapContentData) => (
    <div className="btn-group">
      <LinkButton
        className="btn-default btn-sm"
        title={t("Edit")}
        icon="fa-edit"
        href={`/rhn/manager/audit/scap/content/edit/${row.id}`}
      />
      <ModalButton
        className="btn-default btn-sm"
        title={t("Delete")}
        icon="fa-trash"
        target="delete-modal"
        item={row}
        onClick={setSelected}
      />
    </div>
  );

  return (
    <>
      <TopPanel title={t("SCAP Content")} icon="spacewalk-icon-manage-configuration-files" button={<ActionButtons />}>
        <Messages items={messages} />
        <Table
          data={scapContent}
          identifier={(content) => content.id}
          initialSortColumnKey="id"
          searchField={<SearchField filter={searchFilter} />}
          selectable
          selectedItems={selectedItems}
          onSelect={setSelectedItems}
        >
          <Column
            columnKey="name"
            width="30%"
            comparator={Utils.sortByText}
            header={t("Name")}
            cell={(row: ScapContentData) => row.name}
          />
          <Column
            columnKey="description"
            width="35%"
            comparator={Utils.sortByText}
            header={t("Description")}
            cell={(row: ScapContentData) => row.description || ""}
          />
          <Column
            columnKey="fileName"
            width="20%"
            comparator={Utils.sortByText}
            header={t("File Name")}
            cell={(row: ScapContentData) => row.dataStreamFileName}
          />
          <Column
            columnKey="actions"
            width="15%"
            header={t("Actions")}
            columnClass="text-center"
            headerClass="text-center"
            cell={renderActions}
          />
        </Table>
      </TopPanel>

      <DeleteDialog
        id="delete-modal"
        title={t("Delete SCAP Content")}
        content={
          <span>
            {t("Are you sure you want to delete")} <strong>{selected?.name || t("this content")}</strong>?
          </span>
        }
        item={selected}
        onConfirm={() => selected && deleteScapContent([selected.id])}
        onClosePopUp={() => setSelected(null)}
      />
    </>
  );
};

export const renderer = () => {
  const container = document.getElementById("scap-content-list");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScapContent />, container);
  }
};
