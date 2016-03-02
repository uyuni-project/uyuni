'use strict';

const React = require("react");
const Messages = require("../components/messages").Messages;
const AsyncButton = require("../components/buttons").AsyncButton;

function msg(severityIn, textIn) {
    return [{severity: severityIn, text: textIn}];
}

var Highstate = React.createClass({

    getInitialState: function() {
        var state = {
            "highstate": [],
            "messages": null
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

    applyHighstate: function() {
      const request = $.ajax({
          type: "POST",
          url: "/rhn/manager/api/states/apply",
          data: JSON.stringify({
              sid: serverId,
              states: []
          }),
          contentType: "application/json",
          dataType: "json"
      })
      .done( data => {
          console.log("state.apply action scheduled:" + data)
          this.setState({
              messages: msg('info', <span>{t("Applying the highstate has been ")}
                  <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
                  {t(".")}
              </span>)
          });
      });
      return Promise.resolve(request);
    },

    render: function() {
        const messages = this.state.messages ? <Messages items={this.state.messages}/> : null;
        return (
            <span>
            {messages}
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4>Show and apply the highstate for {serverName}</h4>
                </div>
                <div className="panel-body">
                    <div className="form-horizontal">
                        <div className="form-group">
                            <div className="col-md-offset-3 col-md-6">
                                <textarea className="form-control" rows="20"
                                    value={this.state.highstate} readOnly="true" />
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-md-offset-3 col-md-6">
                                <AsyncButton action={this.applyHighstate} name={t("Apply")} />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            </span>
        );
    }
});

React.render(
  <Highstate />,
  document.getElementById('highstate')
);
