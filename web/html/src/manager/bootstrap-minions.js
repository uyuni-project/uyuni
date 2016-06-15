'use strict';

var React = require("react");

var Panel = require("../components/panel").Panel;
var Messages = require("../components/messages").Messages;
var Network = require("../utils/network");
var Button = require("../components/buttons").Button;
var LinkButton = require("../components/buttons").LinkButton;

var BootstrapMinions = React.createClass({

    getInitialState: function() {
        return {
            host: "my.future.minion",
            user: "root",
            password: "linux"
        };
    },

    hostChanged: function(event) {
        this.setState({
            host: event.target.value
        });
    },

    userChanged: function(event) {
        this.setState({
            user: event.target.value
        });
    },

    passwordChanged: function(event) {
        this.setState({
            password: event.target.value
        });
    },

    onBootstrap: function(event) {
        var formData = {};
        formData['host'] = this.state.host.trim();
        formData['user'] = this.state.user.trim();
        formData['password'] = this.state.password.trim();

        var promise = Network.post(window.location.href, JSON.stringify(formData), "application/json").promise;
        if (promise) {
            promise.then(data => {
                this.setState({
                    success: true,
                    errors: null
                });
            },
            (xhr) => {
               if (xhr.status == 400) {
                   // validation err
                   var errs = JSON.parse(xhr.responseText);
                   this.setState({errors: errs});
               } else {
                   this.setState({errors: [t("An error occurred")]});
               }
            });
        }
    },

    render: function() {
        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(error) {
                return {severity: "error", text: error};
            })}/>;
        }

        var msg = null;
        if (this.state.success) {
            msg = <Messages items={[{severity: "info", text: t("Successfully bootstrapped minion!")}]}/>;
        }

        var buttons = [];
        buttons.push(
            <Button id="bootstrap-btn" className="btn-success" icon="fa-plus" text={t("Bootstrap")} handler={this.onBootstrap}/>
        );
        buttons.push(
            <LinkButton id="cancel-btn" className="btn-default form-horizontal pull-right" text={t("Back to System Overview")} href="/rhn/systems/Overview.do"/>
        );

        return (
        <Panel title={t("Bootstrap Minions")} icon="spacewalk-icon-salt-add">
            {errs}
            {msg}
            <form className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">Host:</label>
                    <div className="col-md-6">
                        <input name="hostname" className="form-control" type="text" defaultValue={this.state.host} onChange={this.hostChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">User:</label>
                    <div className="col-md-6">
                        <input name="user" className="form-control" type="text" defaultValue={this.state.user} onChange={this.userChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <label className="col-md-3 control-label">Password:</label>
                    <div className="col-md-6">
                        <input name="password" className="form-control" type="password" defaultValue={this.state.password} onChange={this.passwordChanged}/>
                    </div>
                </div>
                <div className="form-group">
                    <div className="col-md-offset-3 col-md-6">
                        {buttons}
                    </div>
                </div>
            </form>
        </Panel>
        )
    }
});

React.render(
  <BootstrapMinions />,
  document.getElementById('bootstrap-minions')
);
