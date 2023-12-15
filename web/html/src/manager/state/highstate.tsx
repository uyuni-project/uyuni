import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { ActionSchedule } from "components/action-schedule";
import { LinkButton } from "components/buttons";
import { AsyncButton } from "components/buttons";
import { ActionChainLink, ActionLink } from "components/links";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { InnerPanel } from "components/panels/InnerPanel";
import { Toggler } from "components/toggler";

import { localizedMoment } from "utils";
import Network from "utils/network";

import { DisplayHighstate } from "./display-highstate";

// See java/code/src/com/suse/manager/webui/templates/groups/highstate.jade
declare global {
  interface Window {
    minions?: any[];
    entityType?: any;
    groupName?: any;
    timezone?: any;
    localTime?: any;
    actionChains?: any;
  }
}

const messagesCounterLimit = 3;

type HighstateProps = {};

type HighstateState = {
  messages: any[];
  earliest: moment.Moment;
  test: boolean;
  actionChain?: any;
};

class Highstate extends React.Component<HighstateProps, HighstateState> {
  constructor(props) {
    super(props);
    var state = {
      messages: [],
      earliest: localizedMoment(),
      test: false,
    };
    this.state = state;
  }

  applyHighstate = () => {
    const request = Network.post("/rhn/manager/api/states/applyall", {
      ids: window.minions?.map((m) => m.id),
      earliest: this.state.earliest,
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
      test: this.state.test,
    })
      .then((data) => {
        const msg = MessagesUtils.info(
          this.state.actionChain ? (
            <span>
              {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
                name: this.state.actionChain.text,
                link: (str) => <ActionChainLink id={data}>{str}</ActionChainLink>,
              })}
            </span>
          ) : (
            <span>
              {t("Applying the highstate has been <link>scheduled</link>.", {
                link: (str) => <ActionLink id={data}>{str}</ActionLink>,
              })}
            </span>
          )
        );

        const msgs = this.state.messages.concat(msg);

        // Do not spam UI showing old messages
        while (msgs.length > messagesCounterLimit) {
          msgs.shift();
        }

        this.setState({
          messages: msgs,
        });
      })
      .catch(this.handleResponseError);

    return request;
  };

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };

  onActionChainChanged = (actionChain) => {
    this.setState({ actionChain: actionChain });
  };

  toggleTestState = () => {
    this.setState({ test: !this.state.test });
  };

  isSSM = () => {
    return window.entityType === "SSM" ? true : false;
  };

  render() {
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;
    const buttons = [
      <div className="btn-group pull-right">
        <Toggler
          text={t("Test mode")}
          value={this.state.test}
          className="btn"
          handler={this.toggleTestState.bind(this)}
        />
        <AsyncButton
          action={this.applyHighstate}
          defaultType="btn-success"
          text={t("Apply Highstate")}
          disabled={window.minions?.length === 0}
        />
      </div>,
    ];

    const loc = window.location;
    const createLink = loc.pathname.replace("/highstate", "/recurring-actions") + loc.search + "#/create";
    const buttonsLeft = [
      <LinkButton icon="fa-plus" href={createLink} className="btn-default" text={t("Create Recurring")} />,
    ];
    const showHighstate = [
      <InnerPanel
        title={t("Highstate")}
        icon="spacewalk-icon-salt"
        buttons={buttons}
        buttonsLeft={this.isSSM() ? undefined : buttonsLeft}
      >
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>Apply Highstate</h3>
            </div>
          </div>
          <div className="panel-body">
            <ActionSchedule
              earliest={this.state.earliest}
              actionChains={window.actionChains}
              onActionChainChanged={this.onActionChainChanged}
              onDateTimeChanged={this.onDateTimeChanged}
              systemIds={window.minions?.map((m) => m.id)}
              actionType="states.apply"
            />
          </div>
        </div>
        <DisplayHighstate />
      </InnerPanel>,
    ];
    return (
      <div>
        {messages}
        {showHighstate}
      </div>
    );
  }
}

export const renderer = () => SpaRenderer.renderNavigationReact(<Highstate />, document.getElementById("highstate"));
