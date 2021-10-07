import * as React from "react";
import { Utils } from "../../utils/functions";
import { ElementDefinition } from "./FormulaComponentGenerator";

const generatePassword = Utils.generatePassword;

type Props = {
  id: string;
  value: string;
  onChange: (...args: any[]) => any;
  disabled: boolean;
  element: ElementDefinition;
  required?: boolean;
};

type State = {
  showPassword: boolean;
};

class PasswordInput extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      showPassword: false,
    };

    this.handleGeneratePassword = this.handleGeneratePassword.bind(this);
    this.handleToggleShowPassword = this.handleToggleShowPassword.bind(this);
  }

  handleGeneratePassword(event) {
    event.preventDefault();
    this.props.onChange({
      value: generatePassword(),
      id: this.props.id,
    });
  }

  handleToggleShowPassword(event) {
    event.preventDefault();
    this.setState((state, props) => {
      return { showPassword: !state.showPassword };
    });
  }

  render() {
    // empty password is always rendered as text to prevent problems with browser autocompletion
    return (
      <div className="form-group">
        <label className="col-lg-3 control-label">
          {this.props.element.$name}
          {this.props.required ? <span className="required-form-field"> *</span> : null}:
        </label>
        <div className="col-lg-6">
          <div className="input-group">
            <input
              type={this.state.showPassword || this.props.value === "" ? "text" : "password"}
              name={this.props.element.$name}
              id={this.props.id}
              className="form-control"
              onChange={this.props.onChange}
              placeholder={this.props.element.$placeholder}
              title={this.props.element.$help}
              disabled={this.props.disabled}
              value={this.props.value}
              required={this.props.required}
            />
            <span className="input-group-btn">
              <button
                className="btn btn-default"
                title={t("Generate new password")}
                onClick={this.handleGeneratePassword}
              >
                <i className="fa fa-key no-margin" />
              </button>
              <button
                className="btn btn-default"
                title={t("Show/hide password")}
                onClick={this.handleToggleShowPassword}
              >
                <i className={"fa no-margin " + (this.state.showPassword ? "fa-eye-slash" : "fa-eye")} />
              </button>
            </span>
          </div>
        </div>
      </div>
    );
  }
}

export default PasswordInput;
