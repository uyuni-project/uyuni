import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, Utils as MessageUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import Network from "utils/network";
import { Utils } from "utils/functions";

// Extend window to include scapContent
declare global {
  interface Window {
    scapContent?: ScapContentData[];
  }
}

type ScapContentData = {
  id: number;
  name: string;
  fileName: string;
  description?: string;
};

type Props = {};

type State = {
  messages: React.ReactNode;
  selectedItems: number[];
  selected?: ScapContentData | null;
  scapContent: ScapContentData[];
};

class ScapContent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
      selectedItems: [],
      scapContent: window.scapContent || [],
    };
  }

  selectScapContent = (row: ScapContentData | null) => {
    this.setState({ selected: row });
  };

  deleteScapContent = async (idList: number[]) => {
    const msgMap = {
      delete_success: t("SCAP content has been deleted."),
      delete_success_p: t("SCAP content items have been deleted."),
    };

    try {
      const response = await Network.post(
        "/rhn/manager/api/audit/scap/content/delete",
        idList
      );

      if (response.success) {
        const successMessage = MessageUtils.success(
          msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]
        );
        this.setState((prevState) => ({
          messages: <Messages items={successMessage} />,
          scapContent: prevState.scapContent.filter(
            (content) => !idList.includes(content.id)
          ),
          selectedItems: prevState.selectedItems.filter(
            (item) => !idList.includes(item)
          ),
        }));
      } else {
        const errorMessage = response.messages.map((msg) =>
          MessageUtils.error(msgMap[msg] || msg)
        );
        this.setState({
          messages: <Messages items={errorMessage} />,
        });
      }
    } catch (error) {
      const errorMessage = MessageUtils.error(
        `An unexpected error occurred while deleting the following SCAP content: ${idList.join(", ")}`
      );
      this.setState({
        messages: <Messages items={errorMessage} />,
      });
    }
  };

  handleSelectItems = (items: number[]) => {
    this.setState({ selectedItems: items });
  };

  searchData = (datum: ScapContentData, criteria?: string): boolean => {
    if (!criteria) {
      return true;
    }
    const searchTerm = criteria.toLowerCase();
    return (
      datum.name?.toLowerCase().includes(searchTerm) ||
      datum.fileName?.toLowerCase().includes(searchTerm) ||
      datum.description?.toLowerCase().includes(searchTerm)
    );
  };

  renderPanelButtons = () => (
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

  renderActionsColumn = (row: ScapContentData) => (
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
        onClick={this.selectScapContent}
      />
    </div>
  );

  render() {
    return (
      <>
        <TopPanel
          title={t("SCAP Content")}
          icon="spacewalk-icon-manage-configuration-files"
          button={this.renderPanelButtons()}
        >
          {this.state.messages}
          <Table
            data={this.state.scapContent}
            identifier={(content) => content.id}
            initialSortColumnKey="id"
            searchField={<SearchField filter={this.searchData} />}
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}
          >
            <Column
              key="name"
              columnKey="name"
              width="30%"
              comparator={Utils.sortByText}
              header={t("Name")}
              cell={(row) => row.name}
            />
            <Column
              key="description"
              columnKey="description"
              width="35%"
              comparator={Utils.sortByText}
              header={t("Description")}
              cell={(row) => row.description || ""}
            />
            <Column
              key="fileName"
              columnKey="fileName"
              width="20%"
              comparator={Utils.sortByText}
              header={t("File Name")}
              cell={(row) => row.dataStreamFileName}
            />
            <Column
              key="actions"
              columnKey="actions"
              width="15%"
              header={t("Actions")}
              columnClass="text-center"
              headerClass="text-center"
              cell={this.renderActionsColumn}
            />
          </Table>
        </TopPanel>

        <DeleteDialog
          id="delete-modal"
          title={t("Delete SCAP Content")}
          content={
            <span>
              {t("Are you sure you want to delete the SCAP content")}{" "}
              <strong>{this.state.selected?.name || t("this content")}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={() =>
            this.deleteScapContent([this.state.selected?.id!])
          }
          onClosePopUp={() => this.selectScapContent(null)}
        />
      </>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(
    <ScapContent />,
    document.getElementById("scap-content-list")
  );
};
