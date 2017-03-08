'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, LinkButton, Button} = require("../components/buttons");
const Panel = require("../components/panel").Panel;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Utils = Functions.Utils;
const {Table, Column, SearchField} = require("../components/table");
const Messages = require("../components/messages").Messages;
const DeleteDialog = require("../components/dialogs").DeleteDialog;
const ModalButton = require("../components/dialogs").ModalButton;

const typeMap = {
  "dockerfile": t("Dockerfile")
};

const msgMap = {
  "not_found": t("Image profile cannot be found."),
  "delete_success": t("Image profile has been deleted.")
};

class ImageProfiles extends React.Component {

  constructor(props) {
    super();
    ["reloadData", "selectProfile", "deleteProfile"]
        .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      imageprofiles: []
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

  deleteProfile(row) {
    const id = row.profileId;
    return Network.del("/rhn/manager/api/cm/imageprofiles/" + id).promise.then(data => {
        if (data.success) {
            this.setState({
                messages: <Messages items={data.messages.map(msg => {
                    return {severity: "success", text: msgMap[msg]};
                })}/>,
                imageprofiles: this.state.imageprofiles.filter(profile => profile.profileId !== id)
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
      <AsyncButton id="reload" icon="refresh" name={t("Refresh")} text action={this.reloadData} />
      { isAdmin &&
        <LinkButton id="create" icon="fa-plus" className="btn-default" title={t("Create")} text={t("Create")} href="/rhn/manager/cm/imageprofiles/create" />
      }
    </div>;

    return (
      <span>
        <Panel title="Image Profiles" icon="fa-list" button={ panelButtons }>
          {this.state.messages}
          <Table
              data={this.state.imageprofiles}
              identifier={profile => profile.profileId}
              initialSortColumnKey="profileId"
              initialItemsPerPage={userPrefPageSize}
              searchField={
                  <SearchField filter={this.searchData} criteria={""} />
              }>
            <Column
              columnKey="label"
              width="50%"
              comparator={Utils.sortByText}
              header={t('Label')}
              cell={ (row, criteria) => row.label }
            />
            <Column
              columnKey="imageType"
              width="35%"
              comparator={Utils.sortByText}
              header={t('Build Type')}
              cell={ (row, criteria) => typeMap[row.imageType] }
            />
            { isAdmin &&
              <Column
                width="15%"
                header={t('Actions')}
                columnClass="text-right"
                headerClass="text-right"
                cell={ (row, criteria) => {
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
        </Panel>

        <DeleteDialog id="delete-modal"
          title={t("Delete Profile")}
          content={<span>{t("Are you sure you want to delete profile")} <strong>{this.state.selected ? this.state.selected.label : ''}</strong>?</span>}
          item={this.state.selected}
          onConfirm={this.deleteProfile}
          onClosePopUp={() => this.selectProfile(undefined)}
        />
      </span>
    );
  }
}

ReactDOM.render(
  <ImageProfiles />,
  document.getElementById('image-profiles')
)
