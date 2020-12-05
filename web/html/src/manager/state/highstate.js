/* eslint-disable */
'use strict';

import {LinkButton} from "components/buttons";

import React from 'react';
import ReactDOM from 'react-dom';
import { DisplayHighstate } from './display-highstate';
import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';
import { ActionSchedule } from 'components/action-schedule';
import { AsyncButton } from 'components/buttons';
import { Toggler } from 'components/toggler';
import Network from 'utils/network';
import { InnerPanel } from 'components/panels/InnerPanel';
import { RecurringStatesEdit } from './recurring-states-edit';
import Functions from 'utils/functions';
const Formats = Functions.Formats;
import { ActionLink, ActionChainLink } from 'components/links';
import SpaRenderer from 'core/spa/spa-renderer';

const messagesCounterLimit = 3;

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

class Highstate extends React.Component {
    constructor(props) {
        super(props);
        var state = {
            messages: [],
            earliest: Functions.Utils.dateWithTimezone(localTime),
            test: false
        };
        this.state = state;
    }

    applyHighstate = () => {
        const request = Network.post(
            "/rhn/manager/api/states/applyall",
            JSON.stringify({
                ids: minions.map(m => m.id),
                earliest: Formats.LocalDateTime(this.state.earliest),
                actionChain: this.state.actionChain ? this.state.actionChain.text : null,
                test: this.state.test
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
    };

    handleResponseError = (jqXHR) => {
      this.setState({
           messages: Network.responseErrorMessage(jqXHR)
      });
    };

    onDateTimeChanged = (date) => {
        this.setState({"earliest": date});
    };

    onActionChainChanged = (actionChain) => {
        this.setState({actionChain: actionChain})
    };

    toggleTestState = () => {
        this.setState({test: !this.state.test})
    };

    isSSM = () => {
        return window.entityType === "SSM" ? true : false;
    };

    render() {
        const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages}/> : null;
        const buttons = [
            <div className="btn-group pull-right">
                <Toggler text={t('Test mode')} value={this.state.test} className="btn" handler={this.toggleTestState.bind(this)} />
                <AsyncButton action={this.applyHighstate} defaultType="btn-success" text={t("Apply Highstate")} disabled={minions.length === 0} />
            </div>
        ];

        const loc = window.location;
        const createLink = loc.pathname.replace("/highstate", "/recurring-states") +
              loc.search + "#/create";
        const buttonsLeft = [
            <LinkButton
                icon="fa-plus"
                href={createLink}
                className="btn-default"
                text={t("Create Recurring")}/>
        ];
        const showHighstate = [
            <InnerPanel title={t("Highstate")} icon="spacewalk-icon-salt" buttons={buttons} buttonsLeft={this.isSSM() ? undefined : buttonsLeft}>
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <div>
                            <h3>Apply Highstate</h3>
                        </div>
                    </div>
                    <div className="panel-body">
                        <ActionSchedule timezone={timezone} localTime={localTime}
                                        earliest={this.state.earliest}
                                        actionChains={actionChains}
                                        actionChain={this.state.actionChain}
                                        onActionChainChanged={this.onActionChainChanged}
                                        onDateTimeChanged={this.onDateTimeChanged}
                                        systemIds={window.minions.map(m => m.id)}
                                        actionType="states.apply"/>
                    </div>
                </div>
                <DisplayHighstate />
            </InnerPanel>
        ];
        return (
            <div>
                {messages}
                {showHighstate}
            </div>
        );
    }
}

export const renderer = () => SpaRenderer.renderNavigationReact(
    <Highstate />,
    document.getElementById('highstate')
);
