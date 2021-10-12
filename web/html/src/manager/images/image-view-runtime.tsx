import * as React from "react";
import { BootstrapPanel } from "components/panels/BootstrapPanel";

// See java/code/src/com/suse/manager/webui/templates/content_management/view.jade
declare global {
  interface Window {
    imageId?: any;
    isAdmin?: any;
    timezone?: any;
    localTime?: any;
    isRuntimeInfoEnabled?: any;
  }
}

type ImageViewRuntimeProps = {
  data: any;
  gotRuntimeInfo: any;
};

class ImageViewRuntime extends React.Component<ImageViewRuntimeProps> {
  render() {
    const data = this.props.data;
    const runtimeInfo = data.clusters
      ? Object.keys(data.clusters).map((cluster) => (
          <ClusterInfo key={cluster} data={{ name: cluster, pods: data.clusters[cluster] }} />
        ))
      : undefined;

    let msg;
    if (!this.props.gotRuntimeInfo) {
      msg = t("Loading runtime information...");
    } else if (!runtimeInfo || runtimeInfo.length === 0) {
      msg = (
        <span>
          <i className="fa fa-1-5x fa-info-circle" />
          There is no container running with this image on any <a href="/rhn/manager/vhms">registered cluster</a>.
        </span>
      );
    }

    return (
      <div>
        {msg ? (
          <BootstrapPanel>
            <h4>{msg}</h4>
          </BootstrapPanel>
        ) : (
          runtimeInfo
        )}
      </div>
    );
  }
}

type PodInfoProps = {
  data: any;
};

class PodInfo extends React.Component<PodInfoProps> {
  renderStatusIcon(statusId) {
    let icon;

    if (statusId === 1) {
      icon = (
        <i className="fa fa-check-circle fa-1-5x text-success" title={t("Instance is consistent with SUSE Manager")} />
      );
    } else if (statusId === 2) {
      icon = <i className="fa fa-question-circle fa-1-5x" title={t("No information")} />;
    } else if (statusId === 3) {
      icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Instance is outdated")} />;
    } else {
      icon = <span>-</span>;
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

type ClusterInfoProps = {
  data: any;
};

class ClusterInfo extends React.Component<ClusterInfoProps> {
  renderTitle(data) {
    const statusId = data.pods.map((p) => p.statusId).reduce((a, b) => Math.max(a, b));

    let icon;

    if (statusId === 1) {
      icon = (
        <i className="fa fa-check-circle fa-1-5x text-success" title={t("Cluster is consistent with SUSE Manager")} />
      );
    } else if (statusId === 2) {
      icon = <i className="fa fa-question-circle fa-1-5x" title={t("No information")} />;
    } else if (statusId === 3) {
      icon = <i className="fa fa-exclamation-triangle fa-1-5x text-warning" title={t("Cluster is outdated")} />;
    }

    return icon ? (
      <span>
        {icon} {data.name}
      </span>
    ) : (
      data.name
    );
  }

  render() {
    const data = this.props.data;
    return (
      <BootstrapPanel title={this.renderTitle(data)}>
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <th>{t("Pod")}</th>
                <th>{t("Namespace")}</th>
                <th>{t("Status")}</th>
              </tr>
              {data.pods.map((p) => (
                <PodInfo key={p.name} data={p} />
              ))}
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );
  }
}

export { ImageViewRuntime };
