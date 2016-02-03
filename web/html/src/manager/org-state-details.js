'use strict';

var React = require("react")
var Panel = require("../components/panel.js").Panel

var StateDetail = React.createClass({

//    getInitialState: function() {
//        return this.props.data;
//    },

    handleSubmit: function(e) {
        e.preventDefault();
        var formData = {name: }
        $.ajax({
          url: "/manager/state_catalog/create",
          dataType: 'json',
          type: 'POST',
          data: formData,
          success: function(data) {
            this.setState({data: data});
          }.bind(this),
          error: function(xhr, status, err) {
            console.error(this.props.url, status, err.toString());
          }.bind(this)
        });

    }

    render: function() {
        return (
        <Panel title="Add state" icon="spacewalk-icon-virtual-host-manager">
            <form className="form-horizontal" onSubmit="{this.handleSubmit}">
                <div className="form-group">
                    <label className="col-md-3 control-label">Name:</label>
                    <div className="col-md-6">
                        <input className="form-control" type="text" name="name"/>
                    </div>
                </div>

                <div className="form-group">
                    <label className="col-md-3 control-label">Content:</label>
                    <div className="col-md-6">
                        <textarea className="form-control" name="content"/>
                    </div>
                </div>

                <div className="form-group">
                    <div className="col-md-offset-3 col-md-6">
                        <button className="btn btn-success" type="submit">
                            <i className="fa fa-plus"/>{t("Add state")}
                        </button>
                    </div>
                </div>

            </form>
        </Panel>
        )

    }
});

React.render(
  <StateDetail data={formData()}/>,
  document.getElementById('state-add')
);