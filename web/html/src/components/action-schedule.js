// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {DateTimePicker} = require("./datetimepicker");
const {Combobox} = require("./combobox");
const Functions = require("../utils/functions");

declare function t(msg: string): string;

export type ActionChain = {
  id: number,
  text: string
};

type ActionScheduleProps = {
  earliest: Date,
  timezone: string,
  localTime: string,
  actionChains: Array<ActionChain>,
  onDateTimeChanged: (date: Date) => void,
  onActionChainChanged: (actionChain: ?ActionChain) => void
};

type ActionScheduleState = {
  type: "earliest" | "actionChain",
  earliest: Date,
  actionChain: ActionChain,
  actionChains: Array<ActionChain>
};

class ActionSchedule extends React.Component<ActionScheduleProps, ActionScheduleState> {

  newActionChainOpt = {id: 0, text: t("new action chain")};

  constructor(props: ActionScheduleProps) {
    super(props);

    this.state = {
      type: "earliest",
      earliest: props.earliest,
      actionChain: props.actionChains.length > 0 ? props.actionChains[0] : this.newActionChainOpt,
      actionChains: props.actionChains.length > 0 ? props.actionChains : [this.newActionChainOpt]
    };
  }

  onDateTimeChanged = (date: Date) => {
    this.setState({
      type: "earliest",
      earliest: date
    });
    this.props.onDateTimeChanged(date);
  }

  onActionChainChanged = (idOrNewLabel: ?string) => {
    let selectedActionChain: ?ActionChain;
    let actionChains = this.state.actionChains;
    if (idOrNewLabel) {
      selectedActionChain = this.props.actionChains.find((ac) => ac.id.toString() == idOrNewLabel);
    }
    if (selectedActionChain) {
      this.props.onActionChainChanged(selectedActionChain);
    } else {
      selectedActionChain = {id: 0, text: idOrNewLabel ? idOrNewLabel : ""};
      this.props.onActionChainChanged(selectedActionChain);
    }

    this.setState({
      type: "actionChain",
      actionChain: selectedActionChain,
      actionChains: actionChains
    });
  }

  onSelectEarliest = () => {
    this.onDateTimeChanged(this.state.earliest);
  }

  onSelectActionChain = () => {
    this.onActionChainChanged(this.state.actionChain ? this.state.actionChain.id.toString() : null);
  }

  render() {
    return (
      <div className="spacewalk-scheduler">
        <div className="form-horizontal">
          <div className="form-group">
            <div className="col-sm-3 control-label">
              <input type="radio" name="use_date" value="true" checked={this.state.type == "earliest"} id="schedule-by-date" onChange={this.onSelectEarliest}/>
              <label htmlFor="schedule-by-date">{t("Earliest:")}</label>
            </div>
            <div className="col-sm-6">
              <DateTimePicker onChange={this.onDateTimeChanged} value={this.state.earliest} timezone={this.props.timezone} />
            </div>
          </div>
          <div className="form-group">
            <div className="col-sm-3 control-label">
              <input type="radio" name="action_chain" value="false" checked={this.state.type == "actionChain"} id="schedule-by-action-chain" onChange={this.onSelectActionChain}/>
              <label htmlFor="schedule-by-action-chain">{t("Add to:")}</label>
            </div>
            <div className="col-sm-3">
              <Combobox id="action-chain" name="action_chain" value={this.state.actionChain.id}
                        onFocus={this.onSelectActionChain} data={this.state.actionChains} onSelect={this.onActionChainChanged} />
            </div>
          </div>
        </div>
      </div>
    );
  }

}

module.exports = {
  ActionSchedule: ActionSchedule
}
