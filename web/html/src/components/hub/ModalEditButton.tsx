import { type ReactNode, Component } from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Form, Text } from "components/input";
import { Validator } from "components/input/InputBase";

type Props = {
  /** The label of the button that opens the modal */
  buttonLabel: string;
  /** The optioanl icon of the button that opens the modal */
  buttonIcon?: string;
  /** The title of the modal */
  modalTitle: string;
  /** The label of the text field used to edit the value */
  fieldLabel: string;
  /** The optional placeholder text for the text field */
  placeholder?: string;
  /** An array of validators to run against the input, either sync or async, resolve with `true` for valid & `false` for invalid */
  validators?: Validator | Validator[];
  /** Hint to display on a validation error */
  invalidHint?: ReactNode;
  /** true to disable the button and prevent editing */
  disabled: boolean;
  /** The initial value, show when opening the popup */
  value: string;
  /** calback to notify the modified value, after the user presses Save */
  onSave?: (value: string) => void | Promise<void>;
};

type State = {
  valid: boolean;
  model?: { value: string };
};

/** A component that allows to edit a single value with a modal dialog triggered by a button */
export class ModalEditButton extends Component<Props, State> {
  static defaultProps: Partial<Props> = {
    disabled: false,
    value: "",
  };

  public constructor(props: Props) {
    super(props);

    this.state = {
      valid: false,
      model: undefined,
    };
  }

  public render(): ReactNode {
    return (
      <>
        <Button
          text={this.props.buttonLabel}
          disabled={this.props.disabled}
          className="btn-default"
          icon={this.props.buttonIcon}
          handler={() => this.setState({ model: { value: this.props.value }, valid: false })}
        />
        <Dialog
          id="creation-modal"
          title={this.props.modalTitle}
          isOpen={this.state.model !== undefined}
          onClose={() => this.setState({ model: undefined })}
          content={
            <Form
              model={this.state.model}
              onChange={(model) => this.setState({ model: { ...model } })}
              onValidate={(valid) => this.setState({ valid })}
            >
              <Text
                name="value"
                label={this.props.fieldLabel}
                required
                labelClass="col-md-3"
                divClass="col-md-6"
                placeholder={this.props.placeholder}
                validators={this.props.validators}
                invalidHint={this.props.invalidHint}
              />
            </Form>
          }
          footer={
            <div className="col-lg-6">
              <div className="pull-right btn-group">
                <Button
                  id="token-modal-cancel-button"
                  className="btn-default"
                  text={t("Cancel")}
                  handler={() => this.setState({ model: undefined })}
                />
                <Button
                  id="token-modal-submit-button"
                  className="btn-primary"
                  disabled={!this.state.valid}
                  text={t("Save")}
                  handler={() => this.onSave()}
                />
              </div>
            </div>
          }
        />
      </>
    );
  }

  private onSave(): void {
    // When the excution reaches here model should never be undefined
    if (this.props.onSave !== undefined && this.state.model !== undefined) {
      // Notify the change
      this.props.onSave(this.state.model.value);
    }

    // Close the dialog
    this.setState({ model: undefined });
  }
}
