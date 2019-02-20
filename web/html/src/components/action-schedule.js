/* eslint-disable */
// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {DateTimePicker} = require("./datetimepicker");
const {Combobox} = require("./combobox");
import type {ComboboxItem} from "./combobox";
const Functions = require("../utils/functions");

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

  newActionChainOpt = {id: Number(0), text: t("new action chain")};

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

  onSelectEarliest = () => {
    this.onDateTimeChanged(this.state.earliest);
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

    this.props.onActionChainChanged(newActionChain);
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
    this.onActionChainChanged(this.state.actionChain);
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
              <input type="radio" name="action_chain" value="false" checked={this.state.type == "actionChain"} id="schedule-by-action-chain" onChange={this.onFocusActionChain}/>
              <label htmlFor="schedule-by-action-chain">{t("Add to:")}</label>
            </div>
            <div className="col-sm-3">
              <Combobox id="action-chain" name="action_chain" selectedId={this.state.actionChain.id}
                        data={this.state.actionChains} onSelect={this.onSelectActionChain}
                        onFocus={this.onFocusActionChain} />
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
