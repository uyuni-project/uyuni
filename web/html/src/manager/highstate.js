'use strict';

const React = require("react");
const Messages = require("../components/messages").Messages;
const DateTimePicker = require("../components/datetimepicker").DateTimePicker;
const AsyncButton = require("../components/buttons").AsyncButton;

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

var Highstate = React.createClass({

    getInitialState: function() {
        var state = {
            "highstate": [],
            "messages": [],
            "earliest": new Date()
        };
        return state;
    },

    refreshHighstate: function() {
        $.get("/rhn/manager/api/states/highstate?sid=" + serverId, data => {
            this.setState({"highstate": data});
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
                states: [],
                earliest: this.state.earliest
            }),
            contentType: "application/json",
            dataType: "json"
        })
        .done(data => {
            this.state.messages.push(msg('info', <span>{t("Applying the highstate has been ")}
                    <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
                    {t(".")}</span>))
            this.setState({
                messages: this.state.messages
            });
        });
        return Promise.resolve(request);
    },

    onDateTimeChanged: function(date) {
        this.setState({"earliest": date});
    },

    render: function() {
        const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages}/> : null;
        return (
            <span>
            {messages}
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4>{t("Show and apply the highstate for ")}{serverName}</h4>
                </div>
                <div className="panel-body">
                    <div className="form-horizontal">
                        <div className="form-group">
                            <label className="col-md-3 control-label">
                                {t("Highstate data:")}
                            </label>
                            <div className="col-md-6">
                                <textarea className="form-control" rows="20" value={this.state.highstate} readOnly="true" />
                            </div>
                        </div>
                        <div className="form-group">
                            <label className="col-md-3 control-label">
                                {t("Schedule no sooner than:")}
                            </label>
                            <div className="col-md-6">
                                <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={timezone} />
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-md-offset-3 col-md-6">
                                <AsyncButton action={this.applyHighstate} name={t("Apply Highstate")} />
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
