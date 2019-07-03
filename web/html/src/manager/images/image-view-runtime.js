/* eslint-disable */
'use strict';

const React = require("react");
const { BootstrapPanel } = require('components/panels/BootstrapPanel');

class ImageViewRuntime extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    const data = this.props.data;
    const runtimeInfo = data.clusters ? Object.keys(data.clusters)
      .map(cluster => <ClusterInfo key={cluster} data={{name: cluster, pods: data.clusters[cluster]}}/>)
      : undefined;

    let msg;
    if (!this.props.gotRuntimeInfo) {
      msg = t("Loading runtime information...");
    } else if (!runtimeInfo || runtimeInfo.length === 0) {
      msg = <span><i className="fa fa-1-5x fa-info-circle"/>There is no container running with this image on any <a href="/rhn/manager/vhms">registered cluster</a>.</span>;
    }

    return (
      <div>
        { msg ? <BootstrapPanel><h4>{msg}</h4></BootstrapPanel> : runtimeInfo }
      </div>
    );
  }
}

class PodInfo extends React.Component {
  constructor(props) {
    super(props);
  }

  renderStatusIcon(statusId) {
    let icon;

    if (statusId === 1) {
      icon = <i className="fa fa-check-circle fa-1-5x text-success" title={t("Instance is consistent with SUSE Manager")}/>
    } else if (statusId === 2) {
      icon = <i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>
    } else if (statusId === 3) {
      icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Instance is outdated")}/>
    } else {
      icon = <span>-</span>
    }

    return icon;
  }

  render() {
    const data = this.props.data;
    return (
      <tr>
        <td>{data.name}</td>
        <td>{data.namespace}</td>
        <td>{this.renderStatusIcon(data.statusId)}</td>
      </tr>
    );
  }
}

class ClusterInfo extends React.Component {
  constructor(props) {
    super(props);
  }

  renderTitle(data) {
    const statusId = data.pods.map(p => p.statusId).reduce((a,b) => Math.max(a,b));

    let icon;

    if (statusId === 1) {
      icon = <i className="fa fa-check-circle fa-1-5x text-success" title={t("Cluster is consistent with SUSE Manager")}/>
    } else if (statusId === 2) {
      icon = <i className="fa fa-question-circle fa-1-5x" title={t("No information")}/>
    } else if (statusId === 3) {
      icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Cluster is outdated")}/>
    }

    return icon ? <span>{icon} {data.name}</span> : data.name;
  }

  render() {
    const data = this.props.data;
    return (
      <BootstrapPanel title={this.renderTitle(data)}>
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <th>Pod</th>
                <th>Namespace</th>
                <th>Status</th>
              </tr>
              {data.pods.map(p => <PodInfo key={p.name} data={p}/>)}
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );
  }
}

module.exports = {
  ImageViewRuntime: ImageViewRuntime
}
