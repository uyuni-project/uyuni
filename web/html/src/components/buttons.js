const PropTypes = require('prop-types');
/* eslint-disable */
const React = require("react");

/**
 * Various HTML button components.
 * @module buttons
 */

/**
 * Base class for button components.
 */
class _ButtonBase extends React.Component {
  constructor(props) {
    super(props);
  }

  renderIcon() {
    const margin = this.props.text ? "" : " no-margin";
    const icon = this.props.icon && <i className={'fa ' + this.props.icon + margin}/>;

    return icon;
  }
}

/**
 * A button which performs an asynchronous action and displays an animation while waiting for the result.
 */
class AsyncButton extends _ButtonBase {

  constructor(props) {
    super(props);
    ["trigger"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        value: props.initialValue ? props.initialValue : "initial"
    };
  }

  trigger() {
    this.setState({
        value: "waiting"
    });
    const future = this.props.action();
    if (!future) {
        this.setState({
            value: "initial"
        });
        return;
    }
    future.then(
      () => {
        this.setState({
            value: "success"
        });
      },
      () => {
        this.setState({
            value: "failure"
        });
      }
    );
  }

  render() {
    let style = "btn ";
    switch (this.state.value) {
        case "failure": style += "btn-danger"; break;
        case "waiting": style += "btn-default"; break;
        case "initial": style += this.props.defaultType ? this.props.defaultType : "btn-default"; break;
        default: style += this.props.defaultType ? this.props.defaultType : "btn-default"; break;
    }

    if (this.props.className) {
      style += " " + this.props.className;
    }

    const margin = this.props.text ? "" : " no-margin";
    return (
        <button id={this.props.id} title={this.props.title} className={style} disabled={this.state.value === "waiting" || this.props.disabled} onClick={this.trigger}>
           {this.state.value === "waiting" ? <i className={"fa fa-circle-o-notch fa-spin" + margin}></i> : this.renderIcon()}
           {this.props.text}
        </button>
    );
  }

}

AsyncButton.propTypes = {
  /**
   * The async action function to be performed when clicked.
   * The function is required and must return a Promise object or 'false'.
   * @return {Promise} The asynchronous action.
   */
  action: PropTypes.func.isRequired,
  /**
   * One of Bootstrap button type classes (e.g. 'btn-success', 'btn-primary').
   * Defaults to 'btn-default'.
   */
  defaultType: PropTypes.string,
  /** Text to display on the button. */
  text: PropTypes.string,
  /** 'id' attribute of the button. */
  id: PropTypes.string,
  /** 'title' attribute of the button. */
  title: PropTypes.string,
  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon: PropTypes.string,
  /** If true, disable the button. */
  disabled: PropTypes.bool,
  /**
   * Any additional css classes for the button.
   * @see defaultType
   */
  className: PropTypes.string,
  /**
   * Initial state of the button ('failure', 'warning' or 'initial')
   */
  initialValue: PropTypes.string
};

/**
 * A simple HTML button with an icon and an optional text.
 */
class Button extends _ButtonBase {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <button id={this.props.id} type="button" title={this.props.title} className={'btn ' + this.props.className} onClick={this.props.handler} disabled={this.props.disabled}>
        {this.renderIcon()}{this.props.text}
      </button>
    )
  }
}

Button.propTypes = {
  /** Callback function to execute on button click. */
  handler: PropTypes.func,
  /** Text to display on the button. */
  text: PropTypes.string,
  /** 'id' attribute of the button. */
  id: PropTypes.string,
  /** 'title' attribute of the button. */
  title: PropTypes.string,
  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon: PropTypes.string,
  /** If true, disable the button. */
  disabled: PropTypes.bool,
  /** className of the button. 'btn' class is always prepended. */
  className: PropTypes.string
};

/**
 * An HTML anchor which displays as a button.
 */
class LinkButton extends _ButtonBase {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <a id={this.props.id} title={this.props.title} className={'btn ' + this.props.className} href={this.props.href} target={this.props.target}>
        {this.renderIcon()}{this.props.text}
      </a>
    )
  }
}

LinkButton.props = {
  /** 'href' attribute of the anchor. */
  href: PropTypes.string,
  /** Text to display on the button. */
  text: PropTypes.string,
  /** 'id' attribute of the button. */
  id: PropTypes.string,
  /** 'title' attribute of the button. */
  title: PropTypes.string,
  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon: PropTypes.string,
  /** If true, disable the button. */
  disabled: PropTypes.bool,
  /** className of the button. 'btn' class is always prepended. */
  className: PropTypes.string,
  /** target of the link. */
  target: PropTypes.string,
};

/**
 * A simple submit button with an icon and an optional text.
 */
class SubmitButton extends _ButtonBase {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <button id={this.props.id} type="submit" className={'btn ' + this.props.className} disabled={this.props.disabled}>
        {this.renderIcon()}{this.props.text}
      </button>
    )
  }
}

SubmitButton.props = {
  /** Text to display on the button. */
  text: PropTypes.string,
  /** 'id' attribute of the button. */
  id: PropTypes.string,
  /** 'title' attribute of the button. */
  title: PropTypes.string,
  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon: PropTypes.string,
  /** If true, disable the button. */
  disabled: PropTypes.bool,
  /** className of the button. 'btn' class is always prepended. */
  className: PropTypes.string
};

/**
 * A bootstrap-style dropdown button.
 */
class DropdownButton extends _ButtonBase {
  constructor(props) {
    super(props);
  }

  render() {
    return (
        <div className="dropdown">
            <button
                id={this.props.id}
                type="button"
                title={this.props.title}
                className={'dropdown-toggle btn ' + this.props.className}
                onClick={this.props.handler} data-toggle="dropdown"
                disabled={this.props.disabled}
            >
              {this.renderIcon()}{this.props.text} <span className="caret"/>
            </button>
            <ul className="dropdown-menu dropdown-menu-right">
                {this.props.items.map(i => <li>{i}</li>)}
            </ul>
        </div>
    );
  }
}

DropdownButton.props = {
  /** An array of dropdown elements. */
  items: PropTypes.arrayOf(PropTypes.element),
  /** Callback function to execute on button click. */
  handler: PropTypes.func,
  /** Text to display on the button. */
  text: PropTypes.string,
  /** 'id' attribute of the button. */
  id: PropTypes.string,
  /** 'title' attribute of the button. */
  title: PropTypes.string,
  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon: PropTypes.string,
  /** If true, disable the button. */
  disabled: PropTypes.bool,
  /** className of the button. 'dropdown-toggle' and 'btn' classes are always prepended. */
  className: PropTypes.string
};

module.exports = {
    Button : Button,
    LinkButton : LinkButton,
    AsyncButton : AsyncButton,
    SubmitButton : SubmitButton,
    DropdownButton : DropdownButton
}
