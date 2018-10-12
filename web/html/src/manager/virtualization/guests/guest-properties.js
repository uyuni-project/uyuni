/* eslint-disable */
// @flow

const React = require("react");
const ReactDOM = require("react-dom");
const {SmallPanel} = require("components/panel");
const {Form, FormGroup, Text, Select} = require("components/input");
const {SubmitButton, Button} = require("components/buttons");
const Validation = require("components/validation");
const {Messages} = require("components/messages");
const MessagesUtils = require("components/messages").Utils;
const PropTypes = React.PropTypes;

/*
 * Component editing a virtual machine properties
 *
 * Properties:
 *   + host
 *   + submitText
 *   + submit: a function that takes the hostid, the guest uuid, the form model,
 *             and a setMessages function as parameters
 *   + getInitialModel: a function that returns the default values for the model
 */
class GuestProperties extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      model: props.getInitialModel(),
      isInvalid: false,
      messages: null,
    };

    this.clearFields = () => {
      this.setState({
        model: this.props.getInitialModel()
      });
    };

    this.onValidate = (isValid) => {
      var messages = null;
      if (!Number.isNaN(Number.parseInt(this.state.model.vcpu)) && (this.state.model.vcpu > this.props.host.cpu.count)) {
        messages = MessagesUtils.warning("Overcommitting CPU can harm performances.");
      }

      this.setState({
        isInvalid: !isValid,
        messages : messages
      });
    };

    this.onChange = (model) => {
      this.setState({
        model: model
      });
    };

    this.onSubmit = () => {
      const model = Object.assign({}, this.state.model)
      this.props.submit(model, this.setMessages);
    };

    this.setMessages = (messages) => {
      this.setState({
        messages : messages
      });
    };

    this.renderMessages = () => {
      const messages = [].concat(this.state.messages, this.props.messages);
      return (<Messages items={messages.filter(item => item)}/>);
    };
  }


  render() {

    return (
      <div>
        {this.renderMessages()}
        <Form className="form-horizontal"
              model={this.state.model}
              onValidate={this.onValidate}
              onChange={this.onChange}
              onSubmit={this.onSubmit}>
          <SmallPanel title={t("General")}>
            <Text name="memory" label={t("Maximum Memory (MiB)")} required
                  invalidHint={t("A positive integer is required")}
                  labelClass="col-md-3" divClass="col-md-6"
                  validators={[Validation.isInt({gt:0})]}/>
            <Text name="vcpu" label={t("Virtual CPU Count")} required
                  invalidHint={t("A positive integer is required")}
                  labelClass="col-md-3" divClass="col-md-6"
                  validators={[Validation.isInt({gt:0})]}/>
          </SmallPanel>
          <div className="col-md-offset-3 col-md-6">
            <SubmitButton id="submit-btn" className="btn-success"
                text={this.props.submitText} disabled={this.state.isInvalid}/>
            <Button id="clear-btn" className="btn-default pull-right"
                icon="fa-eraser" text={t("Clear Fields")} handler={this.clearFields}/>
          </div>
        </Form>
      </div>
    );
  }
}

GuestProperties.propTypes = {
    host: PropTypes.object.isRequired,
    submitText: PropTypes.string.isRequired,
    submit: PropTypes.func.isRequired,
    getInitialModel: PropTypes.func.isRequired,
    messages: PropTypes.array,
};

module.exports = {
  GuestProperties,
};
