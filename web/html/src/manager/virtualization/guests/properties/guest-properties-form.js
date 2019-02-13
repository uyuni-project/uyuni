// @flow

import type { ActionChain } from 'components/action-schedule';

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Form } = require('components/input/Form');
const { SubmitButton, Button } = require('components/buttons');
const { Messages } = require('components/messages');
const { ActionSchedule } = require('components/action-schedule');
const Functions = require('utils/functions');

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  submitText: string,
  submit: Function,
  initialModel: ?Object,
  validationChecks: Array<{ check: (model: Object) => boolean, message: Object }>,
  messages: Array<String>,
  children: Function,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

type State = {
  model: Object,
  isInvalid: boolean,
  messages: Array<Object>,
  actionChain: ?ActionChain,
};

/*
 * Component editing a virtual machine properties
 */
class GuestPropertiesForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      model: Object.assign({}, props.initialModel, { earliest: Functions.Utils.dateWithTimezone(props.localTime) }),
      isInvalid: false,
      messages: [],
      actionChain: null,
    };
  }

  clearFields = () => {
    this.setState({
      model: Object.assign({}, this.props.initialModel),
    });
  }

  onValidate = (isValid: boolean) => {
    this.setState(prevState => ({
      isInvalid: !isValid,
      messages: this.props.validationChecks
        .filter(item => item.check(prevState.model))
        .map(item => item.message),
    }));
  }

  onChange = (model: Object) => {
    this.setState({
      model,
    });
  }

  onSubmit = () => {
    const model = Object.assign({}, this.state.model);
    this.props.submit(model);
  }

  changeModel = (model: Object) => {
    this.setState({
      model,
    });
  }

  onDateTimeChanged = (date: Date) => {
    this.setState(state => ({
      model: Object.assign({}, state.model, { earliest: date, actionChain: null }),
      actionChain: null,
    }));
  }

  onActionChainChanged = (actionChain: ?ActionChain) => {
    this.setState(state => ({
      model: Object.assign({}, state.model, { actionChain: actionChain ? actionChain.text : null }),
      actionChain,
    }));
  }

  render() {
    return (
      <div>
        <Form
          className="form-horizontal"
          model={this.state.model}
          onValidate={this.onValidate}
          onChange={this.onChange}
          onSubmit={this.onSubmit}
        >
          <Messages items={[].concat(this.state.messages, this.props.messages)} />
          {
            this.props.children({
              model: this.state.model,
              changeModel: this.changeModel,
            })
          }
          <Panel
            key="schedule"
            title={t('Schedule')}
            headingLevel="h3"
          >
            <ActionSchedule
              timezone={this.props.timezone}
              localTime={this.props.localTime}
              earliest={this.state.model.earliest}
              actionChains={this.props.actionChains}
              actionChain={this.state.actionChain}
              onActionChainChanged={this.onActionChainChanged}
              onDateTimeChanged={this.onDateTimeChanged}
            />
          </Panel>
          <div className="col-md-offset-3 col-md-6">
            <SubmitButton
              id="submit-btn"
              className="btn-success"
              text={this.props.submitText}
              disabled={this.state.isInvalid}
            />
            <Button
              id="clear-btn"
              className="btn-default pull-right"
              icon="fa-eraser"
              text={t('Clear Fields')}
              handler={this.clearFields}
            />
          </div>
        </Form>
      </div>
    );
  }
}

module.exports = {
  GuestPropertiesForm,
};
