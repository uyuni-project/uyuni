'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("../components/messages").Messages;
const DateTimePicker = require("../components/datetimepicker").DateTimePicker;
const AsyncButton = require("../components/buttons").AsyncButton;
const Network = require("../utils/network");
const Panels = require("../components/panel");
const Functions = require("../utils/functions");
const Formats = Functions.Formats;

const messagesCounterLimit = 3;
const InnerPanel = Panels.InnerPanel;

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

function requestHighstate(id) {
    return Network.get("/rhn/manager/api/states/highstate?sid=" + id).promise;
}

var MinionHighstateSingle = React.createClass({
    getInitialState: function() {
        return {};
    },

    getHighstate: function() {
        requestHighstate(this.props.data.id).then(data => {
            this.setState({highstate: data});
        });
    },

    componentWillMount: function() {
        this.getHighstate();
    },

    render: function() {
        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4>{t("Highstate for {0}", this.props.data.name)}</h4>
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
        );
    }
});

var MinionHighstate = React.createClass({
    getInitialState: function() {
        return {show: false};
    },

    getHighstate: function() {
        if(this.state.loading) return;
        this.setState({loading: true});

        requestHighstate(this.props.data.id).then(data => {
            this.setState({highstate: data});
        });
    },

    componentDidMount: function() {
        if(this.state.show) {
            this.getHighstate();
        }
    },

    expand: function() {
        this.getHighstate();
        this.setState({show: !this.state.show});
    },

    render: function() {
        return (
            <div className="panel panel-default">
                <div className="panel-heading" onClick={this.expand} style={{cursor: "pointer"}}>
                    <span>
                        {this.props.data.name}
                    </span>
                    <div className="pull-right">
                        {this.state.show
                            ? <i className="fa fa-right fa-chevron-up fa-1-5x"/>
                            : <i className="fa fa-right fa-chevron-down fa-1-5x"/>
                        }
                    </div>
                </div>
                { this.state.show &&
                <div className="panel-body">
                    { this.state.highstate ?
                        <pre>
                            {this.state.highstate}
                        </pre>
                        : <span>Retrieving highstate data...</span>
                    }
                </div>
                }
            </div>
        );
    }
});

var Highstate = React.createClass({

    getInitialState: function() {
        var state = {
            messages: [],
            earliest: Functions.Utils.dateWithTimezone(localTime)
        };
        return state;
    },

    applyHighstate: function() {
        const request = Network.post(
            "/rhn/manager/api/states/applyall",
            JSON.stringify({
                ids: minions.map(m => m.id),
                earliest: Formats.LocalDateTime(this.state.earliest)
            }),
            "application/json"
        ).promise.then(data => {
            this.state.messages.push(msg('info', <span>{t("Applying the highstate has been ")}
                    <a href={"/rhn/schedule/ActionDetails.do?aid=" + data}>{t("scheduled")}</a>
                    {t(".")}</span>))

            // Do not spam UI showing old messages
            while (this.state.messages.length > messagesCounterLimit) {
              this.state.messages.shift();
            }

            this.setState({
                messages: this.state.messages
            });
        });
        return request;
    },

    onDateTimeChanged: function(date) {
        this.setState({"earliest": date});
    },

    renderMinions: function() {
        const minionList = [];
        for(var system of minions) {
            minionList.push(<MinionHighstate data={system}/>);
        }
        return minionList;
    },

    render: function() {
        const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages}/> : null;
        const buttons = [ <AsyncButton action={this.applyHighstate} name={t("Apply Highstate")} disabled={minions.length === 0} /> ];
        return (
            <div>
                {messages}
                <InnerPanel title={t("Highstate")} icon="spacewalk-icon-salt" buttons={buttons} >
                <div className="spacewalk-scheduler">
                    <div className="form-horizontal">
                        <div className="form-group">
                            <label className="col-md-3 control-label">
                                {t("Earliest:")}
                            </label>
                            <div className="col-md-6">
                                <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={timezone} />
                            </div>
                        </div>
                    </div>
                </div>

                { minions.length === 1 ?
                    <MinionHighstateSingle data={minions[0]}/>
                    : <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Target Systems ({minions.length})</h4>
                        </div>
                        { minions.length === 0 ?
                        <div className="panel-body">
                            {t("There are no applicable systems.")}
                        </div>
                        :
                        <div className="panel-body" style={{paddingBottom: 0}}>
                            {this.renderMinions()}
                        </div>
                        }
                    </div>
                }
              </InnerPanel>
            </div>
        );
    }
});

ReactDOM.render(
    <Highstate />,
    document.getElementById('highstate')
);
