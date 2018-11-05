// @flow

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Form } = require('components/input/Form');
const { Text } = require('components/input/Text');
const { SubmitButton, Button } = require('components/buttons');
const Validation = require('components/validation');
const { Messages } = require('components/messages');
const MessagesUtils = require('components/messages').Utils;

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  host: Object,
  submitText: string,
  submit: Function,
  getInitialModel: Function,
  messages: Array<String>,
};

type State = {
  model: Object,
  isInvalid: boolean,
  messages: Array<String>,
};

/*
 * Component editing a virtual machine properties
 */
class GuestProperties extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      model: props.getInitialModel(),
      isInvalid: false,
      messages: [],
    };
  }

  clearFields = () => {
    this.setState({
      model: this.props.getInitialModel(),
    });
  }

  onValidate = (isValid: boolean) => {
    let messages = [];
    if (!Number.isNaN(Number.parseInt(this.state.model.vcpu, 10))
        && (this.state.model.vcpu > this.props.host.cpu.count)) {
      messages = MessagesUtils.warning('Overcommitting CPU can harm performances.');
    }

    this.setState({
      isInvalid: !isValid,
      messages,
    });
  }

  onChange = (model: Object) => {
    this.setState({
      model,
    });
  }

  onSubmit = () => {
    const model = Object.assign({}, this.state.model);
    this.props.submit(model, this.setMessages);
  }

  setMessages = (messages: Array<String>) => {
    this.setState({
      messages,
    });
  }

  renderMessages = () => {
    const messages = [].concat(this.state.messages, this.props.messages);
    return (<Messages items={messages.filter(item => item)} />);
  }

  render() {
    return (
      <div>
        {this.renderMessages()}
        <Form
          className="form-horizontal"
          model={this.state.model}
          onValidate={this.onValidate}
          onChange={this.onChange}
          onSubmit={this.onSubmit}
        >
          <Panel title={t('General')} HeadingLevel="h2">
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
  GuestProperties,
};
