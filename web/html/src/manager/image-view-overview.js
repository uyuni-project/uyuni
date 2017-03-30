'use strict';

const React = require("react");
const {LinkButton, Button} = require("../components/buttons");
const DateTime = require("../components/datetime").DateTime;

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
                            <td>{data.profile.label}<LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + data.profile.id} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit profile")}/></td>
                            :<td>-</td>
                        }
                    </tr>
                    <tr>
                        <td>Store:</td>
                        { data.store ?
                            <td>{data.store.label}<LinkButton icon="fa-edit" href={"/rhn/manager/cm/imagestores/edit/" + data.store.id} className="btn-xs btn-default pull-right" text={t("Edit")} title={t("Edit store")}/></td>
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
                        <BuildStatus data={data}/>
                        <LinkButton
                            text={t("Rebuild")}
                            icon="fa-cogs"
                            title={t("Reschedule the build")}
                            className="btn-default pull-right btn-xs"
                            href={this.getRescheduleLink()}
                        />
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
            </div>
        );
    }
}

module.exports = {
    ImageViewOverview: ImageViewOverview
}
