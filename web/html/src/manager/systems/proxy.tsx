import { Component } from "react";

import { AsyncButton } from "components/buttons";
import { ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";

import Network from "utils/network";

type ProxyType = {
  hostname: string;
  id: number;
  name: string;
  path: string[];
  primaryFqdn?: string;
  additionalFqdns?: string[];
};

export type ProxySelection = { proxyId: number; proxyFqdn?: string };

/**
 * A proxy selection is encoded in a single <option> value as "<id>:<fqdn>" so that a proxy with
 * several FQDNs can be offered as distinct choices. This parses that value back into its parts.
 */
export function parseProxySelection(value?: string): ProxySelection {
  if (!value || value === "0") {
    return { proxyId: 0 };
  }
  const sep = value.indexOf(":");
  if (sep < 0) {
    return { proxyId: parseInt(value, 10) };
  }
  return { proxyId: parseInt(value.slice(0, sep), 10), proxyFqdn: value.slice(sep + 1) };
}

export function ProxyOptions({ proxies }: { proxies: ProxyType[] }) {
  const arrow = " \u2192 ";
  const optionsList: JSX.Element[] = [];
  proxies.forEach((p) => {
    const primary = p.primaryFqdn || p.name;
    const primaryValue = `${p.id}:${primary}`;
    optionsList.push(
      <option key={primaryValue} value={primaryValue}>
        {[primary].concat(p.path).join(arrow)}
      </option>
    );
    if (p.additionalFqdns && p.additionalFqdns.length > 0) {
      p.additionalFqdns.forEach((add) => {
        const addValue = `${p.id}:${add}`;
        optionsList.push(
          <option key={addValue} value={addValue}>
            {"\u00A0\u00A0\u00A0\u00A0" + [add].concat(p.path).join(arrow)}
          </option>
        );
      });
    }
  });
  return <>{optionsList}</>;
}

// See java/core/src/main/resources/com/suse/manager/webui/templates/minion/proxy.jade
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
  proxy: string;
};

class Proxy extends Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const msg = window.minions?.length
      ? []
      : MessagesUtils.warning(
          <span>{t("Please select a list of minions (not proxies or traditional clients).")}</span>
        );

    let initialProxy = "0";
    if (props.currentProxy) {
      const p = props.proxies.find((px) => px.id === props.currentProxy);
      const primary = p ? p.primaryFqdn || p.name : "";
      initialProxy = p ? `${props.currentProxy}:${primary}` : String(props.currentProxy);
    }

    this.state = {
      messages: msg,
      proxy: initialProxy,
    };
  }

  proxyChanged = (event) => {
    this.setState({
      proxy: event.target.value,
    });
  };

  onSet = () => {
    const { proxyId, proxyFqdn } = parseProxySelection(this.state.proxy);

    const request = Network.post("/rhn/manager/api/systems/proxy", {
      proxy: proxyId,
      proxyFqdn: proxyFqdn || undefined,
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
        defaultType="btn-primary"
        icon="fa-plus"
        text={t("Change Proxy")}
        action={this.onSet}
        disabled={!window.minions?.length}
        key="change"
      />,
    ];

    return (
      <div>
        {messages}
        <BootstrapPanel title={t("Change Proxy")} header={<p>{t("Connect minion(s) to another proxy server.")}</p>}>
          <div className="form-horizontal">
            <div className="row">
              <label className="col-md-3 control-label">{t("New Proxy")}:</label>
              <div className="col-md-6">
                <select value={this.state.proxy} onChange={this.proxyChanged} className="form-control" name="proxies">
                  <option key="none" value="0">
                    {t("None")}
                  </option>
                  <ProxyOptions proxies={this.props.proxies} />
                </select>
              </div>
            </div>
            <div className="row">
              <div className="col-md-offset-3 offset-md-3 col-md-6">{buttons}</div>
            </div>
          </div>
        </BootstrapPanel>
      </div>
    );
  }
}

export { Proxy };
