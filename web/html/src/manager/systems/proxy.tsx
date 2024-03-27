import * as React from "react";

import { AsyncButton } from "components/buttons";
import { ActionLink } from "components/links";
import { Messages, MessageType } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";

import Network from "utils/network";

type ProxyType = {
  hostname: string;
  id: number;
  name: string;
  path: string[];
};

// See java/code/src/com/suse/manager/webui/templates/minion/proxy.jade
declare global {
  interface Window {
    proxies?: any;
    minions?: any[];
    currentProxy?: number;
  }
}

type Props = {
  proxies: ProxyType[];
  currentProxy?: number;
};

type State = {
  messages: MessageType[];
  proxy: number;
};

class Proxy extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const msg = window.minions?.length
      ? []
      : MessagesUtils.warning(
          <span>{t("Please select a list of minions (not proxies or traditional clients).")}</span>
        );

    this.state = {
      messages: msg,
      proxy: props.currentProxy || 0,
    };
  }

  proxyChanged = (event) => {
    this.setState({
      proxy: event.target.value,
    });
  };

  onSet = () => {
    const request = Network.post("/rhn/manager/api/systems/proxy", {
      proxy: this.state.proxy ? this.state.proxy : 0,
      ids: window.minions?.map((m) => m.id),
    })
      .then((data) => {
        const msg = MessagesUtils.info(
          <span>
            {t("Change of proxy server has been ")}
            {data.data.actions.length > 1 ? (
              <>
                <ActionLink id={data.data.actions[0]}>{t("scheduled")}(1)</ActionLink>
                <ActionLink id={data.data.actions[1]}>(2).</ActionLink>
              </>
            ) : (
              <ActionLink id={data.data.actions[0]}>{t("scheduled")}.</ActionLink>
            )}
          </span>
        );

        this.setState({
          messages: msg,
        });
      })
      .catch(this.handleResponseError);

    return request;
  };

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  render() {
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;
    const buttons = [
      <AsyncButton
        id="bootstrap-btn"
        defaultType="btn-success"
        icon="fa-plus"
        text={t("Change Proxy")}
        action={this.onSet}
        disabled={!window.minions?.length}
      />,
    ];

    const arrow = " \u2192 ";
    const proxies = this.props.proxies.map((p) => (
      <option key={p.id} value={p.id}>
        {[p.name].concat(p.path).join(arrow)}
      </option>
    ));

    return (
      <div>
        {messages}
        <BootstrapPanel title={t("Change Proxy")} header={<p>{t("Connect minion(s) to another proxy server.")}</p>}>
          <div className="form-horizontal">
            <div className="form-group">
              <label className="col-md-3 control-label">{t("New Proxy")}:</label>
              <div className="col-md-6">
                <select value={this.state.proxy} onChange={this.proxyChanged} className="form-control" name="proxies">
                  <option key="none" value="0">
                    {t("None")}
                  </option>
                  {proxies}
                </select>
              </div>
            </div>
            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">{buttons}</div>
            </div>
          </div>
        </BootstrapPanel>
      </div>
    );
  }
}

export { Proxy };
