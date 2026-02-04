import React, { useState } from "react";
import SpaRenderer from "core/spa/spa-renderer";

import { LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { ComplianceBadge } from "components/ComplianceBadge";

import Network from "utils/network";
import { Utils } from "utils/functions";

const ENDPOINTS = {
  DELETE: "/rhn/manager/api/audit/scap/policy/delete",
} as const;

interface ScapPolicyData {
  id: number;
  policyName: string;
  scapContentName?: string;
  totalSystems?: number;
  compliantSystems?: number;
  compliancePercentage?: number;
}

declare global {
  interface Window {
    scapPolicies?: ScapPolicyData[];
  }
}

const ScapPolicy = (): JSX.Element => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [selectedItems, setSelectedItems] = useState<number[]>([]);
  const [selected, setSelected] = useState<ScapPolicyData | null>(null);
  const [scapPolicies, setScapPolicies] = useState<ScapPolicyData[]>(
    window.scapPolicies || []
  );

  const deleteScapPolicies = async (idList: number[]) => {
    const msgMap: Record<string, string> = {
      delete_success: t("Scap Policy has been deleted."),
      delete_success_p: t("Scap Policies have been deleted."),
    };

    try {
      const response = await Network.post(ENDPOINTS.DELETE, idList);

      if (response.success) {
        const key = idList.length > 1 ? "delete_success_p" : "delete_success";
        const successMessage = MessageUtils.success(msgMap[key]);

        setMessages(successMessage);
        setScapPolicies((prev) => prev.filter((p) => !idList.includes(p.id)));
        setSelectedItems((prev) => prev.filter((id) => !idList.includes(id)));
        setSelected(null);
      } else {
        const errorMsgs = response.messages.map((m: string) =>
          MessageUtils.error(msgMap[m] || m)
        );
        setMessages(errorMsgs);
      }
    } catch (error: unknown) {
      const errorMessage = `${t("An unexpected error occurred while deleting")}: ${idList.join(", ")}`;
      setMessages(MessageUtils.error(errorMessage));
    }
  };

  const searchFilter = (item: ScapPolicyData, criteria?: string): boolean => {
    if (!criteria) return true;
    const search = criteria.toLowerCase();
    return (
      (item.policyName?.toLowerCase().includes(search) ?? false) ||
      (item.scapContentName?.toLowerCase().includes(search) ?? false)
    );
  };

  const ActionButtons = () => (
    <div className="pull-right btn-group">
      <LinkButton
        id="create"
        icon="fa-plus"
        className="btn-default"
        title={t("Create")}
        text={t("Create")}
        href="/rhn/manager/audit/scap/policy/create"
      />
    </div>
  );

  const renderActions = (row: ScapPolicyData) => (
    <div className="btn-group">
      <LinkButton
        className="btn-default btn-sm"
        title={t("Details")}
        icon="fa-list"
        href={`/rhn/manager/audit/scap/policy/details/${row.id}`}
      />
      <LinkButton
        className="btn-default btn-sm"
        title={t("Edit")}
        icon="fa-edit"
        href={`/rhn/manager/audit/scap/policy/edit/${row.id}`}
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
        title={t("Scap Policies")}
        icon="spacewalk-icon-manage-configuration-files"
        button={<ActionButtons />}
      >
        <Messages items={messages} />
        <Table
          data={scapPolicies}
          identifier={(policy) => policy.id}
          initialSortColumnKey="id"
          searchField={<SearchField filter={searchFilter} />}
          selectable
          selectedItems={selectedItems}
          onSelect={setSelectedItems}
        >
          <Column
            columnKey="name"
            columnClass="text-left"
            headerClass="text-left"
            comparator={Utils.sortByText}
            header={t("Name")}
            cell={(row: ScapPolicyData) => row.policyName}
          />
          <Column
            columnKey="content"
            columnClass="text-left"
            headerClass="text-left"
            comparator={Utils.sortByText}
            header={t("Content")}
            cell={(row: ScapPolicyData) => row.scapContentName || "N/A"}
          />
          <Column
            columnKey="totalSystems"
            columnClass="text-center"
            headerClass="text-center"
            comparator={Utils.sortByNumber}
            header={t("Systems Scanned")}
            cell={(row: ScapPolicyData) => row.totalSystems || 0}
          />
          <Column
            columnKey="compliance"
            columnClass="text-center"
            headerClass="text-center"
            header={t("Compliance")}
            cell={(row: ScapPolicyData) => (
              <ComplianceBadge
                percentage={row.compliancePercentage || 0}
                compliant={row.compliantSystems || 0}
                total={row.totalSystems || 0}
              />
            )}
          />
          <Column
            width="15%"
            header={t("Actions")}
            columnClass="text-center"
            columnKey="actions"
            headerClass="text-center"
            cell={renderActions}
          />
        </Table>
      </TopPanel>

      <DeleteDialog
        id="delete-modal"
        title={t("Delete Scap Policy")}
        content={
          <span>
            {t("Are you sure you want to delete")} <strong>{selected?.policyName || t("this policy")}</strong>?
          </span>
        }
        item={selected}
        onConfirm={() => selected && deleteScapPolicies([selected.id])}
        onClosePopUp={() => setSelected(null)}
      />
    </>
  );
};

export const renderer = () => {
  const container = document.getElementById("scap-policies");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScapPolicy />, container);
  }
};
