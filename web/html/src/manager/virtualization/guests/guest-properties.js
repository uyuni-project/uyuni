// @flow

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Text } = require('components/input/Text');
const Validation = require('components/validation');
const MessagesUtils = require('components/messages').Utils;
const { GuestPropertiesForm } = require('./properties/guest-properties-form');
const { GuestPropertiesTraditional } = require('./properties/guest-properties-traditional');

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  host: Object,
  submitText: string,
  submit: Function,
  initialModel: Object,
  messages: Array<String>,
};

/*
 * Component editing a virtual machine properties
 */
class GuestProperties extends React.Component<Props> {
  validationChecks = [{
    check: (model: Object) => !Number.isNaN(Number.parseInt(model.vcpu, 10))
      && (model.vcpu > this.props.host.cpu.count),
    message: MessagesUtils.warning('Overcommitting CPU can harm performances.'),
  }]

  render() {
    if (!this.props.host.saltEntitled) {
      return (
        <GuestPropertiesTraditional
          host={this.props.host}
          submitText={this.props.submitText}
          submit={this.props.submit}
          initialModel={this.props.initialModel}
          messages={this.props.messages}
        />
      );
    }

    return (
      <GuestPropertiesForm
        submitText={this.props.submitText}
        submit={this.props.submit}
        initialModel={this.props.initialModel}
        validationChecks={this.validationChecks}
        messages={this.props.messages}
      >
        {
          () => [
            <Panel key="general" title={t('General')} headingLevel="h2">
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
            </Panel>,
          ]
        }
      </GuestPropertiesForm>
    );
  }
}

module.exports = {
  GuestProperties,
};
