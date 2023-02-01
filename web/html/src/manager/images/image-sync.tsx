import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton, LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/controllers/image/templates/list-image-sync.jade
declare global {
  interface Window {
    isAdmin?: any;
  }
}

const msgMap = {
  not_found: t("Sync project cannot be found."),
  delete_success: t("Sync project has been deleted."),
  delete_success_p: t("Sync projects have been deleted."),
};

type Props = {};

type State = {
  messages: any;
  syncprojects: any;
  selectedItems: any;
  selected?: any;
};

class ImageSync extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      syncprojects: [],
      selectedItems: [],
    };
  }

  componentDidMount() {
    this.reloadData();
  }

  searchData(row, criteria) {
    if (criteria) {
      return row.label.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());
    }
    return true;
  }

  reloadData = () => {
    Network.get("/rhn/manager/api/cm/imagesync").then((data) => {
      this.setState({
        syncprojects: data,
      });
    });
    this.clearMessages();
  };

  clearMessages() {
    this.setState({
      messages: undefined,
    });
  }

  handleSelectItems = (items) => {
    this.setState({
      selectedItems: items,
    });
  };

  selectProject = (row) => {
    this.setState({
      selected: row,
    });
  };

  deleteProjects = (idList) => {
    return Network.post("/rhn/manager/api/cm/imagesync/delete", idList).then((data) => {
      if (data.success) {
        this.setState({
          messages: (
            <Messages
              items={[
                {
                  severity: "success",
                  text: msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"],
                },
              ]}
            />
          ),
          syncprojects: this.state.syncprojects.filter((project) => !idList.includes(project.id)),
          selectedItems: this.state.selectedItems.filter((item) => !idList.includes(item)),
        });
      } else {
        this.setState({
          messages: (
            <Messages
              items={data.messages.map((msg) => {
                return { severity: "error", text: msgMap[msg] };
              })}
            />
          ),
        });
      }
    });
  };

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  render() {
    const panelButtons = (
      <div className="pull-right btn-group">
        {window.isAdmin && this.state.selectedItems.length > 0 && (
          <ModalButton
            id="delete-selected"
            icon="fa-trash"
            className="btn-default"
            text={t("Delete")}
            title={t("Delete selected")}
            target="delete-selected-modal"
          />
        )}
        <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
        {window.isAdmin && (
          <LinkButton
            id="create"
            icon="fa-plus"
            className="btn-default"
            title={t("Create")}
            text={t("Create")}
            href="/rhn/manager/cm/imagesync/create"
          />
        )}
      </div>
    );

    return (
      <span>
        <TopPanel
          title={t("Image Synchronization")}
          icon="fa-list"
          helpUrl="reference/images/images-sync.html"
          button={panelButtons}
        >
          {this.state.messages}
          <Table
            data={this.state.syncprojects}
            identifier={(project) => project.id}
            initialSortColumnKey="id"
            searchField={<SearchField filter={this.searchData} />}
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}
          >
            <Column
              columnKey="label"
              width="50%"
              comparator={Utils.sortByText}
              header={t("Label")}
              cell={(row) => row.label}
            />
            <Column
              columnKey="target"
              width="35%"
              comparator={Utils.sortByText}
              header={t("Target Registry")}
              cell={(row) => row.target}
            />
            {window.isAdmin && (
              <Column
                width="15%"
                columnClass="text-right"
                headerClass="text-right"
                header={t("Actions")}
                cell={(row) => {
                  return (
                    <div className="btn-group">
                      <LinkButton
                        className="btn-default btn-sm"
                        title={t("Browse")}
                        icon="fa-list"
                        href={"/rhn/manager/cm/imagesync/edit/" + row.id}
                      />
                      <LinkButton
                        className="btn-default btn-sm"
                        title={t("Edit")}
                        icon="fa-edit"
                        href={"/rhn/manager/cm/imagesync/edit/" + row.id}
                      />
                      <ModalButton
                        className="btn-default btn-sm"
                        title={t("Delete")}
                        icon="fa-trash"
                        target="delete-modal"
                        item={row}
                        onClick={this.selectProject}
                      />
                    </div>
                  );
                }}
              />
            )}
          </Table>
        </TopPanel>
        <DeleteDialog
          id="delete-modal"
          title={t("Delete Sync Project")}
          content={
            <span>
              {t("Are you sure you want to delete the selected sync project")}{" "}
              <strong>{this.state.selected ? this.state.selected.label : ""}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={(item) => this.deleteProjects([item.id])}
          onClosePopUp={() => this.selectProject(undefined)}
        />
        <DeleteDialog
          id="delete-selected-modal"
          title={t("Delete Selected Sync Project(s)")}
          content={
            <span>
              {DEPRECATED_unsafeEquals(this.state.selectedItems.length, 1)
                ? t("Are you sure you want to delete the selected sync projects?")
                : t(
                    "Are you sure you want to delete selected sync projects? ({0} projects selected)",
                    this.state.selectedItems.length
                  )}
            </span>
          }
          onConfirm={() => this.deleteProjects(this.state.selectedItems)}
        />
      </span>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<ImageSync />, document.getElementById("image-sync"));
