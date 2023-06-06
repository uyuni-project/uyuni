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

// See java/code/src/com/suse/manager/webui/templates/content_management/list-profiles.jade
declare global {
  interface Window {
    isAdmin?: any;
  }
}

const typeMap = {
  dockerfile: t("Dockerfile"),
  kiwi: t("Kiwi"),
};

const msgMap = {
  not_found: t("Image profile cannot be found."),
  delete_success: t("Image profile has been deleted."),
  delete_success_p: t("Image profiles have been deleted."),
};

type Props = {};

type State = {
  messages: any;
  imageprofiles: any;
  selectedItems: any;
  selected?: any;
};

class ImageProfiles extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      imageprofiles: [],
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
    Network.get("/rhn/manager/api/cm/imageprofiles").then((data) => {
      this.setState({
        imageprofiles: data,
      });
    });
    this.clearMessages();
  };

  handleSelectItems = (items) => {
    this.setState({
      selectedItems: items,
    });
  };

  selectProfile = (row) => {
    this.setState({
      selected: row,
    });
  };

  clearMessages() {
    this.setState({
      messages: undefined,
    });
  }

  deleteProfiles = (idList) => {
    return Network.post("/rhn/manager/api/cm/imageprofiles/delete", idList).then((data) => {
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
          imageprofiles: this.state.imageprofiles.filter((profile) => !idList.includes(profile.profileId)),
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
        {window.isAdmin && (
          <LinkButton
            id="create"
            icon="fa-plus"
            className="btn-default"
            title={t("Create")}
            text={t("Create")}
            href="/rhn/manager/cm/imageprofiles/create"
          />
        )}
        <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
      </div>
    );

    return (
      <span>
        <TopPanel
          title={t("Image Profiles")}
          icon="spacewalk-icon-manage-configuration-files"
          helpUrl="reference/images/images-profiles.html"
          button={panelButtons}
        >
          {this.state.messages}
          <Table
            data={this.state.imageprofiles}
            identifier={(profile) => profile.profileId}
            initialSortColumnKey="profileId"
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
              columnKey="imageType"
              width="35%"
              comparator={Utils.sortByText}
              header={t("Build Type")}
              cell={(row) => typeMap[row.imageType]}
            />
            {window.isAdmin && (
              <Column
                width="15%"
                header={t("Actions")}
                columnClass="text-right"
                headerClass="text-right"
                cell={(row) => {
                  return (
                    <div className="btn-group">
                      <LinkButton
                        className="btn-default btn-sm"
                        title={t("Build")}
                        icon="fa-cogs"
                        href={"/rhn/manager/cm/build?profile=" + row.profileId}
                      />
                      <LinkButton
                        className="btn-default btn-sm"
                        title={t("Edit")}
                        icon="fa-edit"
                        href={"/rhn/manager/cm/imageprofiles/edit/" + row.profileId}
                      />
                      <ModalButton
                        className="btn-default btn-sm"
                        title={t("Delete")}
                        icon="fa-trash"
                        target="delete-modal"
                        item={row}
                        onClick={this.selectProfile}
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
          title={t("Delete Profile")}
          content={
            <span>
              {t("Are you sure you want to delete profile")}{" "}
              <strong>{this.state.selected ? this.state.selected.label : ""}</strong>?
            </span>
          }
          item={this.state.selected}
          onConfirm={(item) => this.deleteProfiles([item.profileId])}
          onClosePopUp={() => this.selectProfile(undefined)}
        />
        <DeleteDialog
          id="delete-selected-modal"
          title={t("Delete Selected Profile(s)")}
          content={
            <span>
              {DEPRECATED_unsafeEquals(this.state.selectedItems.length, 1)
                ? t("Are you sure you want to delete the selected profile?")
                : t(
                    "Are you sure you want to delete selected profiles? ({0} profiles selected)",
                    this.state.selectedItems.length
                  )}
            </span>
          }
          onConfirm={() => this.deleteProfiles(this.state.selectedItems)}
        />
      </span>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<ImageProfiles />, document.getElementById("image-profiles"));
