import * as React from "react";
import { TopPanel } from "components/panels/TopPanel";
import { Messages } from "components/messages";
import Network from "utils/network";
import { AsyncButton } from "components/buttons";
import SpaRenderer from "core/spa/spa-renderer";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

// See java/code/src/com/suse/manager/webui/templates/minion/bootstrap.jade
declare global {
  interface Window {
    availableActivationKeys?: any;
    proxies?: any;
  }
}

type Props = {
  proxies: any[];
  availableActivationKeys: any[];
};

type State = {
  host: string;
  port: string;
  user: string;
  authMethod: string;
  password: string;
  privKey: string;
  privKeyPwd: string;
  activationKey: string;
  ignoreHostKeys: boolean;
  manageWithSSH: boolean;
  messages: any[];
  proxy: string;
  showProxyHostnameWarn: boolean;
  loading: boolean;
  privKeyLoading?: boolean;
  success?: any;
};

class BootstrapMinions extends React.Component<Props, State> {
  initState: State;

  constructor(props: Props) {
    super(props);

    this.initState = {
      host: "",
      port: "",
      user: "",
      authMethod: "password",
      password: "",
      privKey: "",
      privKeyPwd: "",
      activationKey: "",
      ignoreHostKeys: true,
      manageWithSSH: false,
      messages: [],
      proxy: "",
      showProxyHostnameWarn: false,
      loading: false,
    };

    this.state = this.initState;

    [
      "hostChanged",
      "portChanged",
      "userChanged",
      "authMethodChanged",
      "passwordChanged",
      "privKeyPwdChanged",
      "privKeyFileChanged",
      "privKeyLoaded",
      "onBootstrap",
      "ignoreHostKeysChanged",
      "manageWithSSHChanged",
      "activationKeyChanged",
      "clearFields",
      "proxyChanged",
    ].forEach(method => (this[method] = this[method].bind(this)));
  }

  hostChanged(event) {
    this.setState({
      host: event.target.value,
    });
  }

  portChanged(event) {
    this.setState({
      port: event.target.value,
    });
  }

  userChanged(event) {
    this.setState({
      user: event.target.value,
    });
  }

  authMethodChanged(event) {
    this.setState({
      authMethod: event.target.value,
    });
  }

  passwordChanged(event) {
    this.setState({
      password: event.target.value,
    });
  }

  privKeyFileChanged(event) {
    this.setState({
      privKeyLoading: true,
    });
    const reader = new FileReader();
    reader.onload = e => this.privKeyLoaded(e.target?.result);
    reader.readAsText(event.target.files[0]);
  }

  privKeyLoaded(keyString) {
    this.setState({
      // replace CRLF from Windows
      privKey: keyString.replace(/\r\n/g, "\n"),
      privKeyLoading: false,
    });
  }

  privKeyPwdChanged(event) {
    this.setState({
      privKeyPwd: event.target.value,
    });
  }

  ignoreHostKeysChanged(event) {
    this.setState({
      ignoreHostKeys: event.target.checked,
    });
  }

  manageWithSSHChanged(event) {
    this.setState({
      manageWithSSH: event.target.checked,
      port: "",
    });
  }

  activationKeyChanged(event) {
    this.setState({
      activationKey: event.target.value,
    });
  }

  proxyChanged(event) {
    var proxyId = event.target.value;
    var proxy = this.props.proxies.find(p => DEPRECATED_unsafeEquals(p.id, proxyId));
    var showWarn = proxy && proxy.hostname.indexOf(".") < 0;
    this.setState({
      proxy: event.target.value,
      showProxyHostnameWarn: showWarn,
    });
  }

  onBootstrap() {
    this.setState({ messages: [], loading: true });
    var formData: any = {};
    formData["host"] = this.state.host.trim();
    formData["port"] = this.state.port.trim() === "" ? undefined : this.state.port.trim();
    formData["user"] = this.state.user.trim() === "" ? undefined : this.state.user.trim();
    formData["activationKeys"] = this.state.activationKey === "" ? [] : [this.state.activationKey];
    formData["ignoreHostKeys"] = this.state.ignoreHostKeys;

    const authMethod = this.state.authMethod;
    formData["authMethod"] = authMethod;
    if (authMethod === "password") {
      formData["password"] = this.state.password.trim();
    } else if (authMethod === "ssh-key") {
      formData["privKey"] = this.state.privKey;
      formData["privKeyPwd"] = this.state.privKeyPwd;
    }
    if (this.state.proxy) {
      formData["proxy"] = this.state.proxy;
    }

    const request = Network.post(
      this.state.manageWithSSH ? "/rhn/manager/api/systems/bootstrap-ssh" : "/rhn/manager/api/systems/bootstrap",
      formData
    ).then(
      data => {
        this.setState({
          success: data.success,
          messages: data.messages,
          loading: false,
        });
      },
      xhr => {
        try {
          this.setState({
            success: false,
            messages: [JSON.parse(xhr.responseText)],
            loading: false,
          });
        } catch (err) {
          var errMessages =
            DEPRECATED_unsafeEquals(xhr.status, 0)
              ? [
                  t(
                    "Request interrupted or invalid response received from the server. Please check if your minion was bootstrapped correctly."
                  ),
                ]
              : [Network.errorMessageByStatus(xhr.status)];
          this.setState({
            success: false,
            messages: errMessages,
            loading: false,
          });
        }
      }
    );
    return request;
  }

  clearFields() {
    this.setState(this.initState);
  }

  render() {
    var messages: React.ReactNode = null;
    if (this.state.success) {
      messages = (
        <Messages
          items={[
            {
              severity: "success",
              text: (
                <p>
                  {t("Successfully bootstrapped host! Your system should appear in")}{" "}
                  <a className="js-spa" href="/rhn/systems/SystemList.do">
                    {t("systems")}
                  </a>{" "}
                  {t("shortly")}.
                </p>
              ),
            },
          ]}
        />
      );
    } else if (this.state.messages.length > 0) {
      messages = (
        <Messages
          items={this.state.messages.map(function(msg) {
            return { severity: "error", text: msg };
          })}
        />
      );
    } else if (this.state.loading) {
      messages = (
        <Messages
          items={[{ severity: "info", text: <p>{t("Your system is bootstrapping: waiting for a response..")}</p> }]}
        />
      );
    } else if (this.state.privKeyLoading) {
      messages = <Messages items={[{ severity: "info", text: <p>{t("Loading SSH Private Key..")}</p> }]} />;
    }

    var buttons = [
      <AsyncButton
        id="bootstrap-btn"
        defaultType="btn-success"
        icon="fa-plus"
        text={t("Bootstrap")}
        disabled={this.state.privKeyLoading}
        action={this.onBootstrap}
      />,
      <AsyncButton
        id="clear-btn"
        defaultType="btn-default pull-right"
        icon="fa-eraser"
        text={t("Clear fields")}
        action={this.clearFields}
      />,
    ];

    const productName = window._IS_UYUNI ? "Uyuni" : "SUSE Manager";

    const authenticationData =
      this.state.authMethod === "password" ? (
        <div className="form-group">
          <label className="col-md-3 control-label">Password:</label>
          <div className="col-md-6">
            <input
              name="password"
              className="form-control"
              type="password"
              autoComplete="new-password"
              placeholder={t("e.g., ••••••••••••")}
              value={this.state.password}
              onChange={this.passwordChanged}
            />
          </div>
        </div>
      ) : (
        <div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("SSH Private Key")}:</label>
            <div className="col-md-6">
              <input name="privKeyFile" className="form-control" type="file" onChange={this.privKeyFileChanged} />
              <div className="help-block">
                <i className="fa fa-exclamation-triangle" />
                {t(
                  "The file will be stored in a temporary file on the server and will be deleted after the bootstrapping procedure"
                )}
              </div>
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("SSH Private Key Passphrase")}:</label>
            <div className="col-md-6">
              <input
                name="privKeyPwd"
                className="form-control"
                type="password"
                autoComplete="new-password"
                placeholder={t("Leave empty for no passphrase")}
                value={this.state.privKeyPwd}
                onChange={this.privKeyPwdChanged}
              />
            </div>
          </div>
        </div>
      );

    return (
      <TopPanel title={t("Bootstrap Minions")} icon="fa fa-rocket" helpUrl="reference/systems/bootstrapping.html">
        <p>
          {t(
            "You can add systems to be managed by providing SSH credentials only. {0} will prepare the system remotely and will perform the registration.",
            productName
          )}
        </p>
        {messages}
        <div className="form-horizontal">
          <div className="form-group">
            <label className="col-md-3 control-label">Host:</label>
            <div className="col-md-6">
              <input
                name="hostname"
                className="form-control"
                type="text"
                placeholder={t("e.g., host.domain.com")}
                value={this.state.host}
                onChange={this.hostChanged}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">SSH Port:</label>
            <div className="col-md-6">
              <input
                name="port"
                className="form-control numeric set-maxlength-width"
                type="text"
                maxLength={5}
                placeholder="22"
                onChange={this.portChanged}
                onKeyPress={window.numericValidate}
                value={this.state.port}
                disabled={this.state.manageWithSSH}
                title={t("Port range: 1 - 65535")}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">User:</label>
            <div className="col-md-6">
              <input
                name="user"
                className="form-control"
                type="text"
                placeholder="root"
                value={this.state.user}
                onChange={this.userChanged}
              />
              {this.state.manageWithSSH && (
                <div className="help-block">
                  <i className="fa fa-exclamation-triangle" />
                  {t(
                    "The user will have an effect only during the bootstrap process. Further connections will be made by the user specified in rhn.conf. The default user for the key 'ssh_push_sudo_user' is 'root'. This user is set after {0}'s SSH key is deployed during the bootstrap procedure.",
                    productName
                  )}
                </div>
              )}
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">Authentication Method:</label>

            <div className="col-md-6">
              <div className="radio col-md-3">
                <label>
                  <input
                    name="authMethod"
                    type="radio"
                    value="password"
                    checked={this.state.authMethod === "password"}
                    onChange={this.authMethodChanged}
                  />
                  <span>{t("Password")}</span>
                </label>
              </div>
              <div className="radio col-md-3">
                <label>
                  <input
                    name="authMethod"
                    type="radio"
                    value="ssh-key"
                    checked={this.state.authMethod === "ssh-key"}
                    onChange={this.authMethodChanged}
                  />
                  <span>{t("SSH Private Key")}</span>
                </label>
              </div>
            </div>
          </div>
          {authenticationData}
          <div className="form-group">
            <label className="col-md-3 control-label">Activation Key:</label>
            <div className="col-md-6">
              <select
                value={this.state.activationKey}
                onChange={this.activationKeyChanged}
                className="form-control"
                name="activationKeys"
              >
                <option key="none" value="">
                  {t("None")}
                </option>
                {this.props.availableActivationKeys
                  .sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
                  .map(k => (
                    <option key={k} value={k}>
                      {k}
                    </option>
                  ))}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Proxy")}:</label>
            <div className="col-md-6">
              <select value={this.state.proxy} onChange={this.proxyChanged} className="form-control" name="proxies">
                <option key="none" value="">
                  {t("None")}
                </option>
                {this.props.proxies.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                    {p.path.reduce(
                      (acc, val, idx) => acc + "\u2192 " + val + (DEPRECATED_unsafeEquals(idx, p.path.length - 1) ? "" : " "),
                      ""
                    )}
                  </option>
                ))}
              </select>
              <div>
                <i
                  style={this.state.showProxyHostnameWarn ? { display: "inline" } : { display: "none" }}
                  className="fa fa-exclamation-triangle text-warning"
                >
                  {t(
                    "The hostname of the proxy is not fully qualified. This may cause problems when accessing the channels."
                  )}
                </i>
              </div>
            </div>
          </div>
          <div className="form-group">
            <div className="col-md-3"></div>
            <div className="col-md-6">
              <div className="checkbox">
                <label>
                  <input
                    name="ignoreHostKeys"
                    type="checkbox"
                    checked={this.state.ignoreHostKeys}
                    onChange={this.ignoreHostKeysChanged}
                  />
                  <span>Disable SSH strict host key checking during bootstrap process</span>
                </label>
              </div>
            </div>
          </div>
          <div className="form-group">
            <div className="col-md-3"></div>
            <div className="col-md-6">
              <div className="checkbox">
                <label>
                  <input
                    name="manageWithSSH"
                    type="checkbox"
                    checked={this.state.manageWithSSH}
                    onChange={this.manageWithSSHChanged}
                  />
                  <span>Manage system completely via SSH (will not install an agent)</span>
                </label>
              </div>
            </div>
          </div>
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">{buttons}</div>
          </div>
        </div>
      </TopPanel>
    );
  }

  componentDidMount() {
    window.addEventListener("beforeunload", e => {
      if (this.state.loading) {
        var confirmationMessage = "Are you sure you want to close this page while bootstrapping is in progress ?";
        (e || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
      }
    });
  }
}

export const renderer = id =>
  SpaRenderer.renderNavigationReact(
    <BootstrapMinions availableActivationKeys={window.availableActivationKeys} proxies={window.proxies} />,
    document.getElementById(id)
  );
