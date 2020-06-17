/* eslint-disable */
// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {DateTimePicker} = require("./datetimepicker");
const {Combobox} = require("./combobox");
import type {ComboboxItem} from "./combobox";
const Functions = require("../utils/functions");
const Network = require("utils/network");
const {Loading} = require("components/utils/Loading");

export type MaintenanceWindow = {
  id: number,
  from: string,
  to: string,
  fromLocalDate: string
}

export type ActionChain = {
  id: number,
  text: string
};

type ActionScheduleProps = {
  earliest: Date,
  timezone: string,
  localTime: string,
  actionChains?: Array<ActionChain>,
  onDateTimeChanged: (date: Date) => void,
  onActionChainChanged?: (actionChain: ?ActionChain) => void,
  systemIds?: Array<number>,
  actionType?: string,
};

type ActionScheduleState = {
  loading: boolean,
  type: "earliest" | "actionChain",
  earliest: Date,
  actionChain?: ActionChain,
  actionChains?: Array<ActionChain>,
  isMaintenanceModeEnabled: boolean,
  maintenanceWindow: MaintenanceWindow,
  maintenanceWindows: Array<MaintenanceWindow>,
  systemIds: Array<number>,
  actionType: string,
};

class ActionSchedule extends React.Component<ActionScheduleProps, ActionScheduleState> {

  newActionChainOpt = {id: Number(0), text: t("new action chain")};

  constructor(props: ActionScheduleProps) {
    super(props);

    const commonState = {
      loading: true,
      type: "earliest",
      earliest: props.earliest,
      isMaintenanceModeEnabled: false,
      maintenanceWindow: {},
      maintenanceWindows: [],
      systemIds: props.systemIds ? props.systemIds : [],
      actionType: props.actionType ? props.actionType : "",
    }

    let actionChainsState = {};
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
      const postData = JSON.stringify({
        systemIds: this.state.systemIds,
        actionType: this.state.actionType,
      });
      Network.post("/rhn/manager/api/maintenance-windows", postData, "application/json").promise
        .then(data =>
          {
            const maintenanceWindows = data.data.maintenanceWindows;

            if (maintenanceWindows) {
              const indexed = maintenanceWindows.map((elem, idx) => Object.assign(elem, {"id": idx}));
              this.setState({
                loading: false,
                maintenanceWindow: maintenanceWindows[0],
                maintenanceWindows: indexed,
                isMaintenanceModeEnabled: true
              });
            }
            else {
              this.setState({
                loading: false,
                isMaintenanceModeEnabled: false
              });
            }
          }
        ).catch(this.handleResponseError);
    }
    else {
      this.setState({
        loading: false,
        isMaintenanceModeEnabled: false
      });
    }
  };

  handleResponseError = (jqXHR) => {
    console.log(Network.responseErrorMessage(jqXHR));
    this.setState({ loading: false });
  };

  onDateTimeChanged = (date: Date) => {
    this.setState({
      type: "earliest",
      earliest: date
    });
    this.props.onDateTimeChanged(date);
  }

  onSelectEarliest = () => {
    this.onDateTimeChanged(this.state.earliest);
  }

  onMaintenanceWindowChanged = (selectedItem: MaintenanceWindow) => {
    const startDateStr = selectedItem.fromLocalDate;
    this.onDateTimeChanged(Functions.Utils.dateWithTimezone(startDateStr));
  }

  onSelectMaintenanceWindow = (event: Object) => {
    this.onMaintenanceWindowChanged(this.state.maintenanceWindows.filter(mw => mw.id == event.target.value)[0]);
  }

  onFocusMaintenanceWindow = (event: Object) => {
    this.onMaintenanceWindowChanged(this.state.maintenanceWindows.filter(mw => mw.id == event.target.value)[0]);
  }

  onActionChainChanged = (selectedItem: ActionChain) => {
    let newActionChain: ActionChain;

    if (!selectedItem.id) {
      // new option let's generate a new id
      newActionChain = {
        id: Number(-1),
        text: selectedItem.text
      }
    } else {
      newActionChain = {
        id: selectedItem.id,
        text: selectedItem.text
      }
    }

    if (this.props.onActionChainChanged) {
      this.props.onActionChainChanged(newActionChain);
    }
    this.setState({
      type: "actionChain",
      actionChain: newActionChain,
    });
  }

  onSelectActionChain = (selectedItem: ComboboxItem) => {
    this.onActionChainChanged({
      id: selectedItem.id,
      text: selectedItem.text
    });
  }

  onFocusActionChain = () => {
    if (this.state.actionChain) {
      this.onActionChainChanged(this.state.actionChain);
    }
  }

  render() {
    if (this.state.loading) {
      return <Loading text={t('Loading the scheduler...')}/>
    }
    return (
      <div className="spacewalk-scheduler">
        <div className="form-horizontal">
          <div className="form-group">
            <div className="col-sm-3 control-label">
              { (this.state.actionChains && this.state.actionChain) &&
                <input type="radio" name="use_date" value="true" checked={this.state.type == "earliest"} id="schedule-by-date" onChange={this.onSelectEarliest}/> }
              <label htmlFor="schedule-by-date">{!this.state.isMaintenanceModeEnabled ?t("Earliest:") : t("Maintenance Window:")}</label>
            </div>
              {
                !this.state.isMaintenanceModeEnabled ?
                  <div className="col-sm-6">
                    <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={this.props.timezone} />
                  </div>
                  :
                  <div className="col-sm-6">
                    <select
                        id="maintenance-window"
                        className="form-control"
                        name="maintenance_window"
                        onChange={this.onSelectMaintenanceWindow}
                        onFocus={this.onFocusMaintenanceWindow}>
                      { this.state.maintenanceWindows.map(mw =><option key={mw.id} value={mw.id}> {mw.from + " - " + mw.to}</option>) }
                    </select>
                  </div>
              }
          </div>
          { (this.state.actionChains && this.state.actionChain) &&
            <div className="form-group">
              <div className="col-sm-3 control-label">
                <input type="radio" name="action_chain" value="false" checked={this.state.type == "actionChain"} id="schedule-by-action-chain" onChange={this.onFocusActionChain}/>
                <label htmlFor="schedule-by-action-chain">{t("Add to:")}</label>
              </div>
              <div className="col-sm-3">
                { (this.state.actionChains && this.state.actionChain) &&
                  <Combobox id="action-chain" name="action_chain" selectedId={this.state.actionChain.id}
                            data={this.state.actionChains} onSelect={this.onSelectActionChain}
                            onFocus={this.onFocusActionChain} />}
              </div>
            </div>
            }
        </div>
      </div>
    );
  }

}

module.exports = {
  ActionSchedule: ActionSchedule
}
