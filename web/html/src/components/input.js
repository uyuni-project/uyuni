/* eslint-disable */
'use strict';

const React = require("react");
const DateTimePicker = require("./datetimepicker").DateTimePicker;
const Functions = require("../utils/functions");

class Form extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            model: props.model,
            isValid: true
        };
    }

    componentWillMount() {
        this.inputs = {};
    }

    componentWillReceiveProps(props) {
        this.setState({
            model: props.model
        });
    }

    registerInput(component) {
        if(component.props && component.props.name) {
            this.inputs[component.props.name] = component;

            const model = this.state.model;
            model[component.props.name] = component.props.value;
            this.setState({
                model: model
            });
            this.validate(component);
        }
    }

    unregisterInput(component) {
        if(component.props && component.props.name) {
            delete this.inputs[component.props.name];

            const model = this.state.model;
            delete model[component.props.name];
            this.setState({
                model: model
            });
        }
    }

    renderChildren(children) {
        if(typeof children !== 'object' || children == null) {
            return children;
        }

        const childrenCount = React.Children.count(children);

        return React.Children.map(children, child => this.renderChild(child));
    }

    renderChild(child) {
        if (typeof child !== 'object' || child === null) {
            return child;
        }

        // TODO [LuNeves] [Cedric] After upgrading react to v16 this could be improved with Context:
        // formPropsProvider and a formPropsConsumer on the component inputBase
        const fieldTypes = [Text, Password, Check, Select, DateTime];
        if (child.type && child.type.prototype !== null
            && fieldTypes.reduce((isField, currentType) => isField || child.type === currentType, false)) {
            let name = child.props && child.props.name;

            if (!name) {
                throw new Error('Can not add input without "name" attribute');
            }

            let newProps = {
                registerInput: this.registerInput.bind(this),
                unregisterInput: this.unregisterInput.bind(this),
                validate: this.validate.bind(this),
                onFormChange: this.onFieldChange.bind(this),
                invalidHint: child.props.invalidHint || (child.props.required && (child.props.label + " is required."))
            };

            newProps.value = this.state.model[name] || "";

            return React.cloneElement(child, Object.assign({}, child.props, newProps), child.props.children);
        } else {
            const renderedChildren = this.renderChildren(child.props && child.props.children);
            return React.cloneElement(child, child.props, renderedChildren);
        }
    }

    validate(component, propsIn) {
        const props = propsIn || component.props;
        const name = props.name;
        const results = [];
        let isValid = true;

        if(!props.disabled && (this.state.model[name] || props.required)) {
            if(props.required && !this.state.model[name]) {
                isValid = false;
            } else if(props.validators) {
                const validators = Array.isArray(props.validators) ? props.validators : [props.validators];
                validators.forEach(v => {
                    results.push(Promise.resolve(v('' + (this.state.model[name] || ''))));
                });
            }
        }

        Promise.all(results).then(result => {
            result.forEach(r => isValid &= r);
            component.setState({
                isValid: isValid
            }, this.validateForm.bind(this));
        });
    }

    validateForm() {
        let allValid = true;
        const inputs = this.inputs;

        Object.keys(inputs).forEach(name => {
            if(!inputs[name].state.isValid) {
                allValid = false;
            }
        });

        this.setState({
            isValid: allValid
        });

        if(this.props.onValidate) {
            this.props.onValidate(allValid);
        }
    }

    onFieldChange(item) {
        const model = this.state.model;
        model[item.name] = item.value;
        this.setState({
            model: model
        });

        if(this.props.onChange) {
            this.props.onChange(this.state.model, this.state.isValid);
        }
    }

    submit(event) {
        event.preventDefault();
        if(this.state.isValid && this.props.onSubmit) {
            this.props.onSubmit(this.state.model, event);
        } else if (this.props.onSubmitInvalid) {
            this.props.onSubmitInvalid(this.state.model, event);
        }
    }

    render() {
        return (
            <form ref={this.props.formRef} onSubmit={this.submit.bind(this)} className={this.props.className}>
                <div className={"form-horizontal" + (this.props.divClass ? " " + this.props.divClass : "")}>
                    {this.renderChildren(this.props.children)}
                </div>
            </form>
        );
    }
}

class _InputBase extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isValid: true,
            showErrors: false
        };
    }

    componentWillReceiveProps(props) {
        if(!(props.value === this.props.value && props.disabled === this.props.disabled &&
              props.required === this.props.required)) {
            if (this.props.validate) this.props.validate(this, props);
            this.setState({
                showErrors: false
            });
        }
    }

    componentWillMount() {
      if (this.props.registerInput) {
        this.props.registerInput(this);
      }
    }

    componentWillUnmount() {
      if (this.props.unregisterInput) {
        this.props.unregisterInput(this);
      }
    }

    setValue(name, value) {
        if (this.props.onFormChange) {
          this.props.onFormChange({
              name: name,
              value: value
          });
        }

        if(this.props.onChange)
            this.props.onChange(name, value);
    }

    onBlur() {
        this.setState({
            showErrors: true
        });
    }

    render() {
        const isError = this.state.showErrors && !this.state.isValid;
        const invalidHint = isError && this.props.invalidHint;
        const hint = [this.props.hint, (invalidHint && this.props.hint && <br/>), invalidHint];
        return (
            <FormGroup isError={isError}>
                { this.props.label && <Label name={this.props.label} className={this.props.labelClass} required={this.props.required}/> }
                <div className={this.props.divClass}>
                    {
                      this.props.children({
                        setValue: this.setValue.bind(this),
                        onBlur: this.onBlur.bind(this),
                      })
                    }
                    { hint &&
                        <div className="help-block">
                            {hint}
                        </div>
                    }
                </div>
            </FormGroup>
        );
    }
}

function Text(props) {
  const {
    type,
    placeholder,
    inputClass,
    ...propsToPass
  } = props;
  return (
    <_InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          return (
            <input
              className={`form-control${inputClass ? ` ' ${inputClass}` : ''}`}
              type={type || "text"}
              name={props.name}
              value={props.value}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              placeholder={placeholder}
            />
          );
        }
      }
    </_InputBase>
  );
}

function Password(props) {
  return (<Text type="password" {...props} />);
}

function Check(props) {
  const {
    label,
    inputClass,
    ...propsToPass
  } = props;
  return (
    <_InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const setChecked = (event)  => {
            setValue(event.target.name, event.target.checked);
          };
          return (
            <div className="checkbox">
                <label>
                    <input
                      className={inputClass}
                      name={props.name}
                      type="checkbox"
                      checked={props.value}
                      onChange={setChecked}
                      onBlur={onBlur}
                      disabled={props.disabled}
                    />
                    <span>{label}</span>
                </label>
            </div>
          );
        }
      }
    </_InputBase>
  );
}

function Select(props) {
  const {
    inputClass,
    children,
    ...propsToPass
  } = props;
  return (
    <_InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          return (
            <select
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              name={props.name}
              disabled={props.disabled}
              value={props.value}
              onBlur={onBlur}
              onChange={onChange}
            >
              {children}
            </select>
          );
        }
      }
    </_InputBase>
  );
}

function DateTime(props) {
  const {
    timezone,
    ...propsToPass
  } = props;
  return (
    <_InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const onChange = (value) => {
            setValue(props.name, value);
          };
          return (
            <DateTimePicker
              onChange={onChange}
              value={props.value}
              timezone={timezone}
            />
          );
        }
      }
    </_InputBase>
  );
}

const Label = function(props) {
    return (
        <label className={"control-label" + (props.className ? " " + props.className : "")}>
            {props.name}
            { props.required ? <span className="required-form-field"> *</span> : undefined }
            :
        </label>
    );
}

const FormGroup = function(props) {
    return (
        <div className={"form-group" + (props.isError ? " has-error" : "")}>
            {props.children}
        </div>
    );
}

module.exports = {
    Form: Form,
    Text: Text,
    Password: Password,
    Check: Check,
    Select: Select,
    DateTime: DateTime,
    FormGroup: FormGroup,
    Label: Label
};
