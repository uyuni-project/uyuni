import * as React from "react";

import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton } from "components/buttons";
import { ActionChainLink, ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { SystemData, TargetSystems } from "components/target-systems";

import { LocalizedMoment, localizedMoment } from "utils/datetime";
import Network from "utils/network";

type Props = {
  systemSupport: Array<SystemData>;
  actionChains: Array<ActionChain>;
};

type State = {
  messages: MessageType[];
  earliest: LocalizedMoment;
  actionChain?: ActionChain;
};

class CoCoSSMSchedule extends React.Component<Props, State> {
  public constructor(props) {
    super(props);

    this.state = {
      messages: [],
      earliest: localizedMoment(),
    };
  }

  onActionChainChanged = (actionChain) => {
    this.setState({ actionChain: actionChain });
  };

  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };

  onSchedule = () => {
    const request = {
      serverIds: this.props.systemSupport.filter((system) => system.cocoSupport).map((system) => system.id),
      actionType: "coco.attestation",
      earliest: this.state.earliest,
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
    };

    Network.post(`/rhn/manager/api/systems/coco/scheduleAction`, request)
      .then((data) => {
        // Notify the successful outcome
        const msg = MessagesUtils.info(
          request.actionChain ? (
            <span>
              {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
                name: request.actionChain,
                link: (str) => <ActionChainLink id={data}>{str}</ActionChainLink>,
              })}
            </span>
          ) : (
            <span>
              {t("The action has been <link>scheduled</link>.", {
                link: (str) => <ActionLink id={data}>{str}</ActionLink>,
              })}
            </span>
          )
        );

        this.setState({ messages: msg });
      })
      .catch((err) => {
        this.setState({
          messages: MessagesUtils.error(
            t("Unable to schedule action. Please check the server logs for detailed information.")
          ),
        });
      });
  };

  render(): React.ReactNode {
    return (
      <>
        <TopPanel title="Confidential Computing Schedule">
          <Messages items={this.state.messages} />
          <SectionToolbar>
            <div className="action-button-wrapper">
              <div className="btn-group pull-right">
                <AsyncButton
                  className="btn-default"
                  defaultType="btn-danger"
                  text={t("Schedule")}
                  action={this.onSchedule}
                />
              </div>
            </div>
          </SectionToolbar>
          <ActionSchedule
            earliest={this.state.earliest}
            actionChains={this.props.actionChains}
            onActionChainChanged={this.onActionChainChanged}
            onDateTimeChanged={this.onDateTimeChanged}
            systemIds={this.props.systemSupport.filter((system) => system.cocoSupport).map((system) => system.id)}
            actionType="coco.attestation"
          />
        </TopPanel>
        <TargetSystems systemsData={this.props.systemSupport}>
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey="cocoSupport"
            header={t("Confidential Computing Capability")}
            cell={(system: SystemData) => (system.cocoSupport ? t("Yes") : t("No"))}
          />
        </TargetSystems>
      </>
    );
  }
}

export default CoCoSSMSchedule;
