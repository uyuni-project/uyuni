import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { InnerPanel } from "components/panels/InnerPanel";
import { MessagesContainer } from "components/toastr";
import { Loading } from "components/utils/loading/Loading";

import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/templates/minion/ptf-overview.jade
declare global {
  interface Window {
    serverId?: any;
  }
}

type Props = {
  serverId: number;
};

type State = {
  loading: boolean;
  allowedActions: Array<string>;
};

class PtfOverview extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    this.state = {
      loading: true,
      allowedActions: [],
    };

    this.loadData();
  }

  loadData() {
    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/details/ptf/allowedActions`)
      .then((response) => {
        this.setState({
          loading: false,
          allowedActions: response.data,
        });
      })
      .catch(Network.showResponseErrorToastr);
  }

  render() {
    return (
      <>
        <MessagesContainer />
        <InnerPanel title={t("Program Temporary Fixes (PTFs)")} icon="fa-fire-extinguisher">
          {this.state.loading ? (
            <Loading text={t("Loading...")} />
          ) : this.state.allowedActions.length === 0 ? (
            <p>{t("There are no relevant actions that can be executed on this system.")}</p>
          ) : (
            <ul>
              {this.state.allowedActions.includes("packages.remove") && (
                <li>
                  <a className="ja-spa" href={`/rhn/manager/systems/details/ptf/list?sid=${this.props.serverId}`}>
                    {t("List / Remove Installed PTFs")}
                  </a>
                </li>
              )}
              {this.state.allowedActions.includes("packages.update") && (
                <li>
                  <a className="ja-spa" href={`/rhn/manager/systems/details/ptf/install?sid=${this.props.serverId}`}>
                    {t("Install PTF")}
                  </a>
                </li>
              )}
            </ul>
          )}
        </InnerPanel>
      </>
    );
  }
}

export const renderer = (id: string) =>
  SpaRenderer.renderNavigationReact(<PtfOverview serverId={window.serverId} />, document.getElementById(id));
