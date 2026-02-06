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
  DELETE: "/rhn/manager/api/audit/scap/tailoring-file/delete",
} as const;

interface TailoringFileData {
  id: number;
  name: string;
  fileName: string;
  displayFileName: string;
}

declare global {
  interface Window {
    tailoringFiles?: TailoringFileData[];
  }
}

const TailoringFiles = (): JSX.Element => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [selectedItems, setSelectedItems] = useState<number[]>([]);
  const [selected, setSelected] = useState<TailoringFileData | null>(null);
  const [tailoringFiles, setTailoringFiles] = useState<TailoringFileData[]>(window.tailoringFiles || []);

  const deleteTailoringFiles = async (idList: number[]) => {
    const msgMap: Record<string, string> = {
      delete_success: t("Tailoring file has been deleted."),
      delete_success_p: t("Tailoring files have been deleted."),
    };

    try {
      const response = await Network.post(ENDPOINTS.DELETE, idList);

      if (response.success) {
        const key = idList.length > 1 ? "delete_success_p" : "delete_success";
        const successMessage = MessageUtils.success(msgMap[key]);

        setMessages(successMessage);
        setTailoringFiles((prev) => prev.filter((f) => !idList.includes(f.id)));
        setSelectedItems((prev) => prev.filter((id) => !idList.includes(id)));
        setSelected(null);
      } else {
        const errorMsgs = response.messages.map((m: string) => MessageUtils.error(msgMap[m] || m));
        setMessages(errorMsgs);
      }
    } catch (error: unknown) {
      const errorMessage = `${t("An unexpected error occurred while deleting")}: ${idList.join(", ")}`;
      setMessages(MessageUtils.error(errorMessage));
    }
  };

  const searchFilter = (data: TailoringFileData, criteria?: string) => {
    if (!criteria) return true;
    const search = criteria.toLowerCase();
    return data.name?.toLowerCase().includes(search) || data.fileName?.toLowerCase().includes(search);
  };

  const ActionButtons = () => (
    <div className="pull-right btn-group">
      <LinkButton
        id="create"
        icon="fa-plus"
        className="btn-default"
        title={t("Create")}
        text={t("Create")}
        href="/rhn/manager/audit/scap/tailoring-file/create"
      />
    </div>
  );

  const renderActions = (row: TailoringFileData) => (
    <div className="btn-group">
      <LinkButton
        className="btn-default btn-sm"
        title={t("Edit")}
        icon="fa-edit"
        href={`/rhn/manager/audit/scap/tailoring-file/edit/${row.id}`}
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
      <TopPanel
        title={t("Tailoring Files")}
        icon="spacewalk-icon-manage-configuration-files"
        button={<ActionButtons />}
      >
        <Messages items={messages} />
        <Table
          data={tailoringFiles}
          identifier={(file) => file.id}
          initialSortColumnKey="id"
          searchField={<SearchField filter={searchFilter} />}
          selectable
          selectedItems={selectedItems}
          onSelect={setSelectedItems}
        >
          <Column
            columnKey="name"
            width="35%"
            comparator={Utils.sortByText}
            header={t("Label")}
            cell={(row: TailoringFileData) => row.name}
          />
          <Column
            columnKey="fileName"
            width="45%"
            comparator={Utils.sortByText}
            header={t("Tailoring File Name")}
            cell={(row: TailoringFileData) => row.displayFileName}
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
        title={t("Delete Tailoring file")}
        content={
          <span>
            {t("Are you sure you want to delete")} <strong>{selected?.name || t("this file")}</strong>?
          </span>
        }
        item={selected}
        onConfirm={() => selected && deleteTailoringFiles([selected.id])}
        onClosePopUp={() => setSelected(null)}
      />
    </>
  );
};

export const renderer = () => {
  const container = document.getElementById("scap-tailoring-files");
  if (container) {
    return SpaRenderer.renderNavigationReact(<TailoringFiles />, container);
  }
};
