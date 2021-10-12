import * as React from "react";
import { RecurringEventPicker } from "components/picker/recurring-event-picker";
import { DisplayHighstate } from "./display-highstate";
import { Button } from "components/buttons";
import { AsyncButton } from "components/buttons";
import { Toggler } from "components/toggler";
import { InnerPanel } from "components/panels/InnerPanel";

type Props = {
  schedule?: any;
  onEdit: (arg0: any) => any;
  onActionChanged: (arg0: any) => any;
};

type State = {
  minions?: any[];
  active: boolean;
  targetId?: any;
  recurringActionId?: any;
  scheduleName?: any;
  type?: any;
  targetType?: any;
  cronTimes?: any;
  cron?: any;
  test?: any;
};

class RecurringStatesEdit extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    this.state = {
      minions: window.minions,
      active: true,
    };

    if (this.isEdit()) {
      this.setSchedule(this.props.schedule);
    } else {
      this.getTargetType();
    }
  }

  setSchedule = (schedule) => {
    Object.assign(this.state, schedule);
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

  isEdit = () => {
    return this.props.schedule ? true : false;
  };

  onEdit = () => {
    return this.props.onEdit({
      targetId: this.state.targetId,
      recurringActionId: this.state.recurringActionId,
      scheduleName: this.state.scheduleName,
      active: this.state.active,
      type: this.state.type,
      targetType: this.state.targetType,
      cronTimes: this.state.cronTimes,
      cron: this.state.cron,
      test: this.state.test,
    });
  };

  onScheduleNameChanged = (scheduleName) => {
    this.setState({ scheduleName: scheduleName });
  };

  onToggleActive = (active) => {
    this.setState({ active: active });
  };

  onTypeChanged = (type) => {
    this.setState({ type: type });
  };

  onCronTimesChanged = (cronTimes) => {
    this.setState({ cronTimes: cronTimes });
  };

  onCustomCronChanged = (cron) => {
    this.setState({ cron: cron });
  };

  toggleTestState = () => {
    this.setState({ test: !this.state.test });
  };

  render() {
    const buttons = [
      <div className="btn-group pull-right">
        <Toggler
          text={t("Test mode")}
          value={this.state.test}
          className="btn"
          handler={this.toggleTestState.bind(this)}
        />
        <AsyncButton
          action={this.onEdit}
          defaultType="btn-success"
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
        title={t("Schedule Recurring Highstate")}
        icon="spacewalk-icon-salt"
        buttonsLeft={buttonsLeft}
        buttons={buttons}
      >
        <RecurringEventPicker
          timezone={window.timezone}
          scheduleName={this.state.scheduleName}
          type={this.state.type}
          cron={this.state.cron}
          cronTimes={this.state.cronTimes}
          onScheduleNameChanged={this.onScheduleNameChanged}
          onTypeChanged={this.onTypeChanged}
          onCronTimesChanged={this.onCronTimesChanged}
          onCronChanged={this.onCustomCronChanged}
        />
        {window.entityType === "NONE" ? null : <DisplayHighstate minions={this.state.minions} />}
      </InnerPanel>
    );
  }
}

export { RecurringStatesEdit };
