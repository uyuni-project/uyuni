'use strict';

var React = require("react")
const ReactDOM = require("react-dom");
var Panel = require("../components/panel").Panel
var PanelButton = require("../components/panel").PanelButton
var Messages = require("../components/messages").Messages
var Network = require("../utils/network");
var Button = require("../components/buttons").Button;
var LinkButton = require("../components/buttons").LinkButton;

var StateDetail = React.createClass({

    _titles: {
        "add": t("Create State"),
        "edit": t("Edit State"),
        "delete": t("Delete State"),
        "info": t("View State")
    },

    getInitialState: function() {
        if (this.props.sls.errors) {
            return {
                errors: this.props.sls.errors
            };
        }
        return {};
    },

    handleCreate: function(e) {
        this._save(e, "POST")
    },

    handleUpdate: function(e) {
        this._save(e, "PUT")
    },

    handleDelete: function(e) {
        var r = confirm(t("Are you sure you want to delete state '{0}' ?", this.props.sls.name));
        if (r == true) {
            this._save(e, "DELETE")
        }
    },

    _save: function(e, httpMethod) {
        var formData = {};
        formData['name'] = React.findDOMNode(this.refs.stateName).value.trim();
        formData['content'] = React.findDOMNode(this.refs.stateContent).value.trim();
        if (this.props.sls.checksum) {
            formData['checksum'] = this.props.sls.checksum;
        }

        var promise = null;
        if (httpMethod == "POST") {
            promise = Network.post(window.location.href, JSON.stringify(formData), "application/json").promise;
        } else if (httpMethod == "PUT") {
            promise = Network.put(window.location.href, JSON.stringify(formData), "application/json").promise;
        } else if (httpMethod == "DELETE") {
            promise = Network.del(window.location.href, JSON.stringify(formData), "application/json").promise;
        }
        if (promise) {
            promise.then(data => {
                console.log(data);
                this.setState({messages: [data.message]});
                window.location.href = data.url;
            },
            (xhr) => {
               if (xhr.status == 400) {
                   // validation err
                   var errs = JSON.parse(xhr.responseText);
                   this.setState({errors: errs});
               } else {
                   this.setState({errors: [t("An internal server error occurred")]});
               }
            });
        }

    },

    render: function() {
        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }

        var buttons = [];
        if (this.props.sls.action == "edit") {
            buttons.push(
              <Button key="save-btn-update" id="save-btn" className="btn-success" icon="fa-floppy-o" text={t("Update")} handler={this.handleUpdate}/>
            );
        } else {
            buttons.push(
                <Button key="save-btn-create" id="save-btn" className="btn-success" icon="fa-floppy-o" text={t("Create State")} handler={this.handleCreate}/>
            );
        }
        buttons.push(
            <LinkButton key="cancel-btn" id="cancel-btn" className="btn-default form-horizontal pull-right" text={t("Cancel")} href="/rhn/manager/state_catalog"/>
        );

        var deleteButton = null;
        if (this.props.sls.action == "edit") {
            deleteButton = <PanelButton id="delete-btn" text={t("Delete State")} icon="fa-trash" handler={this.handleDelete}/>;
        }

        // TODO show readonly if action==delete or info
        return (
        <Panel title={this._titles[this.props.sls.action]} icon="spacewalk-icon-salt-add" button={deleteButton}>
            {errs}
            <form className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">Name<span className="required-form-field">*</span>:</label>
                    <div className="col-md-6">
                        <input className="form-control" type="text" name="name" ref="stateName"
                            defaultValue={this.props.sls.name}/>
                    </div>
                </div>

                <div className="form-group">
                    <label className="col-md-3 control-label">Content<span className="required-form-field">*</span>:</label>
                    <div className="col-md-6">
                        <textarea className="form-control" rows="20" name="content" ref="stateContent"
                            defaultValue={this.props.sls.content}/>
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

ReactDOM.render(
  <StateDetail sls={stateData()}/>,
  document.getElementById('state-details')
);
