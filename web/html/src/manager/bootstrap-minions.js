'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {AsyncButton, LinkButton} = require("../components/buttons");

class BootstrapMinions extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            host: "",
            port: "",
            user: "",
            password: "",
            activationKey: "",
            ignoreHostKeys: true,
            manageWithSSH: false,
            messages: []
        };
        ["hostChanged", "portChanged", "userChanged", "passwordChanged", "onBootstrap", "ignoreHostKeysChanged", "manageWithSSHChanged", "activationKeyChanged", "clearFields"]
            .forEach(method => this[method] = this[method].bind(this));
    }

    hostChanged(event) {
        this.setState({
            host: event.target.value
        });
    }

    portChanged(event) {
        this.setState({
            port: event.target.value
        });
    }

    userChanged(event) {
        this.setState({
            user: event.target.value
        });
    }

    passwordChanged(event) {
        this.setState({
            password: event.target.value
        });
    }

    ignoreHostKeysChanged(event) {
        this.setState({
            ignoreHostKeys: event.target.checked
        });
    }

    manageWithSSHChanged(event) {
        this.setState({
            manageWithSSH: event.target.checked
        });
    }

    activationKeyChanged(event) {
        this.setState({
            activationKey: event.target.value
        });
    }

    onBootstrap() {
        var formData = {};
        formData['host'] = this.state.host.trim();
        formData['port'] = this.state.port.trim();
        formData['user'] = this.state.user.trim() === "" ? undefined : this.state.user.trim();
        formData['password'] = this.state.password.trim();
        formData['activationKeys'] = this.state.activationKey === "" ? [] : [this.state.activationKey] ;
        formData['ignoreHostKeys'] = this.state.ignoreHostKeys;

        const request = Network.post(
            this.state.manageWithSSH ? "/rhn/manager/api/minions/bootstrapSSH" : "/rhn/manager/api/minions/bootstrap",
            JSON.stringify(formData),
            "application/json"
        ).promise.then(data => {
            this.setState({
                success: data.success,
                messages: data.messages
            });
        }, (xhr) => {
            try {
                this.setState({
                    success: false,
                    messages: [JSON.parse(xhr.responseText)]
                })
            } catch (err) {
                this.setState({
                    success: false,
                    messages: [Network.errorMessageByStatus(xhr.status)]
                })
            }
        });
        return request;
    }

    clearFields() {
      this.setState({
          host: "",
          port: "",
          user: "",
          password: "",
          activationKey: "",
          ignoreHostKeys: true,
          manageWithSSH: false,
          messages: []
      });
      return;
    }

    render() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>{t(': You can add systems to be managed by providing SSH credentials only. SUSE Manager will take care of installing the required software agent (salt-minion) remotely and perform the registration. We would be glad to receive your feedback via the ')} <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.success) {
            messages = <Messages items={[{severity: "success", text:
                <p>{t('Successfully bootstrapped host! Your system should appear in ')}<a href="/rhn/systems/Overview.do">{t('System Overview')}</a>{t(' shortly')}.</p>
            }]}/>;
        } else if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "error", text: msg};
            })}/>;
        }

        var buttons = [
            <AsyncButton id="bootstrap-btn" defaultType="btn-success" icon="plus" name={t("Bootstrap it")} action={this.onBootstrap}/>,
            <AsyncButton id="clear-btn" defaultType="btn-default pull-right" icon="eraser" name={t("Clear fields")} action={this.clearFields}/>
        ];

        return (
        <Panel title={t("Bootstrap Minions")} icon="fa fa-rocket">
            {messages}
            <div className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">Host:</label>
                    <div className="col-md-6">
                        <input name="hostname" className="form-control" type="text" placeholder={t("e.g., host.domain.com")} value={this.state.host} onChange={this.hostChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">SSH Port:</label>
                    <div className="col-md-6">
                        <input name="port" className="form-control numeric set-maxlength-width" type="text" maxLength="5"
                          placeholder="22" onChange={this.portChanged} onKeyPress={numericValidate} value={this.state.port}
                          title={t('Port range: 1 - 65535')}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">User:</label>
                    <div className="col-md-6">
                        <input name="user" className="form-control" type="text" placeholder="root" value={this.state.user} onChange={this.userChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">Password:</label>
                    <div className="col-md-6">
                        <input name="password" className="form-control" type="password" placeholder={t("e.g., ••••••••••••")} value={this.state.password} onChange={this.passwordChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">Activation Key:</label>
                    <div className="col-md-6">
                       <select value={this.state.activationKey} onChange={this.activationKeyChanged} className="form-control" name="activationKeys">
                         <option key="none" value="">None</option>
                         {
                             this.props.availableActivationKeys.map(k =>
                                <option key={k} value={k}>{ k }</option>
                             )
                         }
                       </select>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-md-3"></div>
                    <div className="col-md-6">
                        <div className="checkbox">
                            <label>
                                <input name="ignoreHostKeys" type="checkbox" checked={this.state.ignoreHostKeys} onChange={this.ignoreHostKeysChanged}/>
                                <span>Disable strict host key checking</span>
                            </label>
                        </div>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-md-3"></div>
                    <div className="col-md-6">
                        <div className="checkbox">
                            <label>
                                <input name="manageWithSSH" type="checkbox" checked={this.state.manageWithSSH} onChange={this.manageWithSSHChanged}/>
                                <span>Manage system completely via SSH (will not install an agent)</span>
                            </label>
                        </div>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-md-offset-3 col-md-6">
                        {buttons}
                    </div>
                </div>
            </div>
        </Panel>
        )
    }
}

ReactDOM.render(
    <BootstrapMinions availableActivationKeys={availableActivationKeys} />,
    document.getElementById('bootstrap-minions')
);
