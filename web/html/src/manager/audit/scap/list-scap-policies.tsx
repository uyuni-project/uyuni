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
  scapPolicies: any;
};

class ScapPolicy extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      selectedItems: [],
      scapPolicies: window.scapPolicies || []
    };
  }
  selectScapPolicy = (row) => {
    this.setState({
      selected: row,
    });
  };

  deleteScapPolicies = async (idList) => {
    const msgMap = {
      delete_success: t("Scap Policy has been deleted."),
      delete_success_p: t("Scap Policies have been deleted."),
    };
    try {
      const response = await Network.post("/rhn/manager/api/audit/scap/policy/delete", idList);

      if (response.success) {

        const successMessage = MessageUtils.success(
          msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]
        );
        this.setState((prevState) => ({
          messages: <Messages items={successMessage} />,
          scapPolicies: prevState.scapPolicies.filter((policy) => !idList.includes(policy.id)),
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
        `An unexpected error occurred while deleting the following Scap policies: ${idList.join(', ')}`
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
          href="/rhn/manager/audit/scap/policy/create"
        />
      </div>
    );
    const renderActionsColumn = (row) => (
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
          onClick={this.selectScapPolicy}
        />
      </div>
    );
    return (
      <span>
        <TopPanel
          title={t("Scap Policies")}
          icon="spacewalk-icon-manage-configuration-files"
          button={renderPanelButtons()}
        >
          {this.state.messages}
          <Table
            data={this.state.scapPolicies}
            identifier={(scapPolicy) => scapPolicy.id}
            initialSortColumnKey="id"
            searchField={<SearchField filter={this.searchData} />} // TODO
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}
          >
            <Column
              columnKey="name"
              columnClass="text-left"
              headerClass="text-left"
              comparator={Utils.sortByText}
              header={t("Name")}
              cell={(row) => row.policyName}
            />
            <Column
              columnKey="content"
              columnClass="text-left"
              headerClass="text-left"
              comparator={Utils.sortByText}
              header={t("Content")}
              cell={(row) => row.dataStreamName}
            />
            <Column
              width="15%"
              header={t("Actions")}
              columnClass="text-center"
              headerClass="text-center"
              cell={renderActionsColumn}
            />
          </Table>
        </TopPanel>

        <DeleteDialog
          id="delete-modal"
          title={t("Delete Scap Policy")}
          content={
            <span>
              {t("Are you sure you want to delete the Scap policy")}{" "}
              <strong>{this.state.selected?.name || t("this file")}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={() => this.deleteScapPolicies([this.state.selected?.id])}
          onClosePopUp={() => this.selectScapPolicy(null)}
        />
      </span>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<ScapPolicy />, document.getElementById("scap-policies"));
}
