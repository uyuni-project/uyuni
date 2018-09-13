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

        if (childrenCount > 1) {
            return React.Children.map(children, child => this.renderChild(child));
        } else if (childrenCount === 1) {
            return this.renderChild(Array.isArray(children) ? children[0] : children);
        }
    }

    renderChild(child) {
        if (typeof child !== 'object' || child === null) {
            return child;
        }

        if (child.type && child.type.prototype !== null &&
                child.type.prototype instanceof _InputBase) {
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

            return React.cloneElement(child, newProps);
        } else {
            return React.cloneElement(child, {}, this.renderChildren(child.props && child.props.children));
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
            this.props.validate(this, props);
            this.setState({
                showErrors: false
            });
        }
    }

    componentWillMount() {
        this.props.registerInput(this);
    }

    componentWillUnmount() {
        this.props.unregisterInput(this);
    }

    setValue(event) {
        const name = event.target.name;
        const value = event.target.value;
        this.props.onFormChange({
            name: name,
            value: value
        });

        if(this.props.onChange)
            this.props.onChange(event);
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
                    <input className={"form-control" + (this.props.inputClass ? " " + this.props.inputClass : "")}
                            type={this.type} name={this.props.name} value={this.props.value}
                            onChange={this.setValue.bind(this)} disabled={this.props.disabled}
                            onBlur={this.onBlur.bind(this)} placeholder={this.props.placeholder}/>
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

class Text extends _InputBase {
    constructor(props) {
        super(props);
        this.type = this.props.type || "text";
    }
}

class Password extends _InputBase {
    constructor(props) {
        super(props);
        this.type = "password";
    }
}

class Check extends _InputBase {
    constructor(props) {
        super(props);
    }

    setChecked(event) {
        const name = event.target.name;
        const value = event.target.checked;
        this.props.onFormChange({
            name: name,
            value: value
        });

        if(this.props.onChange)
            this.props.onChange(event);
    }

    render() {
        const isError = this.state.showErrors && !this.state.isValid;
        const invalidHint = isError && this.props.invalidHint;
        const hint = [this.props.hint, (invalidHint && this.props.hint && <br/>), invalidHint];
        return (
            <FormGroup isError={isError}>
                <div className={this.props.divClass}>
                    <div className="checkbox">
                        <label>
                            <input className={this.props.inputClass} name={this.props.name}
                                    type="checkbox" checked={this.props.value}
                                    onChange={this.setChecked.bind(this)} onBlur={this.onBlur.bind(this)}
                                    disabled={this.props.disabled}/>
                            <span>{this.props.label}</span>
                        </label>
                    </div>
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

class Select extends _InputBase {
    constructor(props) {
        super(props);
    }

    render() {
        const isError = this.state.showErrors && !this.state.isValid;
        const invalidHint = isError && this.props.invalidHint;
        const hasHint = this.props.hint || invalidHint;
        const hint = [this.props.hint, (invalidHint && this.props.hint && <br/>), invalidHint];
        return (
            <FormGroup isError={isError}>
                { this.props.label && <Label name={this.props.label} className={this.props.labelClass} required={this.props.required}/> }
                <div className={this.props.divClass}>
                    <select className={"form-control" + (this.props.inputClass ? " " + this.props.inputClass : "")}
                            name={this.props.name} disabled={this.props.disabled} value={this.props.value}
                            onBlur={this.onBlur.bind(this)} onChange={this.setValue.bind(this)}>
                        {this.props.children}
                    </select>
                    { hasHint &&
                        <div className="help-block">
                            {hint}
                        </div>
                    }
                </div>
            </FormGroup>
        );
    }
}

class DateTime extends _InputBase {
    constructor(props) {
        super(props);
    }

    setValue(value) {
        const name = this.props.name;
        this.props.onFormChange({
            name: name,
            value: value
        });

        if(this.props.onChange)
            this.props.onChange(value);
    }

    render() {
        const isError = this.state.showErrors && !this.state.isValid;
        const invalidHint = isError && this.props.invalidHint;
        const hint = [this.props.hint, (invalidHint && this.props.hint && <br/>), invalidHint];
        return (
            <FormGroup isError={isError}>
                { this.props.label && <Label name={this.props.label} className={this.props.labelClass} required={this.props.required}/> }
                <div className={this.props.divClass}>
                    <DateTimePicker onChange={this.setValue.bind(this)} value={this.props.value} timezone={this.props.timezone} />
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
