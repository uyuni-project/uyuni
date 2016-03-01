'use strict';

var React = require("react");
var Panel = require("../components/panel").Panel;

var Highstate = React.createClass({

    getInitialState: function() {
        var state = {
            "highstate": []
        };
        return state;
    },

    refreshHighstate: function() {
        console.log("Refreshing the highstate...");
        $.get("/rhn/manager/api/states/highstate?sid=" + serverId, data => {
          this.setState({"highstate" : data});
        });
    },

    componentWillMount: function() {
        this.refreshHighstate();
    },

    render: function() {
        return (
            <Panel title="Highstate" icon="spacewalk-icon-virtual-host-manager">
                <textarea className="form-control" rows="20" name="content"
                        value={JSON.stringify(this.state.highstate, undefined, 4)} readOnly="true"/>
            </Panel>
        );
    }
});

React.render(
  <Highstate />,
  document.getElementById('highstate')
);
