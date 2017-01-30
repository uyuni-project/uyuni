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
    ["reloadData", "deleteProfile"]
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
  }

  deleteProfile(id) {
    return Network.del("/rhn/manager/api/cm/imageprofiles/" + id).promise.then(data => {
        if (data.success) {
            this.setState({
                messages: <Messages items={data.messages.map(msg => {
                    return {severity: "success", text: msgMap[msg]};
                })}/>,
                imageprofiles: this.state.imageprofiles.filter(profile => profile.profile_id !== id)
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
              identifier={profile => profile.profile_id}
              initialSortColumnKey="profile_id"
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
              columnKey="image_type"
              width="40%"
              comparator={Utils.sortByText}
              header={t('Build Type')}
              cell={ (row, criteria) => typeMap[row.image_type] }
            />
            { isAdmin &&
              <Column
                width="10%"
                header={t('Actions')}
                cell={ (row, criteria) => {
                  return <div className="btn-group">
                      <LinkButton
                          className="btn-default btn-sm"
                          title={t("Edit")}
                          icon="fa-edit"
                          href={"/rhn/manager/cm/imageprofiles/edit/" + row.profile_id}
                      />
                      <AsyncButton
                          title={t("Delete")}
                          defaultType="btn-default btn-sm"
                          icon="trash"
                          action={() => this.deleteProfile(row.profile_id)}
                      />
                  </div>;
                }}
              />
            }
          </Table>
        </Panel>
      </span>
    );
  }
}

ReactDOM.render(
  <ImageProfiles />,
  document.getElementById('image-profiles')
)
