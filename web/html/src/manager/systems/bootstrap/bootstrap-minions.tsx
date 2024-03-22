import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton, Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { ModalLink } from "components/dialog/ModalLink";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/templates/minion/bootstrap.jade
declare global {
  interface Window {
    availableActivationKeys?: any;
    proxies?: any;
  }
}

type ErrorDetails = {
  message: string;
  standardOutput?: string;
  standardError?: string;
  result?: string;
};

type ErrorDetailsDialogProps = {
  error: ErrorDetails | null;
  onDialogClose: () => void;
};

class ErrorDetailsDialog extends React.Component<ErrorDetailsDialogProps> {
  render() {
    let content, title, buttons;

    if (this.props.error) {
      title = (
        <span>
          <i className="fa fa-list" /> {t("Error Details")}
        </span>
      );

      content = (
        <>
          <p>{this.props.error.message}</p>
          {this.props.error.standardOutput && (
            <div className="form-group">
              <label className="control-label">Standard Output:</label>
              <textarea
                readOnly
                disabled
                className="form-control"
                data-testid="stdout"
                value={this.props.error.standardOutput}
                rows={5}
              />
            </div>
          )}
          {this.props.error.standardError && (
            <div className="form-group">
              <label className="control-label">Standard Error:</label>
              <textarea
                readOnly
                disabled
                className="form-control"
                data-testid="stderr"
                value={this.props.error.standardError}
                rows={5}
              />
            </div>
          )}
          {this.props.error.result && (
            <div className="form-group">
              <label className="control-label">Result:</label>
              <textarea
                readOnly
                disabled
                className="form-control"
                data-testid="result"
                value={this.props.error.result}
                rows={5}
              />
            </div>
          )}
        </>
      );

      buttons = (
        <div>
          <Button
            className="btn-default"
            text={t("Close")}
            title={t("Close")}
            icon="fa-close"
            handler={this.props.onDialogClose}
          />
        </div>
      );
    }

    return (
      <Dialog
        id="show-error-details"
        isOpen={this.props.error != null}
        title={title}
        className="modal-xs"
        onClose={this.props.onDialogClose}
        content={content}
        footer={buttons}
      />
    );
  }
}

type Props = {
  proxies: any[];
  availableActivationKeys: any[];
  ansibleInventoryId: number | null;
  targetHost: string | null;
};

enum AuthMethod {
  Password = "password",
  SshKey = "ssh-key",
  AnsiblePreauth = "ansible-preauth",
}

type State = {
  host: string;
  port: string;
  user: string;
  authMethod: AuthMethod;
  password: string;
  privKey: string;
  privKeyPwd: string;
  activationKey: string;
  reactivationKey: string;
  ignoreHostKeys: boolean;
  manageWithSSH: boolean;
  errors: any[];
  proxy: string;
  showProxyHostnameWarn: boolean;
  loading: boolean;
  privKeyLoading?: boolean;
  success?: any;
  errorDetails: ErrorDetails | null;
};

class BootstrapMinions extends React.Component<Props, State> {
  initState: State;

  constructor(props: Props) {
    super(props);

    this.initState = {
      host: props.targetHost || "",
      port: "",
      user: "",
      authMethod: props.ansibleInventoryId ? AuthMethod.AnsiblePreauth : AuthMethod.Password,
      password: "",
      privKey: "",
      privKeyPwd: "",
      activationKey: "",
      reactivationKey: "",
      ignoreHostKeys: true,
      manageWithSSH: false,
      errors: [],
      proxy: "",
      showProxyHostnameWarn: false,
      loading: false,
      errorDetails: null,
    };

    this.state = this.initState;
  }

  hostChanged = (event) => {
    this.setState({
      host: event.target.value,
    });
  };

  portChanged = (event) => {
    this.setState({
      port: event.target.value,
    });
  };

  userChanged = (event) => {
    this.setState({
      user: event.target.value,
    });
  };

  authMethodChanged = (event) => {
    this.setState({
      authMethod: event.target.value,
    });
  };

  passwordChanged = (event) => {
    this.setState({
      password: event.target.value,
    });
  };

  privKeyFileChanged = (event) => {
    this.setState({
      privKeyLoading: true,
    });
    const reader = new FileReader();
    reader.onload = (e) => this.privKeyLoaded(e.target?.result);
    reader.readAsText(event.target.files[0]);
  };

  privKeyLoaded = (keyString) => {
    this.setState({
      // replace CRLF from Windows
      privKey: keyString.replace(/\r\n/g, "\n"),
      privKeyLoading: false,
    });
  };

  privKeyPwdChanged = (event) => {
    this.setState({
      privKeyPwd: event.target.value,
    });
  };

  ignoreHostKeysChanged = (event) => {
    this.setState({
      ignoreHostKeys: event.target.checked,
    });
  };

  manageWithSSHChanged = (event) => {
    this.setState({
      manageWithSSH: event.target.checked,
    });
  };

  activationKeyChanged = (event) => {
    this.setState({
      activationKey: event.target.value,
    });
  };

  reactivationKeyChanged = (event) => {
    this.setState({
      reactivationKey: event.target.value,
    });
  };

  proxyChanged = (event) => {
    var proxyId = parseInt(event.target.value, 10);
    var proxy = this.props.proxies.find((p) => p.id === proxyId);
    var showWarn = proxy && proxy.hostname.indexOf(".") < 0;
    this.setState({
      proxy: event.target.value,
      showProxyHostnameWarn: showWarn,
    });
  };

  hasDetails = (error) => {
    return error.standardOutput || error.standardError || error.result;
  };

  showErrorDetailsDialog = (error) => {
    this.setState({ errorDetails: error });
  };

  closeErrorDetailsDialog = () => {
    this.setState({ errorDetails: null });
  };

  onBootstrap = () => {
    this.setState({ errors: [], loading: true });
    var formData: any = {};
    formData["host"] = this.state.host.trim();
    formData["port"] = this.state.port.trim() === "" ? undefined : this.state.port.trim();
    formData["user"] = this.state.user.trim() === "" ? undefined : this.state.user.trim();
    formData["activationKeys"] = this.state.activationKey === "" ? [] : [this.state.activationKey];
    formData["reactivationKey"] =
      this.state.reactivationKey.trim() === "" ? undefined : this.state.reactivationKey.trim();
    formData["ignoreHostKeys"] = this.state.ignoreHostKeys;

    const authMethod = this.state.authMethod;
    formData["authMethod"] = authMethod;
    if (authMethod === AuthMethod.Password) {
      formData["password"] = this.state.password.trim();
    } else if (authMethod === AuthMethod.SshKey) {
      formData["privKey"] = this.state.privKey;
      formData["privKeyPwd"] = this.state.privKeyPwd;
    } else if (authMethod === AuthMethod.AnsiblePreauth) {
      formData["ansibleInventoryId"] = this.props.ansibleInventoryId;
    }
    if (this.state.proxy) {
      formData["proxy"] = this.state.proxy;
    }

    const request = Network.post(
      this.state.manageWithSSH ? "/rhn/manager/api/systems/bootstrap-ssh" : "/rhn/manager/api/systems/bootstrap",
      formData
    ).then(
      (data) => {
        this.setState({
          success: data.success,
          errors: data.errors,
          loading: false,
        });
      },
      (xhr) => {
        try {
          this.setState({
            success: false,
            errors: [
              {
                message: JSON.parse(xhr.responseText),
              },
            ],
            loading: false,
          });
        } catch (err) {
          var errMessage =
            xhr.status === 0
              ? t(
                  "Request interrupted or invalid response received from the server. Please check if your minion was bootstrapped correctly."
                )
              : Network.errorMessageByStatus(xhr.status);
          this.setState({
            success: false,
            errors: [
              {
                message: errMessage,
              },
            ],
            loading: false,
          });
        }
      }
    );
    return request;
  };

  clearFields = () => {
    this.setState(this.initState);
  };

  render() {
    var alertMessages: MessageType[] = [];
    if (this.state.success) {
      alertMessages = MessagesUtils.success(
        <p>
          {t(
            "Bootstrap process initiated. Your system should be visible at the following location shortly: <link>systems</link>. If any issues arise, you'll receive an error notification. In case you're working with a transactional system, please perform a system reboot to complete the registration process.",
            {
              link: (str) => (
                <a className="js-spa" href="/rhn/manager/systems/list/all">
                  {str}
                </a>
              ),
            }
          )}
        </p>
      );
    } else if (this.state.errors.length > 0) {
      alertMessages = MessagesUtils.error(
        this.state.errors.map((error, index) => (
          <>
            {error.message}{" "}
            {this.hasDetails(error) && (
              <ModalLink
                id={"error-details-" + index}
                text={t("Details")}
                title={t("Show additional details about this error")}
                target="show-error-details"
                className="no-padding"
                onClick={() => this.showErrorDetailsDialog(error)}
              />
            )}
          </>
        )),
        true,
        t("Unable to bootstrap host. The following errors have happened:")
      );
    } else if (this.state.loading) {
      alertMessages = MessagesUtils.info(t("Your system is bootstrapping: waiting for a response.."));
    } else if (this.state.privKeyLoading) {
      alertMessages = MessagesUtils.info(t("Loading SSH Private Key.."));
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

    const authenticationData = (
      <>
        {this.state.authMethod === AuthMethod.Password && (
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Password")}:</label>
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
        )}
        {this.state.authMethod === AuthMethod.SshKey && (
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
        )}
      </>
    );

    return (
      <TopPanel title={t("Bootstrap Minions")} icon="fa fa-rocket" helpUrl="reference/systems/bootstrapping.html">
        <p>
          {t(
            "You can add systems to be managed by providing SSH credentials only. {productName} will prepare the system remotely and will perform the registration.",
            { productName }
          )}
        </p>
        <Messages items={alertMessages} />
        <ErrorDetailsDialog error={this.state.errorDetails} onDialogClose={this.closeErrorDetailsDialog} />
        <div className="form-horizontal">
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Host")}:</label>
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
            <label className="col-md-3 control-label">{t("SSH Port")}:</label>
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
                title={t("Port range: 1 - 65535")}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("User")}:</label>
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
                    "The user will have an effect only during the bootstrap process. Further connections will be made by the user specified in rhn.conf. The default user for the key 'ssh_push_sudo_user' is 'root'. This user is set after {productName}'s SSH key is deployed during the bootstrap procedure.",
                    { productName }
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="form-group">
            <label className="col-md-3 control-label">{t("Authentication Method")}:</label>

            <div className="col-md-6">
              <div className="radio col-md-3">
                <label>
                  <input
                    name="authMethod"
                    type="radio"
                    value={AuthMethod.Password}
                    checked={this.state.authMethod === AuthMethod.Password}
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
                    value={AuthMethod.SshKey}
                    checked={this.state.authMethod === AuthMethod.SshKey}
                    onChange={this.authMethodChanged}
                  />
                  <span>{t("SSH Private Key")}</span>
                </label>
              </div>
              {this.props.ansibleInventoryId && (
                <div className="radio col-md-6">
                  <label>
                    <input
                      name="authMethod"
                      type="radio"
                      value={AuthMethod.AnsiblePreauth}
                      checked={this.state.authMethod === AuthMethod.AnsiblePreauth}
                      onChange={this.authMethodChanged}
                    />
                    <span>{t("Ansible control node")}</span>
                  </label>
                </div>
              )}
            </div>
          </div>
          {authenticationData}

          <div className="form-group">
            <label className="col-md-3 control-label">{t("Activation Key")}:</label>
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
                  .map((k) => (
                    <option key={k} value={k}>
                      {k}
                    </option>
                  ))}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Reactivation Key")}:</label>
            <div className="col-md-6">
              <input
                name="reactivationKey"
                className="form-control"
                type="text"
                placeholder={t("Leave empty when no reactivation is wanted")}
                value={this.state.reactivationKey}
                onChange={this.reactivationKeyChanged}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Proxy")}:</label>
            <div className="col-md-6">
              <select value={this.state.proxy} onChange={this.proxyChanged} className="form-control" name="proxies">
                <option key="none" value="">
                  {t("None")}
                </option>
                {this.props.proxies.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                    {p.path.reduce(
                      (acc, val, idx) => acc + "\u2192 " + val + (idx === p.path.length - 1 ? "" : " "),
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
                  <span>{t("Disable SSH strict host key checking during bootstrap process")}</span>
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
                  <span>{t("Manage system completely via SSH (will not install an agent)")}</span>
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
    window.addEventListener("beforeunload", (e) => {
      if (this.state.loading) {
        var confirmationMessage = t("Are you sure you want to close this page while bootstrapping is in progress ?");
        (e || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
      }
    });
  }
}

export const renderer = (id) => {
  const params = new URLSearchParams(window.location.search);
  const targetHost = params.get("targetHost");
  const ansibleInventoryId = Number.parseInt(params.get("ansibleInventoryId") || "", 10) || null;

  return SpaRenderer.renderNavigationReact(
    <BootstrapMinions
      availableActivationKeys={window.availableActivationKeys}
      proxies={window.proxies}
      ansibleInventoryId={ansibleInventoryId}
      targetHost={targetHost}
    />,
    document.getElementById(id)
  );
};
