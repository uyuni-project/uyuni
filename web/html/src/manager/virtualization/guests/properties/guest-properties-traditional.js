// @flow

import type { ActionChain } from 'components/action-schedule';

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Text } = require('components/input/Text');
const Validation = require('components/validation');
const MessagesUtils = require('components/messages').Utils;
const { GuestPropertiesForm } = require('./guest-properties-form');

type Props = {
  host: Object,
  submitText: string,
  submit: Function,
  initialModel: ?Object,
  messages: Array<String>,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

type State = {
  model: Object,
  isInvalid: boolean,
  messages: Array<String>,
};

/*
 * Component editing a virtual machine properties
 */
class GuestPropertiesTraditional extends React.Component<Props, State> {
  validationChecks = [{
    check: (model: Object) => !Number.isNaN(Number.parseInt(model.vcpu, 10))
      && (model.vcpu > this.props.host.cpu.count),
    message: MessagesUtils.warning('Overcommitting CPU can harm performances.'),
  }]

  render() {
    return (
      <GuestPropertiesForm
        submitText={this.props.submitText}
        submit={this.props.submit}
        initialModel={this.props.initialModel}
        validationChecks={this.validationChecks}
        messages={this.props.messages}
        localTime={this.props.localTime}
        timezone={this.props.timezone}
        actionChains={this.props.actionChains}
      >
        {
          () => (
            <Panel title={t('General')} headingLevel="h2">
              <Text
                name="memory"
                label={t('Maximum Memory (MiB)')}
                required
                invalidHint={t('A positive integer is required')}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[Validation.isInt({ gt: 0 })]}
              />
              <Text
                name="vcpu"
                label={t('Virtual CPU Count')}
                required
                invalidHint={t('A positive integer is required')}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[Validation.isInt({ gt: 0 })]}
              />
            </Panel>
          )
        }
      </GuestPropertiesForm>
    );
  }
}

module.exports = {
  GuestPropertiesTraditional,
};
