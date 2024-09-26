import * as React from "react";

import { AsyncButton } from "components/buttons";
import { Messages } from "components/messages/messages";
import { MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/templates/systems/mgr-server.jade
type Props = {
  serverId: string;
  name: string;
  version: string;
  reportDbName: string;
  reportDbHost: string;
  reportDbPort: number;
  reportDbUser: string;
  reportDbLastSynced?: string;
  isAdmin: boolean;
  /** Locale of the help links */
  docsLocale: string;
};

type State = {
  messages: MessageType[];
};

const messageMap = {
  invalid_systemid: t("Not a system id"),
  unknown_system: t("Unknown System"),
  system_not_mgr_server: t("System is not a peripheral server"),
};

class MgrServer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
    };
  }

  handlePwReset() {
    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/mgr-server-reportdb-newpw`)
      .then((data) => {
        if (data.success) {
          this.setState({
            messages: MessagesUtils.success(
              <a href={"/rhn/systems/details/history/History.do?sid=" + this.props.serverId}>
                {t("New password generated and action for peripheral server scheduled")}
              </a>
            ),
          });
        } else {
          this.setState({
            messages: MessagesUtils.error(data.messages.map((m) => messageMap[m])),
          });
        }
      })
      .catch((jqXHR) => {
        this.handleResponseError(jqXHR);
      });
  }

  handleResponseError = (jqXHR, arg = "") => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR, (status, msg) => (messageMap[msg] ? t(messageMap[msg], arg) : msg)),
    });
  };

  render() {
    return (
      <div>
        {this.state.messages ? <Messages items={this.state.messages} /> : null}
        <TopPanel
          title={t("Peripheral Server")}
          icon={"spacewalk-icon-suse-manager"}
          helpUrl={"reference/systems/systems-list.html"}
        >
          <div className="panel panel-default">
            <div className="panel-body">
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Name")}</dt>
                <dd className="col-10 col-xs-10">{this.props.name}</dd>
              </dl>
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Version")}</dt>
                <dd className="col-10 col-xs-10">{this.props.version}</dd>
              </dl>
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Report Database Name")}</dt>
                <dd className="col-10 col-xs-10">{this.props.reportDbName}</dd>
              </dl>
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Report Database Host")}</dt>
                <dd className="col-10 col-xs-10">
                  {this.props.reportDbHost}:{this.props.reportDbPort}
                </dd>
              </dl>
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Report Database User")}</dt>
                <dd className="col-10 col-xs-10">{this.props.reportDbUser}</dd>
              </dl>
              <dl className="row">
                <dt className="col-2 col-xs-2">{t("Report Database Last Synced")}</dt>
                <dd className="col-10 col-xs-10">{this.props.reportDbLastSynced}</dd>
              </dl>
            </div>
          </div>
          <AsyncButton
            id={"btn-delete-confirm-" + this.props.serverId}
            text={t("Regenerate Reporting Database Password")}
            title={t("Regenerate Reporting Database Password")}
            icon="fa-refresh"
            defaultType="btn-default"
            action={() => this.handlePwReset()}
          />
        </TopPanel>
      </div>
    );
  }
}

export { MgrServer };
