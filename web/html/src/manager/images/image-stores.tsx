import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton, LinkButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/templates/content_management/list-stores.jade
declare global {
  interface Window {
    isAdmin?: any;
  }
}

const messageMap = {
  not_found: t("Image store cannot be found."),
  delete_success: t("Image store has been deleted."),
  delete_success_p: t("Image stores have been deleted."),
};

const typeMap = {
  registry: "Registry",
  os_image: "OS Image",
};

type Props = {};

type State = {
  messages: any;
  imagestores: any;
  selectedItems: any;
  selected?: any;
};

class ImageStores extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      imagestores: [],
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
    Network.get("/rhn/manager/api/cm/imagestores").then((data) => {
      this.setState({
        imagestores: data,
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

  selectStore = (row) => {
    this.setState({
      selected: row,
    });
  };

  deleteStores = (idList) => {
    return Network.post("/rhn/manager/api/cm/imagestores/delete", idList).then((data) => {
      if (data.success) {
        this.setState({
          messages: (
            <Messages
              items={[
                {
                  severity: "success",
                  text: messageMap[idList.length > 1 ? "delete_success_p" : "delete_success"],
                },
              ]}
            />
          ),
          imagestores: this.state.imagestores.filter((store) => !idList.includes(store.id)),
          selectedItems: this.state.selectedItems.filter((item) => !idList.includes(item)),
        });
      } else {
        this.setState({
          messages: (
            <Messages
              items={data.messages.map((msg) => {
                return { severity: "error", text: messageMap[msg] };
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
        {window.isAdmin && (
          <LinkButton
            id="create"
            icon="fa-plus"
            className="btn-default"
            title={t("Create")}
            text={t("Create")}
            href="/rhn/manager/cm/imagestores/create"
          />
        )}
        <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
      </div>
    );

    return (
      <span>
        <TopPanel
          title={t("Image Stores")}
          icon="fa-list"
          helpUrl="reference/images/images-stores.html"
          button={panelButtons}
        >
          {this.state.messages}
          <Table
            data={this.state.imagestores}
            identifier={(imagestore) => imagestore.id}
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
              columnKey="type"
              width="35%"
              comparator={Utils.sortByText}
              header={t("Type")}
              cell={(row) => typeMap[row.type]}
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
                        title={t("Edit")}
                        icon="fa-edit"
                        href={"/rhn/manager/cm/imagestores/edit/" + row.id}
                      />
                      <ModalButton
                        className="btn-default btn-sm"
                        title={t("Delete")}
                        icon="fa-trash"
                        target="delete-modal"
                        item={row}
                        onClick={this.selectStore}
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
          title={t("Delete Store")}
          content={
            <span>
              {t("Are you sure you want to delete store")}{" "}
              <strong>{this.state.selected ? this.state.selected.label : ""}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={(item) => this.deleteStores([item.id])}
          onClosePopUp={() => this.selectStore(undefined)}
        />
        <DeleteDialog
          id="delete-selected-modal"
          title={t("Delete Selected Store(s)")}
          content={
            <span>
              {DEPRECATED_unsafeEquals(this.state.selectedItems.length, 1)
                ? t("Are you sure you want to delete the selected store?")
                : t("Are you sure you want to delete selected stores? ({count} stores selected)", {
                    count: this.state.selectedItems.length,
                  })}
            </span>
          }
          onConfirm={() => this.deleteStores(this.state.selectedItems)}
        />
      </span>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<ImageStores />, document.getElementById("image-stores"));
