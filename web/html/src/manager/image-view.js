'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, Button} = require("../components/buttons");
const Panel = require("../components/panel").Panel;
const Network = require("../utils/network");
const Utils = require("../utils/functions").Utils;
const {Table, Column, SearchField} = require("../components/table");
const Messages = require("../components/messages").Messages;
const DeleteDialog = require("../components/dialogs").DeleteDialog;
const ModalButton = require("../components/dialogs").ModalButton;
const TabContainer = require("../components/tab-container").TabContainer;
const ImageViewOverview = require("./image-view-overview").ImageViewOverview;
const ImageViewPatches = require("./image-view-patches").ImageViewPatches;
const ImageViewPackages = require("./image-view-packages").ImageViewPackages;
const DateTime = require("../components/datetime").DateTime;

const msgMap = {
  "not_found": t("Image cannot be found."),
  "delete_success": t("Image profile has been deleted.")
};

const hashUrlRegex = /^#\/([^\/]*)\/(\d*)$/;

function getHashId() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[2] : undefined;
}

function getHashTab() {
    const match = window.location.hash.match(hashUrlRegex);
    return match ? match[1] : undefined;
}

class ImageView extends React.Component {

  constructor(props) {
    super(props);
    ["reloadData", "handleBackAction", "handleDetailsAction", "deleteImage"]
        .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      images: []
    };

    this.updateView(getHashId(), getHashTab());
    window.addEventListener("popstate", () => {
        this.updateView(getHashId(), getHashTab());
    });
  }

  updateView(id, tab) {
    if(id)
        this.getImageInfoDetails(id, tab)
            .then(data => this.setState({selected: data}));
    else {
        this.getImageInfoList()
            .then(data => this.setState({selected: undefined, images: data}));
    }
    this.clearMessages();
  }

  reloadData() {
    if(this.state.selected) {
        this.getImageInfoDetails(this.state.selected.id, getHashTab())
            .then(data => this.setState({selected: data}));
    } else {
        this.getImageInfoList()
            .then(data => this.setState({images: data}));
    }
    this.clearMessages();
  }

  clearMessages() {
    this.setState({
        messages: undefined
    });
  }

  handleBackAction() {
    this.getImageInfoList().then(data => {
        this.setState({selected: undefined, images: data});
        const loc = window.location;
        history.pushState(null, null, loc.pathname + loc.search);
    });
  }

  handleDetailsAction(row) {
    this.getImageInfoDetails(row.id).then(data => {
        this.setState({selected: data})
        history.pushState(null, null, '#/' + (getHashTab() || 'overview') + '/' + row.id);
    });
  }

  getImageInfoList() {
    return Network.get("/rhn/manager/api/cm/images").promise;
  }

  getImageInfoDetails(id, tab) {
    let url;
    if(tab === "patches" || tab === "packages")
        url = "/rhn/manager/api/cm/images/" + tab + "/" + id;
    else
        url = "/rhn/manager/api/cm/images/" + id;

    return Network.get(url).promise;
  }

  deleteImage(row) {
    const id = row.id;
    return Network.del("/rhn/manager/api/cm/images/" + id).promise.then(data => {
        if (data.success) {
            this.setState({
                messages: <Messages items={data.messages.map(msg => {
                    return {severity: "success", text: msgMap[msg]};
                })}/>,
                images: this.state.images.filter(img => img.id !== id)
            });
        } else {
            this.setState({
                messages: <Messages items={state.messages.map(msg => {
                    return {severity: "error", text: msgMap[msg]};
                })}/>
            });
        }
    }).promise;
  }

  render() {
    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name={t("Refresh")} text action={this.reloadData} />
    </div>;

    return (
      <span>
        <Panel title={this.state.selected ? this.state.selected.name : t("Images")} icon={this.state.selected ? "fa-hdd-o" : "fa-list"} button={ panelButtons }>
          {this.state.messages}
          { this.state.selected ?
              <ImageViewDetails data={this.state.selected} onTabChange={() => this.updateView(getHashId(), getHashTab())} onCancel={this.handleBackAction}/>
          :
              <ImageViewList data={this.state.images} onSelect={this.handleDetailsAction} onDelete={this.deleteImage}/>
          }
        </Panel>

      </span>
    );
  }
}

class ImageViewList extends React.Component {
    constructor(props) {
        super(props);

        ["selectImage"].forEach(method => this[method] = this[method].bind(this));
        this.state = {};
    }

    searchData(row, criteria) {
        if (criteria) {
            return row.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) ||
                row.version.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());

        }
        return true;
    }

    isFiltered(criteria) {
        return criteria && criteria.length > 0;
    }

    selectImage(row) {
        this.setState({
            selected: row
        });
    }

    renderUpdatesIcon(row) {
        let icon;

        if(!row.patches || row.installedPackages === 0) {
            icon = <i className="fa fa-question-circle fa-1-5x" title="No information"/>
        } else if (row.patches.critical > 0) {
            icon = <i className="fa fa-exclamation-circle fa-1-5x text-danger" title={"Critical updates available"}/>
        } else if (row.patches.noncritical > 0) {
            icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={"Non-critical updates available"}/>
        } else if (row.packages > 0) {
            icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={"Package updates available"}/>
        } else {
            icon = <i className="fa fa-check-circle fa-1-5x text-success" title="Image is up to date"/>
        }

        return icon;
    }

    renderStatusIcon(row) {
        let icon;
        if(row.statusId === 0) {
            icon = <i className="fa fa-clock-o fa-1-5x" title="Queued"/>
        } else if(row.statusId === 1) {
            icon = <i className="fa fa-exchange fa-1-5x text-info" title="Building"/>
        } else if(row.statusId === 2) {
            icon = <i className="fa fa-check-circle-o fa-1-5x text-success" title="Built"/>
        } else if(row.statusId === 3) {
            icon = <i className="fa fa-times-circle-o fa-1-5x text-danger" title="Failed"/>
        } else {
            icon = <i className="fa fa-question-circle fa-1-5x" title="Unknown"/>
        }

        return icon;
    }

    render() {
        return (<div>
          <Table
              data={this.props.data}
              identifier={img => img.id}
              initialSortColumnKey="modified"
              initialSortDirection="-1"
              initialItemsPerPage={userPrefPageSize}
              searchField={
                  <SearchField filter={this.searchData} criteria={""} />
              }>
            <Column
              columnKey="name"
              comparator={Utils.sortByText}
              header={t('Name')}
              cell={ (row, criteria) => row.name }
            />
            <Column
              columnKey="version"
              header={t('Version')}
              cell={ (row, criteria) => row.version }
            />
            <Column
              columnKey="updates"
              header={t('Updates')}
              cell={ (row, criteria) => this.renderUpdatesIcon(row) }
            />
            <Column
              columnKey="patches"
              header={t('Patches')}
              comparator={(a, b, ck, sd) => Utils.sortByNumber(
                {patches: a.patches.critical + a.patches.noncritical},
                {patches: b.patches.critical + b.patches.noncritical},
                ck, sd)}
              cell={ (row, criteria) => row.patches ? row.patches.critical + row.patches.noncritical : '-' }
            />
            <Column
              columnKey="packages"
              header={t('Packages')}
              comparator={Utils.sortByNumber}
              cell={ (row, criteria) => row.patches ? row.packages : '-' }
            />
            <Column
              columnKey="status"
              header={t('Status')}
              cell={ (row, criteria) => this.renderStatusIcon(row) }
            />
            <Column
              columnKey="modified"
              header={t('Last Modified')}
              comparator={Utils.sortByDate}
              cell={ (row, criteria) => <DateTime time={row.modified}/> }
            />
            <Column
              width="10%"
              header={t('Actions')}
              cell={ (row, criteria) => {
                return (<div className="btn-group">
                  <Button
                    className="btn-default btn-sm"
                    title={t("Details")}
                    icon="fa-list"
                    handler={() => {this.props.onSelect(row)}}
                  />
                  { isAdmin &&
                  <ModalButton
                    className="btn-default btn-sm"
                    title={t("Delete")}
                    icon="fa-trash"
                    target="delete-modal"
                    item={row}
                    onClick={this.selectImage}
                  />
                  }
                  </div>
                )}}
              />
          </Table>
          <DeleteDialog id="delete-modal"
            title={t("Delete Image")}
            content={<span>{t("Are you sure you want to delete image")} <strong>{this.state.selected ? (this.state.selected.name + " (" + this.state.selected.version + ")") : ''}</strong>?</span>}
            item={this.state.selected}
            onConfirm={this.props.onDelete}
            onClosePopUp={() => this.selectImage(undefined)}
          />
        </div>
        );
    }
}

class ImageViewDetails extends React.Component {

    constructor(props) {
        super(props);
        ['onTabChange'].forEach(method => this[method] = this[method].bind(this));
    }

    getHashUrls(tabs) {
        const id = this.props.data.id;
        return tabs.map((t) => '#/' + t + '/' + id);
    }

    onTabChange(hash) {
        history.pushState(null, null, hash);
        if(this.props.onTabChange) {
            this.props.onTabChange(hash);
        }
    }

    render() {
        const data = this.props.data;

        return (
        <div>
            <TabContainer
                labels={[t('Overview'), t('Patches'), t('Packages')]}
                hashes={this.getHashUrls(['overview', 'patches', 'packages'])}
                initialActiveTabHash={window.location.hash}
                onTabHashChange={this.onTabChange}
                tabs={[
                    <ImageViewOverview data={data}/>,
                    <ImageViewPatches data={data}/>,
                    <ImageViewPackages data={data}/>
                    ]}
            />
            <Button
                text={t("Back")}
                icon="fa-chevron-left"
                title={t("Back")}
                className="btn-default"
                handler={this.props.onCancel}
            />
        </div>
        );
    }
}

ReactDOM.render(
  <ImageView />,
  document.getElementById('image-view')
)
