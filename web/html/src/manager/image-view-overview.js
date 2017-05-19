'use strict';

const React = require("react");
const {LinkButton, Button} = require("../components/buttons");
const DateTime = require("../components/datetime").DateTime;
const ModalButton = require("../components/dialogs").ModalButton;
const PopUp = require("../components/popup").PopUp;
const Input = require("../components/input");
const Functions = require("../utils/functions");

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
    return (<ActionStatus name="Build" data={props.data} action={props.data.buildAction}/>);
}

function InspectStatus(props) {
    return (<ActionStatus name="Inspect" data={props.data} action={props.data.inspectAction}/>);
}

function ActionStatus(props) {
    const data = props.data;
    const action = props.action;

    let status;
    if(!action) {
        status = [<i className="fa fa-question-circle fa-1-5x" title="Unknown"/>,"No information"]
    } else if(action.status === 0) {
        status = [<i className="fa fa-clock-o fa-1-5x" title="Queued"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " is queued")}</a>]
    } else if(action.status === 1) {
        status = [<i className="fa fa-exchange fa-1-5x text-info" title="In progress"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " in progress")}</a>]
    } else if(action.status === 2) {
        status = [<i className="fa fa-check-circle-o fa-1-5x text-success" title="Successful"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " is successful")}</a>]
    } else if(action.status === 3) {
        status = [<i className="fa fa-times-circle-o fa-1-5x text-danger" title="Failed"/>,<a title={t("Go to event")} href={"/rhn/systems/details/history/Event.do?sid=" + data.buildServer.id + "&aid=" + action.id}>{t(props.name + " has failed")}</a>]
    } else {
        status = [<i className="fa fa-question-circle fa-1-5x" title="Unknown"/>,"No information"]
    }

    return (
        <div className="table-responsive">
            <table className="table">
                <tbody>
                    <tr>
                        <td width="20%">{t(props.name + " Status")}:</td>
                        <td>{status}</td>
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

function ImageInfo(props) {
    const data = props.data;
    return (
        <div className="table-responsive">
            <table className="table">
                <tbody>
                    <tr>
                        <td>Image Name:</td>
                        <td>{data.name ? data.name : "-"}</td>
                    </tr>
                    <tr>
                        <td>Version:</td>
                        <td>{data.version ? data.version : "-"}</td>
                    </tr>
                    <tr>
                        <td>Checksum:</td>
                        <td>{data.checksum ? data.checksum : "-"}</td>
                    </tr>
                    <tr>
                        <td>Profile:</td>
                        { data.profile ?
                            <td>{data.profile.label}{ isAdmin && <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + data.profile.id + "?url_bounce=" + encodeURIComponent("/rhn/manager/cm/images#/overview/" + data.id)} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit profile")}/>}</td>
                            :<td>-</td>
                        }
                    </tr>
                    <tr>
                        <td>Store:</td>
                        { data.store ?
                            <td>{data.store.label}{ isAdmin && <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imagestores/edit/" + data.store.id + "?url_bounce=" + encodeURIComponent("/rhn/manager/cm/images#/overview/" + data.id)} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit store")}/>}</td>
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
                                        <ul>{data.channels.children.map(ch => <li><a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>{ch.name}</a></li>)}</ul>
                                    </li>
                                </ul>
                            </td>
                            :<td>-</td>
                        }
                    </tr>
                    <tr>
                        <td>Installed Products</td>
                        { data.installedProducts ?
                            <td>
                                <ul className="list-group">
                                    <li className="list-group-item">{data.installedProducts.base}</li>
                                    {data.installedProducts.addons.map(addon =>
                                        <li className="list-group-item">{addon}</li>
                                    )}
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
                    { Object.entries(data).map(e => <tr><td>{e[0]}:</td><td>{e[1] || "-"}</td></tr>)}
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
        let status, statusText;

        if(!row.patches || row.installedPackages === 0) {
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
        return this.props.data.buildAction && this.props.data.buildAction.status === 2;
    }

    hasInspected() {
        return this.props.data.inspectAction && this.props.data.inspectAction.status === 2;
    }

    getRescheduleLink() {
        const data = this.props.data;

        const params = {
            host: data.buildServer ? data.buildServer.id : undefined,
            profile: data.profile ? data.profile.id : undefined,
            version: data.version ? encodeURIComponent(data.version) : undefined
        }

        const loc = "/rhn/manager/cm/build?" + (
            Object.keys(params).filter(k => params[k] !== undefined)
                    .map(key => key + '=' + params[key]).join('&')
        );

        return loc;
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
                        <div>
                            <h3>Build</h3>
                            <BuildStatus data={data}/>
                        </div>

                        { this.hasBuilt() &&
                        <div>
                            <h3>Inspect</h3>
                            <InspectStatus data={data}/>
                        </div>
                        }
                        { isAdmin &&
                        <div className="btn-group pull-right">
                            <LinkButton
                                text={t("Rebuild")}
                                icon="fa-cogs"
                                title={t("Reschedule the build")}
                                className="btn-default btn-xs"
                                href={this.getRescheduleLink()}
                            />
                            { this.hasBuilt() &&
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
            <InspectDialog data={data} onInspect={this.props.onInspect}/>
            </div>
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
                <p>Schedule an inspect for image: <strong>{this.props.data.name + ":" + this.props.data.version}</strong></p>
                <Input.Form model={this.state.model} className="image-inspect-form"
                        onChange={this.onChange.bind(this)} divClass="col-md-12">
                    <Input.DateTime name="earliest" required timezone={timezone} />
                </Input.Form>
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
