// @flow

const React = require('react');
const { Text } = require('./Text');
const { Password } = require('./Password');
const { Check } = require('./Check');
const { Select } = require('./Select');
const { DateTime } = require('./DateTime');

type Props = {
  model: Object,
  onSubmit?: Function,
  onSubmitInvalid?: Function,
  formRef?: string,
  className?: string,
  divClass?: string,
  formDirection?: string,
  children: React.Node,
  onChange?: Function,
  onValidate?: Function,
};

type State = {
  model: Object,
  isValid: boolean,
};

class Form extends React.Component<Props, State> {
  static defaultProps = {
    onSubmit: undefined,
    onSubmitInvalid: undefined,
    formRef: undefined,
    divClass: '',
    onChange: undefined,
    onValidate: undefined,
    formDirection: "form-horizontal"
  };

  inputs = {};

  constructor(props: Props) {
    super(props);
    this.state = {
      model: props.model,
      isValid: true,
    };
  }

  // eslint-disable-next-line
  UNSAFE_componentWillReceiveProps(props: Props) {
    this.setState({
      model: props.model,
    });
  }

  onFieldChange(item: Object) {
    const component = this.inputs[item.name];
    const { model } = this.state;
    model[item.name] = item.value;
    this.setState({
      model,
    },
    () => this.validate(component, component.props));

    if (this.props.onChange) {
      this.props.onChange(this.state.model, this.state.isValid);
    }
  }

  validateForm() {
    let allValid = true;

    Object.keys(this.inputs).forEach((name) => {
      if (!this.inputs[name].state.isValid) {
        allValid = false;
      }
    });

    this.setState({
      isValid: allValid,
    });

    if (this.props.onValidate) {
      this.props.onValidate(allValid);
    }
  }

  validate(component: React.ElementRef<any>, props: Object) {
    const { name } = props;
    const results = [];
    let isValid = true;

    if (!props.disabled && (this.state.model[name] || props.required)) {
      if (props.required && !this.state.model[name]) {
        isValid = false;
      } else if (props.validators) {
        const validators = Array.isArray(props.validators) ? props.validators : [props.validators];
        validators.forEach((v) => {
          results.push(Promise.resolve(v(`${this.state.model[name] || ''}`)));
        });
      }
    }

    Promise.all(results).then((result) => {
      result.forEach((r) => {
        isValid = isValid && r;
      });
      component.setState({
        isValid,
      }, this.validateForm.bind(this));
    });
  }

  unregisterInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name) {
      delete this.inputs[component.props.name];

      this.setState((prevState) => {
        const { model } = prevState;
        delete model[component.props.name];
        return { model };
      });
    }
  }

  registerInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name) {
      this.inputs[component.props.name] = component;

      this.setState((prevState) => {
        const { model } = prevState;
        model[component.props.name] = component.props.value;
        return { model };
      },
      () => this.validate(component, component.props));
    }
  }

  submit(event: Object) {
    event.preventDefault();
    if (this.state.isValid && this.props.onSubmit) {
      this.props.onSubmit(this.state.model, event);
    } else if (this.props.onSubmitInvalid) {
      this.props.onSubmitInvalid(this.state.model, event);
    }
  }

  renderChild(child: React.Element<any>) {
    if (typeof child !== 'object' || child === null) {
      return child;
    }


    // TODO [LuNeves] [Cedric] After upgrading react to v16 this could be improved with Context:
    // formPropsProvider and a formPropsConsumer on the component inputBase
    const fieldTypes = [<Text />.type, <Password />.type, <Check />.type, <Select />.type, <DateTime />.type];
    if (fieldTypes.includes(child.type)) {
      const name = child.props && child.props.name;

      if (!name) {
        throw new Error('Can not add input without "name" attribute');
      }

      const newProps = {
        registerInput: this.registerInput.bind(this),
        unregisterInput: this.unregisterInput.bind(this),
        validate: this.validate.bind(this),
        onFormChange: this.onFieldChange.bind(this),
        invalidHint: child.props.invalidHint || (child.props.required && (`${child.props.label} is required.`)),
        value: this.state.model[name] || child.props.defaultValue || '',
      };

      return React.cloneElement(child, Object.assign({}, child.props, newProps), child.props.children);
    }
    const renderedChildren = this.renderChildren(child.props && child.props.children);
    return React.cloneElement(child, child.props, renderedChildren);
  }

  renderChildren(children: React.Node) {
    if (typeof children !== 'object' || children == null) {
      return children;
    }
    return React.Children.map(children, child => this.renderChild(child));
  }

  render() {
    return (
      <form ref={this.props.formRef} onSubmit={this.submit.bind(this)} className={this.props.className}>
        <div className={`${this.props.formDirection || ''} ${this.props.divClass ? ` ${this.props.divClass}` : ''}`}>
          {this.renderChildren(this.props.children)}
        </div>
      </form>
    );
  }
}

module.exports = {
  Form,
};
