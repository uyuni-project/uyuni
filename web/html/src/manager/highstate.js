'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("../components/messages").Messages;
const DateTimePicker = require("../components/datetimepicker").DateTimePicker;
const AsyncButton = require("../components/buttons").AsyncButton;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Formats = Functions.Formats;

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

var Highstate = React.createClass({

    getInitialState: function() {
        var state = {
            messages: [],
            earliest: Functions.Utils.dateWithTimezone(localTime)
        };
        return state;
    },

    refreshHighstate: function() {
        Network.get("/rhn/manager/api/states/highstate?sid=" + serverId).promise.then(data => {
            this.setState({"highstate": data});
        });
    },

    componentWillMount: function() {
        this.refreshHighstate();
    },

    applyHighstate: function() {
        const request = Network.post(
            "/rhn/manager/api/states/apply",
            JSON.stringify({
                id: serverId,
                type: "SERVER",
                states: [],
                earliest: Formats.LocalDateTime(this.state.earliest)
            }),
            "application/json"
        ).promise.then(data => {
            this.state.messages.push(msg('info', <span>{t("Applying the highstate has been ")}
                    <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
                    {t(".")}</span>))
            this.setState({
                messages: this.state.messages
            });
        });
        return request;
    },

    onDateTimeChanged: function(date) {
        this.setState({"earliest": date});
    },

    render: function() {
        const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages}/> : null;
        return (
            <span>
                {messages}
                <div className="spacewalk-section-toolbar">
                    <div className="action-button-wrapper">
                        <AsyncButton action={this.applyHighstate} name={t("Apply Highstate")} />
                    </div>
                </div>
                <div className="spacewalk-scheduler">
                    <div className="form-horizontal">
                        <div className="form-group">
                            <label className="col-md-3 control-label">
                                {t("Schedule no sooner than:")}
                            </label>
                            <div className="col-md-6">
                                <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={timezone} />
                            </div>
                        </div>
                    </div>
                </div>
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>{t("Highstate for ")}{serverName}</h4>
                    </div>
                    <div className="panel-body">
                        { this.state.highstate ?
                            <pre>
                                {this.state.highstate}
                            </pre>
                            : <span>Retrieving highstate data...</span>
                        }
                    </div>
                </div>
            </span>
        );
    }
});

ReactDOM.render(
    <Highstate />,
    document.getElementById('highstate')
);
