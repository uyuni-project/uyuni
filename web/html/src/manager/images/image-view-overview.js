/* eslint-disable */
'use strict';

const React = require("react");
const {LinkButton, Button} = require("components/buttons");
const DateTime = require("components/datetime").DateTime;
const {ModalButton} = require("components/dialog/ModalButton");
const {ModalLink} = require("components/dialog/ModalLink");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const PopUp = require("components/popup").PopUp;
const { Form } = require('components/input/Form');
const { DateTime: InputDateTime } = require('components/input/DateTime');
const Functions = require("utils/functions");
const { BootstrapPanel } = require('components/panels/BootstrapPanel');

/* global isAdmin, localTime, timezone */

const typeMap = {
  "dockerfile": "Container Image",
  "kiwi": "OS Image"
};

function StatusIcon(props) {
  const data = props.data;
  const action = props.action;

  let status;
  if(!action) {
    status = <span><i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>{t("No information")}</span>
  } else if(action.status === 0) {
    status = <span><i className="fa fa-clock-o fa-1-5x" title={t("Queued")}/><a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " is queued")}</a></span>
  } else if(action.status === 1) {
    status = <span><i className="fa fa-exchange fa-1-5x text-info" title={t("In progress")}/><a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " in progress")}</a></span>
  } else if(action.status === 2) {
    status = <span><i className="fa fa-check-circle fa-1-5x text-success" title={t("Successful")}/><a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " is successful")}</a></span>
  } else if(action.status === 3) {
    status = <span><i className="fa fa-times-circle-o fa-1-5x text-danger" title={t("Failed")}/><a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " has failed")}</a></span>
  } else {
    status = <span><i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>{t("No information")}</span>
  }

  return status;
}

function BuildStatus(props) {
  let status;
  if(props.data.external) {
    status = <span><i className="fa fa-minus-circle fa-1-5x text-muted" title={t("Built externally")}/>{t("Built externally")}</span>
  } else {
    status = <StatusIcon name="Build" data={props.data} action={props.data.buildAction}/>;
  }
  return (<ActionStatus name="Build" status={status} data={props.data} action={props.data.buildAction}/>);
}

function InspectStatus(props) {
  const status = <StatusIcon name="Inspect" data={props.data} action={props.data.inspectAction}/>;
  return (<ActionStatus name="Inspect" status={status} action={props.data.inspectAction}/>);
}

function ActionStatus(props) {
  const action = props.action;

  return (
    <div className="table-responsive">
      <table className="table">
        <tbody>
          <tr>
            <td width="20%">{t(props.name + " Status")}:</td>
            <td>{props.status}</td>
          </tr>
          { action && action.pickup_time &&
                        <tr>
                          <td>{t("Picked Up")}:</td>
                          <td><DateTime time={action.pickup_time}/></td>
                        </tr>
          }
          { action && action.completion_time &&
                        <tr>
                          <td>{t("Completed")}:</td>
                          <td><DateTime time={action.completion_time}/></td>
                        </tr>
          }
        </tbody>
      </table>
    </div>
  );
}

class ImageInfo extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      instancePopupContent: {}
    };
  }

  renderInstanceDetails(row) {
    let data;
    if(row.instances) {
      data = Object.keys(row.instances).map(i => <tr key={i}><td>{i}</td><td>{row.instances[i]}</td></tr>);
    }

    return (
      <table className="table">
        <thead>
          <tr>
            <th>{t("Cluster")}</th>
            <th>{t("Instances")}</th>
          </tr>
        </thead>
        <tbody>
          {data}
        </tbody>
      </table>
    );
  }

  renderInstances(data) {
    if (!this.props.gotRuntimeInfo) {
        return <i className="fa fa-circle-o-notch fa-spin fa-1-5x" title={t("Waiting for update ...")}/>;
    }

    let totalCount = 0;
    if(data.instances) {
      totalCount =  Object.keys(data.instances)
        .map(k => Number(data.instances[k]))
        .reduce((a,b) => a + b, 0);
    }

    return totalCount === 0 ? '-' :
      <span>{totalCount}&nbsp;&nbsp;
        <ModalLink
          target="instance-details-popup"
          title={t("View cluster summary")}
          icon="fa-external-link"
          item={data}
          onClick={(data) =>
              this.setState({
                instancePopupContent: {
                  name: data.name,
                  content: this.renderInstanceDetails(data)
                }
              })
          }
        />
      </span>;
  }

  renderRuntime(data) {
    if(!this.props.gotRuntimeInfo) {
      return <span><i className="fa fa-circle-o-notch fa-spin fa-1-5x" title={t("Waiting for update ...")}/></span>;
    }

    let elm = <span> - </span>;
    if (data.runtimeStatus === 1) {
      elm = <span><i className="fa fa-check-circle fa-1-5x text-success" title={t("All instances are consistent with SUSE Manager")}/><a href={"#/runtime/" + data.id}>{t("All instances are consistent with SUSE Manager")}</a></span>
    } else if (data.runtimeStatus === 2) {
      elm = <span><i className="fa fa-question-circle fa-1-5x" title={t("No information")}/><a href={"#/runtime/" + data.id}>{t("No information")}</a></span>
    } else if (data.runtimeStatus === 3) {
      elm = <span><i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Outdated instances found")}/><a href={"#/runtime/" + data.id}>{t("Outdated instances found")}</a></span>
    }
    return elm;
  }

  render() {
    const data = this.props.data;
    const isStoreEditable = data.store.type !== "os_image";

    return (
      <div className="table-responsive">
        <table className="table">
          <tbody>
            <tr>
              <td>Image Name:</td>
              <td>{data.name ? data.name : "-"}</td>
            </tr>
            <tr>
              <td>Type:</td>
              <td>{data.type ? typeMap[data.type] : "-"}</td>
            </tr>
            <tr>
              <td>Version:</td>
              <td>{data.version ? data.version : "-"}</td>
            </tr>
            <tr>
              <td>Revision:</td>
              <td>{data.revision > 0 ? data.revision : "-"}</td>
            </tr>
            { this.props.runtimeInfoEnabled ?
              <tr>
                <td>Runtime:</td>
                <td>{this.renderRuntime(data)}</td>
              </tr> : null }
            { this.props.runtimeInfoEnabled ?
              <tr>
                <td>Instances:</td>
                <td>{this.renderInstances(data)}</td>
              </tr> : null }
            <tr>
              <td>Checksum:</td>
              <td>{data.checksum ? data.checksum : "-"}</td>
            </tr>
            { !data.external &&
            <tr>
              <td>Profile:</td>
              { data.profile ?
                <td>{data.profile.label}{ isAdmin && <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + data.profile.id + "?url_bounce=" + encodeURIComponent("/rhn/manager/cm/images#/overview/" + data.id)} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit profile")}/>}</td>
                :<td>-</td>
              }
            </tr>
            }
            <tr>
              <td>Store:</td>
              { data.store ?
                <td>{data.store.label}{ isAdmin && isStoreEditable && <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imagestores/edit/" + data.store.id + "?url_bounce=" + encodeURIComponent("/rhn/manager/cm/images#/overview/" + data.id)} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit store")}/>}</td>
                :<td>-</td>
              }
            </tr>
            <tr>
              <td>Build Host:</td>
              { data.buildServer ?
                <td><a href={"/rhn/systems/details/Overview.do?sid=" + data.buildServer.id}>{data.buildServer.name}</a></td>
                :<td>-</td>
              }
            </tr>
            <tr>
              <td>Software Channels:</td>
              { data.channels && data.channels.base ?
                <td>
                  <ul className="list-unstyled">
                    <li><a href={"/rhn/channels/ChannelDetail.do?cid=" + data.channels.base.id} title={data.channels.base.name}>{data.channels.base.name}</a></li>
                    <li>
                      <ul>{data.channels.children.map(ch => <li key={ch.id}><a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>{ch.name}</a></li>)}</ul>
                    </li>
                  </ul>
                </td>
                :<td>-</td>
              }
            </tr>
            <tr>
              <td>Installed Products:</td>
              { data.installedProducts ?
                <td>
                  <ul className="list-group">
                    <li className="list-group-item">{data.installedProducts.base}</li>
                    {data.installedProducts.addons.map(addon =>
                      <li key={addon} className="list-group-item">{addon}</li>
                    )}
                  </ul>
                </td>
                :<td>-</td>
              }
            </tr>
          </tbody>
        </table>
        <PopUp
          id="instance-details-popup"
          title={t("Instance Details for '{0}'", this.state.instancePopupContent.name)}
          content={this.state.instancePopupContent.content}
        />
      </div>
    );
  }
}

class ImageCustomInfo extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    const data = this.props.data.customData;
    return (
      <div className="table-responsive">
        <table className="table">
          <tbody>
            { Object.entries(data).map(e => <tr key={e[0]}><td>{e[0]}:</td><td>{e[1] || "-"}</td></tr>)}
          </tbody>
        </table>
      </div>
    );
  }
}

class ImageViewOverview extends React.Component {
  constructor(props) {
    super(props);
  }

  renderStatus(row) {
    let status;

    if(!row.patches || row.installedPackages === 0) {
      status = <span><i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>{t("No information")} </span>
    } else if (row.patches.security > 0) {
      status = <span><i className="fa fa-exclamation-circle fa-1-5x text-danger" title={t("Critical updates available")}/>{t("Critical updates available")} </span>
    } else if (row.patches.bugs + row.patches.enhancement > 0) {
      status = <span><i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Non-critical updates available")}/>{t("Non-critical updates available")} </span>
    } else if (row.packages > 0) {
      status = <span><i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Package updates available")}/>{t("Package updates available")} </span>
    } else {
      status = <span><i className="fa fa-check-circle fa-1-5x text-success" title={t("Image is up to date")}/>{t("Image is up to date")} </span>
    }

    const counts = <span>
      { row.patches.security > 0 &&
                <span>
                  <strong>Security patches: </strong>{row.patches.security}
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </span>
      }
      { row.patches.bug > 0 &&
                <span>
                  <strong>Bug patches: </strong>{row.patches.bug}
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </span>
      }
      { row.patches.enhancement > 0 &&
                <span>
                  <strong>Enhancement patches: </strong>{row.patches.enhancement}
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </span>
      }
      { row.packages > 0 &&
                <span>
                  <strong>Package updates: </strong>{row.packages}
                    &nbsp;&nbsp;&nbsp;&nbsp;
                </span>
      }
    </span>;

    return <span>{status}&nbsp;&nbsp;&nbsp;&nbsp;{counts}</span>;
  }

  hasUpdates() {
    const data = this.props.data;
    return (data.patches && data.patches > 0) || (data.packages && data.packages > 0);
  }

  hasBuilt() {
    const data = this.props.data;
    return data.external || (data.buildAction && data.buildAction.status === 2);
  }

  canInspect() {
    const data = this.props.data;
    return this.hasBuilt() && data.type !== 'kiwi';
  }

  hasInspected() {
    return this.props.data.inspectAction && this.props.data.inspectAction.status === 2;
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
              <div className="auto-overflow">
                <ImageInfo data={data} runtimeInfoEnabled={this.props.runtimeInfoEnabled}
                    gotRuntimeInfo={this.props.gotRuntimeInfo}/>
              </div>
            </BootstrapPanel>
          </div>
          <div className="col-md-6">
            <BootstrapPanel title={t("Build Status")}>
              <div>
                <h3>{t("Build")}</h3>
                <BuildStatus data={data}/>
              </div>

              { this.hasBuilt() &&
                        <div>
                          <h3>{t("Inspect")}</h3>
                          <InspectStatus data={data}/>
                        </div>
              }
              { isAdmin && data.buildServer &&
                        <div className="btn-group pull-right">
                          { !data.external &&
                            <ModalButton
                              className="btn-default btn-xs"
                              text={t("Rebuild")}
                              title={t("Reschedule the build")}
                              icon="fa-cogs"
                              target="build-modal"
                            />
                          }
                          { this.canInspect() &&
                            <ModalButton
                              className="btn-default btn-xs"
                              text={t("Reinspect")}
                              title={t("Reschedule the inspect")}
                              icon="fa-search"
                              target="inspect-modal"
                            />
                          }
                        </div>
              }
            </BootstrapPanel>
          </div>
        </div>
        {data.customData && Object.keys(data.customData).length > 0 &&
                <div className="row-0">
                  <div className="col-md-12">
                    <BootstrapPanel title={t("Custom Image Information")}>
                      <ImageCustomInfo data={data}/>
                    </BootstrapPanel>
                  </div>
                </div>
        }
        { data.buildServer &&
            <div>
              <BuildDialog data={data} onBuild={this.props.onBuild}/>
              <InspectDialog data={data} onInspect={this.props.onInspect}/>
            </div>
        }
        <DeleteDialog id="delete-modal"
            title={t("Delete Image")}
            content={<span>{t("Are you sure you want to delete image")} <strong>{data ? (data.name + " (" + data.version + ")") : ''}</strong>?</span>}
            item={data}
            onConfirm={this.props.onDelete}
        />

      </div>
    );
  }
}

class BuildDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      model: {
        earliest: Functions.Utils.dateWithTimezone(localTime)
      }
    };
  }

  onChange(model) {
    this.setState({
      model: model
    });
  }

  render() {
    const buttons = <div>
      <Button
        className="btn-success"
        text={t("Build")}
        title={t("Schedule build")}
        icon="fa-cogs"
        handler={() => {
          if(this.props.onBuild) this.props.onBuild(this.props.data.profile.id, this.props.data.version, this.props.data.buildServer.id, this.state.model.earliest);
          $("#build-modal").modal('hide');
        }}
      />
      <Button
        className="btn-default"
        text={t("Cancel")}
        title={t("Cancel")}
        icon="fa-close"
        handler={() => {
          $("#build-modal").modal('hide');
        }}
      />
    </div>;

    const form =
            <div className="row clearfix">
              <p>Schedule a rebuild for image: <strong>{this.props.data.name + ":" + this.props.data.version}</strong> on <strong>{this.props.data.buildServer.name}</strong></p>
              <Form model={this.state.model} className="image-build-form"
                onChange={this.onChange.bind(this)} divClass="col-md-12">
                <InputDateTime name="earliest" required timezone={timezone} />
              </Form>
            </div>

    return (
      <PopUp
        id="build-modal"
        content={form}
        title={t("Rebuild Image")}
        footer={buttons}
      />
    );
  }
}

class InspectDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      model: {
        earliest: Functions.Utils.dateWithTimezone(localTime)
      }
    };
  }

  onChange(model) {
    this.setState({
      model: model
    });
  }

  render() {
    const buttons = <div>
      <Button
        className="btn-success"
        text={t("Inspect")}
        title={t("Schedule inspect")}
        icon="fa-search"
        handler={() => {
          if(this.props.onInspect) this.props.onInspect(this.props.data.id, this.state.model.earliest);
          $("#inspect-modal").modal('hide');
        }}
      />
      <Button
        className="btn-default"
        text={t("Cancel")}
        title={t("Cancel")}
        icon="fa-close"
        handler={() => {
          $("#inspect-modal").modal('hide');
        }}
      />
    </div>;

    const form =
            <div className="row clearfix">
              <p>Schedule an inspect for image: <strong>{this.props.data.name + ":" + this.props.data.version}</strong> on <strong>{this.props.data.buildServer.name}</strong></p>
              <Form model={this.state.model} className="image-inspect-form"
                onChange={this.onChange.bind(this)} divClass="col-md-12">
                <InputDateTime name="earliest" required timezone={timezone} />
              </Form>
            </div>

    return (
      <PopUp
        id="inspect-modal"
        content={form}
        title={t("Reinspect Image")}
        footer={buttons}
      />
    );
  }
}

module.exports = {
  ImageViewOverview: ImageViewOverview
}
