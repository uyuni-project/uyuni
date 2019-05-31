/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, LinkButton} = require("components/buttons");
const { TopPanel } = require('components/panels/TopPanel');
const Network = require("utils/network");
const Functions = require("utils/functions");
const Utils = Functions.Utils;
const {Table, Column, SearchField} = require("components/table");
const Messages = require("components/messages").Messages;
const DeleteDialog = require("components/dialog/DeleteDialog").DeleteDialog;
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const SpaRenderer  = require("core/spa/spa-renderer").default;

/* global isAdmin */

const typeMap = {
  "dockerfile": t("Dockerfile"),
  "kiwi": t("Kiwi")
};

const msgMap = {
  "not_found": t("Image profile cannot be found."),
  "delete_success": t("Image profile has been deleted."),
  "delete_success_p": t("Image profiles have been deleted.")
};

class ImageProfiles extends React.Component {

  constructor(props) {
    super(props);
    ["reloadData", "handleSelectItems", "selectProfile", "deleteProfiles"]
      .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      imageprofiles: [],
      selectedItems: []
    };
    this.reloadData();
  }

  searchData(row, criteria) {
    if (criteria) {
      return row.label.toLocaleLowerCase().includes(criteria.toLocaleLowerCase())

    }
    return true;
  }

  reloadData() {
    Network.get("/rhn/manager/api/cm/imageprofiles").promise.then(data => {
      this.setState({
        imageprofiles: data
      });
    });
    this.clearMessages();
  }

  handleSelectItems(items) {
    this.setState({
      selectedItems: items
    });
  }

  selectProfile(row) {
    this.setState({
      selected: row
    });
  }

  clearMessages() {
    this.setState({
      messages: undefined
    });
  }

  deleteProfiles(idList) {
    return Network.post("/rhn/manager/api/cm/imageprofiles/delete",
      JSON.stringify(idList), "application/json").promise.then(data => {
      if (data.success) {
        this.setState({
          messages: <Messages items={[{severity: "success", text: msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]}]}/>,
          imageprofiles: this.state.imageprofiles.filter(profile => !idList.includes(profile.profileId)),
          selectedItems: this.state.selectedItems.filter(item => !idList.includes(item))
        });
      } else {
        this.setState({
          messages: <Messages items={data.messages.map(msg => {
            return {severity: "error", text: msgMap[msg]};
          })}/>
        });
      }
    }).promise;
  }

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  render() {
    const panelButtons = <div className="pull-right btn-group">
      { isAdmin && this.state.selectedItems.length > 0 &&
          <ModalButton id="delete-selected" icon="fa-trash" className="btn-default" text={t("Delete")}
            title={t("Delete selected")} target="delete-selected-modal"/>
      }
      <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
      { isAdmin &&
        <LinkButton id="create" icon="fa-plus" className="btn-default" title={t("Create")} text={t("Create")} href="/rhn/manager/cm/imageprofiles/create" />
      }
    </div>;

    return (
      <span>
        <TopPanel title={t("Image Profiles")} icon="spacewalk-icon-manage-configuration-files" helpUrl="/docs/reference/images/images-profiles.html" button={ panelButtons }>
          {this.state.messages}
          <Table
            data={this.state.imageprofiles}
            identifier={profile => profile.profileId}
            initialSortColumnKey="profileId"
            initialItemsPerPage={userPrefPageSize}
            searchField={
              <SearchField filter={this.searchData} criteria={""} />
            }
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}>
            <Column
              columnKey="label"
              width="50%"
              comparator={Utils.sortByText}
              header={t('Label')}
              cell={ (row) => row.label }
            />
            <Column
              columnKey="imageType"
              width="35%"
              comparator={Utils.sortByText}
              header={t('Build Type')}
              cell={ (row) => typeMap[row.imageType] }
            />
            { isAdmin &&
              <Column
                width="15%"
                header={t('Actions')}
                columnClass="text-right"
                headerClass="text-right"
                cell={ (row) => {
                  return <div className="btn-group">
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
                  </div>;
                }}
              />
            }
          </Table>
        </TopPanel>

        <DeleteDialog id="delete-modal"
          title={t("Delete Profile")}
          content={<span>{t("Are you sure you want to delete profile")} <strong>{this.state.selected ? this.state.selected.label : ''}</strong>?</span>}
          item={this.state.selected}
          onConfirm={(item) => this.deleteProfiles([item.profileId])}
          onClosePopUp={() => this.selectProfile(undefined)}
        />
        <DeleteDialog id="delete-selected-modal"
          title={t("Delete Selected Profile(s)")}
          content={
            <span>
              {this.state.selectedItems.length == 1 ? t("Are you sure you want to delete the selected profile?") : t("Are you sure you want to delete selected profiles? ({0} profiles selected)", this.state.selectedItems.length)}
            </span>
          }
          onConfirm={() => this.deleteProfiles(this.state.selectedItems)}
        />
      </span>
    );
  }
}

SpaRenderer.renderNavigationReact(
  <ImageProfiles />,
  document.getElementById('image-profiles')
)
