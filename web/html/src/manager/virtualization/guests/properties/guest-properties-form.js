// @flow

const React = require('react');
const { Form } = require('components/input/Form');
const { SubmitButton, Button } = require('components/buttons');
const { Messages } = require('components/messages');

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  submitText: string,
  submit: Function,
  initialModel: Object,
  validationChecks: Array<{ check: (model: Object) => boolean, message: Object }>,
  messages: Array<String>,
  children: Function,
};

type State = {
  model: Object,
  isInvalid: boolean,
  messages: Array<Object>,
};

/*
 * Component editing a virtual machine properties
 */
class GuestPropertiesForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      model: Object.assign({}, props.initialModel),
      isInvalid: false,
      messages: [],
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
