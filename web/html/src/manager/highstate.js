'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Messages = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const {ActionSchedule} = require("../components/action-schedule");
const AsyncButton = require("../components/buttons").AsyncButton;
const Network = require("../utils/network");
const Panels = require("../components/panel");
const Functions = require("../utils/functions");
const Formats = Functions.Formats;
const {ActionLink, ActionChainLink} = require("../components/links");

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
                earliest: Formats.LocalDateTime(this.state.earliest),
                actionChain: this.state.actionChain ? this.state.actionChain.text : null
            }),
            "application/json"
        ).promise.then(data => {
            const msg = MessagesUtils.info(this.state.actionChain ?
                    <span>{t("Action has been successfully added to the ")}<ActionChainLink id={data}>{this.state.actionChain ? this.state.actionChain.text : ""}</ActionChainLink></span> :
                    <span>{t("Applying the highstate has been ")}<ActionLink id={data}>{t("scheduled.")}</ActionLink></span>);

            const msgs = this.state.messages.concat(msg);

            // Do not spam UI showing old messages
            while (msgs.length > messagesCounterLimit) {
              msgs.shift();
            }

            this.setState({
                messages: msgs
            });
        }).catch(this.handleResponseError);

        return request;
    },

    handleResponseError: function(jqXHR) {
      this.setState({
           messages: Network.responseErrorMessage(jqXHR)
      });
    },

    onDateTimeChanged: function(date) {
        this.setState({"earliest": date});
    },

    onActionChainChanged: function(actionChain) {
        this.setState({actionChain: actionChain})
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
        const buttons = [ <AsyncButton action={this.applyHighstate} defaultType="btn-success" name={t("Apply Highstate")} disabled={minions.length === 0} /> ];
        return (
            <div>
                {messages}
                <InnerPanel title={t("Highstate")} icon="spacewalk-icon-salt" buttons={buttons} >

                <ActionSchedule timezone={timezone} localTime={localTime}
                   earliest={this.state.earliest}
                   actionChains={actionChains}
                   actionChain={this.state.actionChain}
                   onActionChainChanged={this.onActionChainChanged}
                   onDateTimeChanged={this.onDateTimeChanged}/>

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
