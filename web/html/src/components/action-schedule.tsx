import * as React from "react";

import { DateTimePicker } from "components/datetime";
import { Loading } from "components/utils/Loading";

import { localizedMoment } from "utils";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

import { Combobox } from "./combobox";
import { ComboboxItem } from "./combobox";

export type MaintenanceWindow = {
  id: number;
  from: string;
  to: string;
  fromLocalDate: string;
};

export type ActionChain = {
  id: number;
  text: string;
};

type ActionScheduleProps = {
  earliest: moment.Moment;
  actionChains?: Array<ActionChain>;
  onDateTimeChanged: (value: moment.Moment) => void;
  onActionChainChanged?: (actionChain: ActionChain | null) => void;
  systemIds?: Array<string | number>;
  actionType?: string;
};

type ActionScheduleState = {
  loading: boolean;
  type: "earliest" | "actionChain";
  earliest: moment.Moment;
  actionChain?: ActionChain;
  actionChains?: Array<ActionChain>;
  isMaintenanceModeEnabled: boolean;
  multiMaintenanceWindows: boolean;
  maintenanceWindow: MaintenanceWindow;
  maintenanceWindows: Array<MaintenanceWindow>;
  systemIds: Array<string | number>;
  actionType: string;
};

class ActionSchedule extends React.Component<ActionScheduleProps, ActionScheduleState> {
  newActionChainOpt = { id: Number(0), text: t("new action chain") };

  constructor(props: ActionScheduleProps) {
    super(props);

    const commonState = {
      loading: true,
      type: "earliest",
      earliest: props.earliest,
      isMaintenanceModeEnabled: false,
      maintenanceWindow: {},
      multiMaintenanceWindows: false,
      systemIds: props.systemIds ? props.systemIds : [],
      actionType: props.actionType ? props.actionType : "",
    };

    let actionChainsState: any = {};
    if (props.actionChains) {
      actionChainsState = {
        actionChain: props.actionChains.length > 0 ? props.actionChains[0] : this.newActionChainOpt,
        actionChains: props.actionChains.length > 0 ? props.actionChains : [this.newActionChainOpt],
      };
    }

    this.state = Object.assign(commonState, actionChainsState);
  }

  UNSAFE_componentWillMount = () => {
    if (this.state.systemIds && this.state.actionType) {
      const postData = {
        systemIds: this.state.systemIds,
        actionType: this.state.actionType,
      };
      Network.post("/rhn/manager/api/maintenance/upcoming-windows", postData)
        .then((data) => {
          const multiMaintWindows = data.data.maintenanceWindowsMultiSchedules;
          const maintenanceWindows = data.data.maintenanceWindows;

          if (multiMaintWindows === true) {
            this.setState({
              loading: false,
              multiMaintenanceWindows: true,
            });
          } else if (maintenanceWindows) {
            const indexed = maintenanceWindows.map((elem, idx) => Object.assign(elem, { id: idx }));
            this.setState({
              loading: false,
              maintenanceWindow: maintenanceWindows[0],
              maintenanceWindows: indexed,
              isMaintenanceModeEnabled: true,
            });
            this.onMaintenanceWindowChanged(maintenanceWindows[0]);
          } else {
            this.setState({
              loading: false,
              isMaintenanceModeEnabled: false,
            });
          }
        })
        .catch(this.handleResponseError);
    } else {
      this.setState({
        loading: false,
        isMaintenanceModeEnabled: false,
      });
    }
  };

  handleResponseError = (jqXHR) => {
    Loggerhead.error(Network.responseErrorMessage(jqXHR).toString());
    this.setState({ loading: false });
  };

  onDateTimeChanged = (value: moment.Moment) => {
    this.setState({
      type: "earliest",
      earliest: value,
    });
    this.props.onDateTimeChanged(value);

    if (this.props.onActionChainChanged) {
      this.props.onActionChainChanged(null);
    }
  };

  onSelectEarliest = () => {
    this.onDateTimeChanged(this.state.earliest);
  };

  onMaintenanceWindowChanged = (selectedItem: MaintenanceWindow) => {
    const startDateStr = selectedItem.fromLocalDate;
    this.onDateTimeChanged(localizedMoment(startDateStr));
  };

  onSelectMaintenanceWindow = (event: any) => {
    this.onMaintenanceWindowChanged(
      this.state.maintenanceWindows.filter((mw) => DEPRECATED_unsafeEquals(mw.id, event.target.value))[0]
    );
  };

  onFocusMaintenanceWindow = (event: any) => {
    this.onMaintenanceWindowChanged(
      this.state.maintenanceWindows.filter((mw) => DEPRECATED_unsafeEquals(mw.id, event.target.value))[0]
    );
  };

  onActionChainChanged = (selectedItem: ActionChain) => {
    let newActionChain: ActionChain;

    if (!selectedItem.id) {
      // new option let's generate a new id
      newActionChain = {
        id: Number(-1),
        text: selectedItem.text,
      };
    } else {
      newActionChain = {
        id: selectedItem.id,
        text: selectedItem.text,
      };
    }

    if (this.props.onActionChainChanged) {
      this.props.onActionChainChanged(newActionChain);
    }
    this.setState({
      type: "actionChain",
      actionChain: newActionChain,
    });
  };

  onSelectActionChain = (selectedItem: ComboboxItem) => {
    this.onActionChainChanged({
      id: selectedItem.id,
      text: selectedItem.text,
    });
  };

  onFocusActionChain = () => {
    if (this.state.actionChain) {
      this.onActionChainChanged(this.state.actionChain);
    }
  };

  renderMultiMaintWindowsInfo = () => {
    return (
      <div className="alert alert-info">
        {t(
          "There are multiple maintenance schedules for selected systems. Make sure that systems in the set use at most 1 maintenance schedule if you want to schedule by date or maintenance window."
        )}
      </div>
    );
  };

  renderEmptyMaintWindowsInfo = () => {
    return <div className="alert alert-info">{t("No upcoming maintenance windows")}</div>;
  };

  renderDatePicker = () => {
    return <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} />;
  };

  renderMaintWindowPicker = () => {
    const rows = this.state.maintenanceWindows.map((mw) => (
      <option key={mw.id} value={mw.id}>
        {" "}
        {mw.from + " - " + mw.to}
      </option>
    ));
    return (
      <select
        id="maintenance-window"
        className="form-control"
        name="maintenance_window"
        onChange={this.onSelectMaintenanceWindow}
        onFocus={this.onFocusMaintenanceWindow}
      >
        {rows}
      </select>
    );
  };

  // responsible for rendering date picker or maintenance window picker
  renderPickers = () => {
    const renderRadioBtn = this.state.actionChains && this.state.actionChain;

    return (
      <div className="form-group">
        <div className="col-sm-3 control-label">
          {renderRadioBtn && (
            <input
              type="radio"
              name="use_date"
              value="true"
              checked={this.state.type === "earliest"}
              id="schedule-by-date"
              onChange={this.onSelectEarliest}
            />
          )}
          <label htmlFor="schedule-by-date">
            {!this.state.isMaintenanceModeEnabled ? t("Earliest:") : t("Maintenance Window:")}
          </label>
        </div>

        <div className="col-sm-6">
          {this.state.isMaintenanceModeEnabled ? this.renderMaintWindowPicker() : this.renderDatePicker()}
        </div>
      </div>
    );
  };

  // maintenance windows is defined, but empty
  emptyMaintenanceWindows = () => {
    return this.state.maintenanceWindows && this.state.maintenanceWindows.length === 0;
  };

  renderActionChainPicker = () => {
    return (
      <div className="form-group">
        <div className="col-sm-3 control-label">
          <input
            type="radio"
            name="action_chain"
            value="false"
            checked={
              this.state.type === "actionChain" || this.emptyMaintenanceWindows() || this.state.multiMaintenanceWindows
            }
            id="schedule-by-action-chain"
            onChange={this.onFocusActionChain}
          />
          <label htmlFor="schedule-by-action-chain">{t("Add to:")}</label>
        </div>
        <div className="col-sm-3">
          <Combobox
            id="action-chain"
            name="action_chain"
            selectedId={this.state.actionChain?.id}
            options={this.state.actionChains}
            onSelect={this.onSelectActionChain}
            onFocus={this.onFocusActionChain}
          />
        </div>
      </div>
    );
  };

  render() {
    if (this.state.loading) {
      return <Loading text={t("Loading the scheduler...")} />;
    }

    let pickers;
    if (this.state.multiMaintenanceWindows) {
      pickers = this.renderMultiMaintWindowsInfo();
    } else if (this.emptyMaintenanceWindows()) {
      pickers = this.renderEmptyMaintWindowsInfo();
    } else {
      pickers = this.renderPickers();
    }

    return (
      <div className="spacewalk-scheduler">
        <div className="form-horizontal">
          {pickers}
          {this.state.actionChains && this.state.actionChain && this.renderActionChainPicker()}
        </div>
      </div>
    );
  }
}

export { ActionSchedule };
