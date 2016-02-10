'use strict';

var React = require("react")

var Panel = require("../components/panel").Panel
var Messages = require("../components/messages").Messages

var Button = React.createClass({

    render: function() {
        return (
            <button type="button" className={'btn ' + this.props.className} onClick={this.props.handler}>
                <i className={'fa ' + this.props.icon}/>{this.props.text}
            </button>
        )
    }
})

var StateDetail = React.createClass({

    _titles: {
        "add": t("Add state"),
        "edit": t("Edit state"),
        "delete": t("Delete state"),
        "info": t("View state")
    },

    getInitialState: function() {
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

        $.ajax({
          url: window.location.href + (csrfToken ? "?csrf_token=" + csrfToken : ""),
          dataType: 'json',
          contentType: "application/json",
          type: httpMethod,
          data: JSON.stringify(formData),
          success: function(data) {
            console.log(data)
            this.setState({messages: [data.message]})
            window.location.href = data.url
          }.bind(this),

          error: function(xhr, status, err) {
            if (xhr.status == 400) {
                // validation err
                var errs = $.parseJSON(xhr.responseText);
                this.setState({errors: errs})
            } else if (xhr.status == 500) {
                this.setState({errors: [t("An internal server error occurred")]})
            } else {
                console.error(status, err.toString());
            }
          }.bind(this)
        });

    },

    render: function() {
        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
//            errs = this.state.errors.map( function(e) {
//                    return (<div className="alert alert-danger">{t(e)}</div>)
//                   })

        }

//        if (this.state.messages) {
//            errs = this.state.messages.map( function(e) {
//                    return (<div className="alert alert-info">{t(e)}</div>)
//                   });
//        }

        var buttons = [];
        if (this.props.sls.action == "edit") {
            buttons.push(
                <Button className="btn-success" icon="fa-plus" text={t("Save state")} handler={this.handleUpdate}/>,
                <Button className="btn-danger" icon="fa-trash" text={t("Delete state")} handler={this.handleDelete}/>
            );
        } else {
            buttons.push(
                <Button className="btn-success" icon="fa-plus" text={t("Create state")} handler={this.handleCreate}/>
                );
        }
        // TODO show readonly if action==delete or info
        return (
        <Panel title={this._titles[this.props.sls.action]} icon="spacewalk-icon-virtual-host-manager">
            {errs}
            <form className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">Name:</label>
                    <div className="col-md-6">
                        <input className="form-control" type="text" name="name" ref="stateName"
                            defaultValue={this.props.sls.name}/>
                    </div>
                </div>

                <div className="form-group">
                    <label className="col-md-3 control-label">Content:</label>
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

React.render(
  <StateDetail sls={stateData()}/>,
  document.getElementById('state-details')
);