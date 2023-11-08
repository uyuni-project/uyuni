import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils as MessageUtils } from "components/messages/messages";
import Network from "utils/network";
import { TopPanel } from "components/panels/TopPanel";
import { LinkButton } from "components/buttons";
import { SearchField } from "components/table/SearchField";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Utils } from "utils/functions";

type Props = {};

type State = {
  messages: any;
  selectedItems: any;
  selected?: any;
  tailoringFiles: any;
};

class TailoringFiles extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      selectedItems: [],
      tailoringFiles: window.tailoringFiles || []
    };
  }
  selectTailoringFile = (row) => {
    this.setState({
      selected: row,
    });
  };

  deleteTailoringFiles = async (idList) => {
    const msgMap = {
      delete_success: t("Tailoring file has been deleted."),
      delete_success_p: t("Tailoring files have been deleted."),
    };
    try {
      const response = await Network.post("/rhn/manager/api/audit/scap/tailoring-file/delete", idList);

      if (response.success) {

        const successMessage = MessageUtils.success(
          msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]
        );
        this.setState((prevState) => ({
          messages: <Messages items={successMessage} />,
          tailoringFiles: prevState.tailoringFiles.filter((file) => !idList.includes(file.id)),
          selectedItems: prevState.selectedItems.filter((item) => !idList.includes(item)),
        }));
      } else {
        const errorMessage = response.messages.map((msg) =>
          MessageUtils.error(msgMap[msg] || msg) // Handle case if the msgMap is not defined for some messages
        );

        this.setState({
          messages: <Messages items={errorMessage} />,
        });
      }
    } catch (error) {
      const errorMessage = MessageUtils.error(
        `An unexpected error occurred while deleting the following tailoring files: ${idList.join(', ')}`
      );
      this.setState({
        messages: <Messages items={errorMessage} />,
      });
    }
  };

  handleSelectItems = (items) => {
    this.setState({
      selectedItems: items,
    });
  };

  render() {
    const renderPanelButtons = () => (
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
    const renderActionsColumn = (row) => (
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
          onClick={this.selectTailoringFile}
        />
      </div>
    );
    return (
      <span>
        <TopPanel
          title={t("Tailoring Files")}
          icon="spacewalk-icon-manage-configuration-files"
          button={renderPanelButtons()}
        >
          {this.state.messages}
          <Table
            data={this.state.tailoringFiles}
            identifier={(tailoringFile) => tailoringFile.id}
            initialSortColumnKey="id"
            searchField={<SearchField filter={this.searchData} />} // TODO
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}
          >
            <Column
              columnKey="label"
              width="35%"
              comparator={Utils.sortByText}
              header={t("Label")}
              cell={(row) => row.name}
            />
            <Column
              columnKey="fileName"
              width="45%"
              comparator={Utils.sortByText}
              header={t("Tailoring File Name")}
              cell={(row) => row.fileName}
            />
            <Column
              width="15%"
              header={t("Actions")}
              columnClass="text-right"
              headerClass="text-right"
              cell={renderActionsColumn}
            />
          </Table>
        </TopPanel>

        <DeleteDialog
          id="delete-modal"
          title={t("Delete Tailoring file")}
          content={
            <span>
              {t("Are you sure you want to delete the Tailoring file")}{" "}
              <strong>{this.state.selected?.name || t("this file")}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={() => this.deleteTailoringFiles([this.state.selected?.id])}
          onClosePopUp={() => this.selectTailoringFile(null)}
        />
      </span>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<TailoringFiles />, document.getElementById("scap-tailoring-files"));
}
