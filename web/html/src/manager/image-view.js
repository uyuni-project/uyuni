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
const DateTime = require("../components/datetime").DateTime;

const msgMap = {
  "not_found": t("Image cannot be found."),
  "delete_success": t("Image profile has been deleted.")
};

class ImageView extends React.Component {

  constructor(props) {
    super(props);
    ["reloadData", "selectImage", "deleteImage"]
        .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      images: []
    };

    const imageInfoId = window.location.hash.substring(1);
    if(imageInfoId)
        this.selectImage(imageInfoId);
    else
        this.reloadData();
  }

  componentWillMount() {
    window.addEventListener("popstate", () => {
        const imageInfoId = window.location.hash.substring(1);
        if(imageInfoId)
            this.selectImage(imageInfoId);
        else
            this.reloadData();
    });
  }

  reloadData() {
    if(this.state.selected) {
        this.getImageInfoDetails(this.state.selected.id);
    } else {
        this.getImageInfoList();
    }
    this.clearMessages();
  }

  clearMessages() {
    this.setState({
        messages: undefined
    });
  }

  selectImage(row) {
    const id = row instanceof Object ? row.id : row;

    if(!id) {
        this.setState({
            selected: undefined
        }, () => {
            const loc = window.location;
            history.pushState(null, null, loc.pathname + loc.search);
            this.reloadData();
        });
    } else {
        this.getImageInfoDetails(id);
        history.pushState(null, null, "#" + id);
    }
  }

  getImageInfoList() {
    Network.get("/rhn/manager/api/cm/images").promise.then(data => {
        this.setState({
            images: data
        });
    });
  }

  getImageInfoDetails(id) {
    Network.get("/rhn/manager/api/cm/images/" + id).promise.then(data => {
        this.setState({
            selected: data
        });
    });
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
              <ImageViewDetails data={this.state.selected} onCancel={() => {this.selectImage(undefined)}}/>
          :
              <ImageViewList data={this.state.images} onSelect={this.selectImage} onDelete={this.deleteImage}/>
          }
        </Panel>

      </span>
    );
  }
}

function BootstrapPanel(props) {
    return (
        <div className="panel panel-default">
            <div className="panel-heading">
                <h4>{props.title}</h4>
            </div>
            <div className="panel-body">
                {props.children}
            </div>
        </div>
    );
}

function BuildStatus(props) {
    const data = props.data;

    let status;
    if(!data.action) {
        status = [<i className="fa fa-question-circle fa-1-5x" title="Unknown"/>,"No information"]
    } else if(data.action.status === 0) {
        status = [<i className="fa fa-clock-o fa-1-5x" title="Queued"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + data.action.id}>{t("Build is queued")}</a>]
    } else if(data.action.status === 1) {
        status = [<i className="fa fa-exchange fa-1-5x text-info" title="Building"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + data.action.id}>{t("Build in progress")}</a>]
    } else if(data.action.status === 2) {
        status = [<i className="fa fa-check-circle-o fa-1-5x text-success" title="Built"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + data.action.id}>{t("Build is successful")}</a>]
    } else if(data.action.status === 3) {
        status = [<i className="fa fa-times-circle-o fa-1-5x text-danger" title="Failed"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + data.action.id}>{t("Build has failed")}</a>]
    } else {
        status = [<i className="fa fa-question-circle fa-1-5x" title="Unknown"/>,"No information"]
    }

    return (
        <div className="table-responsive">
            <table className="table">
                <tbody>
                    <tr>
                        <td>Build Status:</td>
                        <td>{status}</td>
                    </tr>
                    { data.action && data.action.pickup_time &&
                        <tr>
                            <td>Picked Up:</td>
                            <td><DateTime time={data.action.pickup_time}/></td>
                        </tr>
                    }
                    { data.action && data.action.completion_time &&
                        <tr>
                            <td>Completed:</td>
                            <td><DateTime time={data.action.completion_time}/></td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    );
}

function ImageInfo(props) {
    const data = props.data;
    return (
        <div className="table-responsive">
            <table className="table">
                <tbody>
                    <tr>
                        <td>Image Name:</td>
                        <td>{data.name}</td>
                    </tr>
                    <tr>
                        <td>Version:</td>
                        <td>{data.version}</td>
                    </tr>
                    <tr>
                        <td>Checksum:</td>
                        <td>{data.checksum ? data.checksum : "-"}</td>
                    </tr>
                    <tr>
                        <td>Profile:</td>
                        { data.profile ?
                            <td>{data.profile.label}<LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + data.profile.id} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit profile")}/></td>
                            :<td>-</td>
                        }
                    </tr>
                    <tr>
                        <td>Store:</td>
                        <td>{data.store.label}<LinkButton icon="fa-edit" href={"/rhn/manager/cm/imagestores/edit/" + data.store.id} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit store")}/></td>
                    </tr>
                    <tr>
                        <td>Build Host:</td>
                        { data.buildServer ?
                            <td><a href={"/rhn/systems/details/Overview.do?sid=" + data.buildServer.id}>{data.buildServer.name}</a></td>
                            :<td></td>
                        }
                    </tr>
                    <tr>
                        <td>Software Channels:</td>
                        { data.channels && data.channels.base ?
                            <td>
                                <ul className="list-unstyled">
                                    <li><a href={"/rhn/channels/ChannelDetail.do?cid=" + data.channels.base} title={data.channels.base.name}>{data.channels.base.name}</a></li>
                                    <li>
                                        <ul>{data.channels.children.map(ch => <li><a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>{ch.name}</a></li>)}</ul>
                                    </li>
                                </ul>
                            </td>
                            :<td>-</td>
                        }
                    </tr>
                </tbody>
            </table>
        </div>
    );
}

class ImageViewDetails extends React.Component {
    constructor(props) {
        super(props);
    }

    renderStatus(row) {
        let status, statusText;

        if(!row.patches) {
            status = [<i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>,t("No information ")];
        } else if (row.patches.security > 0) {
            status = [<i className="fa fa-exclamation-circle fa-1-5x text-danger" title={t("Critical updates available")}/>,t("Critical updates available ")];
        } else if (row.patches.bugs + row.patches.enhancement > 0) {
            status = [<i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Non-critical updates available")}/>,t("Non-critical updates available ")];
        } else if (row.packages > 0) {
            status = [<i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Package updates available")}/>,t("Package updates available ")];
        } else {
            status = [<i className="fa fa-check-circle fa-1-5x text-success" title={t("Image is up to date")}/>,t("Image is up to date ")];
        }

        const counts = <span>
            { row.patches.security > 0 &&
                [<strong> Security patches: </strong>,row.patches.security]
            }
            { row.patches.bug > 0 &&
                [<strong> Bug patches: </strong>,row.patches.bug]
            }
            { row.patches.security > 0 &&
                [<strong> Enhancement patches: </strong>,row.patches.enhancement]
            }
            { row.packages > 0 &&
                [<strong> Package updates: </strong>,row.packages]
            }
        </span>;

        return <span>{status} {counts}</span>;
    }

    hasUpdates() {
        const data = this.props.data;
        return (data.patches && data.patches > 0) || (data.packages && data.packages > 0);
    }

    hasBuilt() {
        return this.props.data.action && this.props.data.action.status === 2;
    }

    render() {
        const data = this.props.data;
        return (
            <div>
            { this.hasBuilt() &&
                <BootstrapPanel title={t("Image Status")}>
                    {this.renderStatus(data)}
                </BootstrapPanel>
            }
            <div className="row-0">
                <div className="col-md-6">
                    <BootstrapPanel title={t("Image Info")}>
                        <ImageInfo data={data}/>
                    </BootstrapPanel>
                </div>
                <div className="col-md-6">
                    <BootstrapPanel title={t("Build Status")}>
                        <BuildStatus data={data}/>
                    </BootstrapPanel>
                </div>
            </div>
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

        if(!row.patches) {
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
              initialSortColumnKey="id"
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
              comparator={Utils.sortByText}
              cell={ (row, criteria) => this.renderUpdatesIcon(row) }
            />
            <Column
              columnKey="patches"
              header={t('Patches')}
              comparator={Utils.sortById}
              cell={ (row, criteria) => row.patches ? row.patches.critical + row.patches.noncritical : '-' }
            />
            <Column
              columnKey="packages"
              header={t('Packages')}
              comparator={Utils.sortById}
              cell={ (row, criteria) => row.patches ? row.packages : '-' }
            />
            <Column
              columnKey="status"
              header={t('Status')}
              comparator={Utils.sortByText}
              cell={ (row, criteria) => this.renderStatusIcon(row) }
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

ReactDOM.render(
  <ImageView />,
  document.getElementById('image-view')
)
