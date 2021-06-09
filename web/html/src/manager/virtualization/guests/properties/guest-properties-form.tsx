import { ActionChain } from "components/action-schedule";
import { MessageType } from "components/messages";

import * as React from "react";
import { Panel } from "components/panels/Panel";
import { Form } from "components/input/Form";
import { SubmitButton, Button } from "components/buttons";
import { Messages } from "components/messages";
import { ActionSchedule } from "components/action-schedule";
import { localizedMoment } from "utils";

type Props = {
  submitText: string;
  submit: Function;
  initialModel: any | null | undefined;
  validationChecks: Array<{ check: (model: any) => boolean; message: MessageType }>;
  messages: Array<MessageType>;
  children: (props: { model: any, changeModel: any }) => JSX.Element | JSX.Element[];
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

type State = {
  model: {
    earliest: moment.Moment
  } & any;
  isInvalid: boolean;
  actionChain: ActionChain | null | undefined;
};

/*
 * Component editing a virtual machine properties
 */
class GuestPropertiesForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      model: Object.assign({}, props.initialModel, { earliest: localizedMoment() }),
      isInvalid: false,
      actionChain: null,
    };
  }

  clearFields = () => {
    this.setState({
      model: Object.assign({}, this.props.initialModel),
    });
  };

  onValidate = (isValid: boolean) => {
    this.setState(prevState => ({
      isInvalid: !isValid,
    }));
  };

  onChange = (model: any) => {
    this.setState({
      model,
    });
  };

  onSubmit = () => {
    const model = Object.assign({}, this.state.model);
    this.props.submit(model);
  };

  onDateTimeChanged = (value: moment.Moment) => {
    this.setState(state => ({
      model: Object.assign({}, state.model, { earliest: value, actionChain: null }),
      actionChain: null,
    }));
  };

  onActionChainChanged = (actionChain: ActionChain | null | undefined) => {
    this.setState(state => ({
      model: Object.assign({}, state.model, { actionChain: actionChain ? actionChain.text : null }),
      actionChain,
    }));
  };

  render() {
    const checksMessages = this.props.validationChecks
      .filter(item => item.check(this.state.model))
      .flatMap(item => item.message);
    return (
      <div>
        <Form
          className="form-horizontal"
          model={this.state.model}
          onValidate={this.onValidate}
          onChange={this.onChange}
          onSubmit={this.onSubmit}
        >
          <Messages items={([] as any[]).concat(checksMessages, this.props.messages)} />
          {this.props.children({
            model: this.state.model,
            changeModel: this.onChange,
          })}
          <Panel key="schedule" title={t("Schedule")} headingLevel="h3">
            <ActionSchedule
              earliest={this.state.model.earliest}
              actionChains={this.props.actionChains}
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
              text={t("Clear Fields")}
              handler={this.clearFields}
            />
          </div>
        </Form>
      </div>
    );
  }
}

export { GuestPropertiesForm };
