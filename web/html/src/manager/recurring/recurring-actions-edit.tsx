import * as React from "react";

import _isEqual from "lodash/isEqual";

import { RecurringPlaybookPicker } from "manager/recurring/recurring-playbook-picker";

import { AsyncButton, Button } from "components/buttons";
import { DEPRECATED_Select, Form } from "components/input";
import { Utils as MessagesUtils } from "components/messages/messages";
import { InnerPanel } from "components/panels/InnerPanel";
import { RecurringEventPicker } from "components/picker/recurring-event-picker";
import { StatesPicker } from "components/states-picker";
import { Toggler } from "components/toggler";

import Network from "utils/network";

import { DisplayHighstate } from "../state/display-highstate";

type Props = {
  schedule?: any;
  onActionChanged: (arg0: any) => any;
  onSetMessages: (arg0: any) => any;
  onError: (arg0: any) => any;
};

type State = {
  minions?: any[];
  active: boolean;
  targetId?: any;
  recurringActionId?: any;
  scheduleName?: any;
  type?: any;
  targetType?: any;
  actionTypeDescription?: any;
  cron?: any;
  details?: any;
};

class RecurringActionsEdit extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    this.state = {
      minions: window.minions,
      active: true,
      details: {},
    };

    if (this.isEdit()) {
      this.setSchedule(this.props.schedule);
    } else {
      this.getTargetType();
    }
  }

  componentDidMount(): void {
    if (this.props.schedule && this.props.schedule.recurringActionId) {
      this.getDetailsData();
    }
  }

  componentDidUpdate(prevProps: Readonly<Props>): void {
    if (!_isEqual(prevProps.schedule, this.props.schedule)) {
      this.getDetailsData();
    }
  }

  getDetailsData(): void {
    Network.get(`/rhn/manager/api/recurringactions/${this.props.schedule.recurringActionId}/details`)
      .then((details) => {
        this.setState({ details });
      })
      .catch(this.props.onError);
  }

  updateSchedule = (schedule) => {
    return Network.post("/rhn/manager/api/recurringactions/save", schedule)
      .then(() => {
        const successMsg = (
          <span>{this.isEdit() ? t("Schedule successfully updated.") : t("Schedule successfully created.")}</span>
        );
        this.props.onSetMessages(MessagesUtils.info(successMsg));
        this.props.onActionChanged("back");
      })
      .catch(this.props.onError);
  };

  executeCustom = (schedule) => {
    return Network.post("/rhn/manager/api/recurringactions/custom/execute", schedule)
      .then(() => {
        const successMsg = <span>{t("Action scheduled on selected minions")}</span>;
        this.props.onSetMessages(MessagesUtils.info(successMsg));
      })
      .catch(this.props.onError);
  };

  matchUrl = (target?: string) => {
    const id = this.state.recurringActionId;
    return "/rhn/manager/api/recurringactions/states?" + (id ? "id=" + id : "") + (target ? "&target=" + target : "");
  };

  setSchedule = (schedule) => {
    Object.assign(this.state, schedule);
  };

  getActionTypeFromString = (actionType: string) => {
    if (actionType === "Ansible Playbook") {
      actionType = "Playbook";
    }
    return actionType.replace(/\s+/g, "").toUpperCase();
  };

  getTargetType = () => {
    if (window.entityType === "GROUP") {
      Object.assign(this.state, {
        targetType: window.entityType,
        targetId: window.groupId,
      });
    } else if (window.entityType === "ORG") {
      Object.assign(this.state, {
        targetType: window.entityType,
        targetId: window.orgId,
      });
    } else if (window.entityType === "MINION") {
      Object.assign(this.state, {
        targetType: window.entityType,
        targetId: window.minions?.[0].id,
      });
    }
  };

  getTypes = () => {
    let types = ["Highstate", "Custom state"];
    if (window.isControlNode) {
      types = types.concat("Ansible Playbook");
    }
    return types;
  };

  isEdit = () => {
    return this.props.schedule ? true : false;
  };

  onEdit = () => {
    return this.updateSchedule({
      targetId: this.state.targetId,
      recurringActionId: this.state.recurringActionId,
      scheduleName: this.state.scheduleName,
      active: this.state.active,
      targetType: this.state.targetType,
      cron: this.state.cron,
      details: this.state.details,
      actionType: this.getActionTypeFromString(this.state.actionTypeDescription),
    });
  };

  onClickExecute = (items) => {
    return this.executeCustom({
      details: this.state.details,
      memberIds: items,
    });
  };

  onActionTypeChanged = (model) => {
    this.setState({ actionTypeDescription: model.actionTypeDescription });
  };

  onScheduleNameChanged = (scheduleName) => {
    this.setState({ scheduleName: scheduleName });
  };

  onToggleActive = (active) => {
    this.setState({ active: active });
  };

  onTypeChanged = (type) => {
    const { details } = this.state;
    details.type = type;
    this.setState({ details });
  };

  onCronTimesChanged = (cronTimes) => {
    const { details } = this.state;
    details.cronTimes = cronTimes;
    this.setState({ details });
  };

  onCustomCronChanged = (cron) => {
    this.setState({ cron: cron });
  };

  onSaveStates = (states) => {
    const { details } = this.state;
    details.states = states;
    this.setState({ details });
    return Promise.resolve(states);
  };

  onSelectPlaybook = (playbook) => {
    this.setState({
      details: {
        ...this.state.details,
        playbookPath: playbook?.playbookPath,
        inventoryPath: playbook?.inventoryPath,
        flushCache: playbook?.flushCache,
      },
    });
  };

  toggleTestState = () => {
    const { details } = this.state;
    details.test = !this.state.details.test;
    this.setState({ details });
  };

  render() {
    if (!this.state.details.type && this.isEdit()) {
      return false;
    }
    const buttons = [
      <div className="btn-group pull-right">
        <Toggler
          text={t("Test mode")}
          value={this.state.details.test}
          className="btn"
          handler={this.toggleTestState.bind(this)}
        />
        <AsyncButton
          action={this.onEdit}
          defaultType="btn-primary"
          text={(this.isEdit() ? t("Update ") : t("Create ")) + t("Schedule")}
        />
      </div>,
    ];
    const buttonsLeft = [
      <div className="btn-group pull-left">
        <Button
          id="back-btn"
          className="btn-default"
          icon="fa-chevron-left"
          text={t("Back to list")}
          handler={() => this.props.onActionChanged("back")}
        />
      </div>,
    ];

    return (
      <InnerPanel
        title={this.isEdit() ? this.state.scheduleName : t("Schedule Recurring Action")}
        icon="spacewalk-icon-salt"
        buttonsLeft={buttonsLeft}
        buttons={buttons}
      >
        <Form onChange={this.onActionTypeChanged} model={{ actionTypeDescription: this.state.actionTypeDescription }}>
          <DEPRECATED_Select
            required
            name="actionTypeDescription"
            label={t("Action Type")}
            disabled={this.isEdit()}
            options={this.getTypes()}
            labelClass="col-sm-3"
            divClass="col-sm-6"
          />
        </Form>
        <RecurringEventPicker
          scheduleName={this.state.scheduleName}
          type={this.state.details.type}
          cron={this.state.cron}
          cronTimes={this.state.details.cronTimes}
          onScheduleNameChanged={this.onScheduleNameChanged}
          onTypeChanged={this.onTypeChanged}
          onCronTimesChanged={this.onCronTimesChanged}
          onCronChanged={this.onCustomCronChanged}
        />
        {window.entityType === "NONE" || this.state.actionTypeDescription !== "Highstate" ? null : (
          <DisplayHighstate minions={this.state.minions} />
        )}
        {this.state.actionTypeDescription === "Custom state" && (
          <span>
            <h3>
              {t("Configure states to execute")}
              &nbsp;
            </h3>
            <StatesPicker
              type={"state"}
              matchUrl={this.matchUrl}
              saveRequest={this.onSaveStates}
              applyRequest={this.onClickExecute}
            />
          </span>
        )}
        {this.state.actionTypeDescription === "Ansible Playbook" && (
          <RecurringPlaybookPicker
            minionServerId={this.state.targetId}
            playbookPath={this.state.details.playbookPath}
            inventoryPath={this.state.details.inventoryPath}
            flushCache={this.state.details.flushCache}
            onSelectPlaybook={this.onSelectPlaybook}
          />
        )}
      </InnerPanel>
    );
  }
}

export { RecurringActionsEdit };
