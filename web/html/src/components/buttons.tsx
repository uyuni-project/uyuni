import * as React from "react";

/**
 * Various HTML button components.
 * @module buttons
 */

type BaseProps = {
  /** Text to display on the button. */
  text?: React.ReactNode;

  /**
   * FontAwesome icon class of the button. Can also include additional FA classes
   * (sizing, animation etc.).
   */
  icon?: string;

  /** 'id' attribute of the button. */
  id?: string;

  /** 'title' attribute of the button. */
  title?: string;

  /** If true, disable the button. */
  disabled?: boolean;

  /**
   * Any additional css classes for the button, `"btn"` is prepended automatically
   */
  className?: string;
};

type BaseState = {};

/**
 * Base class for button components.
 */
class _ButtonBase<P extends BaseProps = BaseProps, S extends BaseState = BaseState> extends React.Component<P, S> {
  renderIcon() {
    const margin = this.props.text ? "" : " no-margin";
    const icon = this.props.icon && <i className={"fa " + this.props.icon + margin} />;

    return icon;
  }
}

type AsyncProps = BaseProps & {
  /**
   * The async action function to be performed when clicked.
   * The function is required and must return a Promise object or 'false'.
   * @return {Promise} The asynchronous action.
   */
  action?: (...args: any[]) => Promise<any> | false | void;

  /**
   * One of Bootstrap button type classes (e.g. 'btn-success', 'btn-primary').
   * Defaults to 'btn-default'.
   * @see className
   */
  defaultType?: string;

  /**
   * Initial state of the button ('failure', 'warning' or 'initial')
   */
  initialValue?: string;
};

type AsyncState = {
  value: string;
};

/**
 * A button which performs an asynchronous action and displays an animation while waiting for the result.
 */
export class AsyncButton extends _ButtonBase<AsyncProps, AsyncState> {
  constructor(props: AsyncProps) {
    super(props);
    this.state = {
      value: props.initialValue ? props.initialValue : "initial",
    };
  }

  trigger = () => {
    this.setState({
      value: "waiting",
    });
    const future = this.props.action?.();
    if (!future) {
      this.setState({
        value: "initial",
      });
      return;
    }
    future.then(
      () => {
        this.setState({
          value: "success",
        });
      },
      () => {
        this.setState({
          value: "failure",
        });
      }
    );
  };

  render() {
    let style = "btn ";
    switch (this.state.value) {
      case "failure":
        style += "btn-danger";
        break;
      case "waiting":
      case "initial":
        style += this.props.defaultType ? this.props.defaultType : "btn-default";
        break;
      default:
        style += this.props.defaultType ? this.props.defaultType : "btn-default";
        break;
    }

    if (this.props.className) {
      style += " " + this.props.className;
    }

    const margin = this.props.text ? "" : " no-margin";
    return (
      <button
        id={this.props.id}
        title={this.props.title}
        className={style}
        disabled={this.state.value === "waiting" || this.props.disabled}
        onClick={this.trigger}
      >
        {this.state.value === "waiting" ? (
          <i className={"fa fa-circle-o-notch fa-spin" + margin}></i>
        ) : (
          this.renderIcon()
        )}
        {this.props.text}
      </button>
    );
  }
}

export type ButtonProps = BaseProps & {
  /** Callback function to execute on button click. */
  handler?: (...args: any[]) => any;
};

/**
 * A simple HTML button with an icon and an optional text.
 */
export class Button extends _ButtonBase<ButtonProps> {
  render() {
    return (
      <button
        id={this.props.id}
        type="button"
        title={this.props.title}
        className={"btn " + (this.props.className ?? "")}
        onClick={this.props.handler}
        disabled={this.props.disabled}
      >
        {this.renderIcon()}
        {this.props.text}
      </button>
    );
  }
}

type LinkProps = BaseProps & {
  /** 'href' attribute of the anchor. */
  href?: string;

  /** target of the link */
  target?: string;

  /** Callback function to execute on button click. */
  handler?: (...args: any[]) => any;
};

/**
 * An HTML anchor which displays as a button.
 */
export class LinkButton extends _ButtonBase<LinkProps> {
  render() {
    const targetProps: Partial<React.HTMLProps<HTMLAnchorElement>> =
      this.props.target === "_blank"
        ? {
            target: "_blank",
            rel: "noopener noreferrer",
          }
        : {
            target: this.props.target,
          };
    return (
      <a
        id={this.props.id}
        title={this.props.title}
        className={"btn " + this.props.className}
        href={this.props.href}
        onClick={this.props.handler}
        {...targetProps}
      >
        {this.renderIcon()}
        {this.props.text}
      </a>
    );
  }
}

/**
 * A simple submit button with an icon and an optional text.
 */
export class SubmitButton extends _ButtonBase {
  render() {
    return (
      <button id={this.props.id} type="submit" className={"btn " + this.props.className} disabled={this.props.disabled}>
        {this.renderIcon()}
        {this.props.text}
      </button>
    );
  }
}

type DropdownProps = BaseProps & {
  /** 'dropdown-toggle' and 'btn' classes are always prepended for dropdowns */
  className: string;

  /** Callback function to execute on button click. */
  handler?: (...args: any[]) => any;

  items: React.ReactNode[];
};

/**
 * A bootstrap-style dropdown button.
 */
export class DropdownButton extends _ButtonBase<DropdownProps> {
  render() {
    return (
      <div className="dropdown">
        <button
          id={this.props.id}
          type="button"
          title={this.props.title}
          className={"dropdown-toggle btn " + this.props.className}
          onClick={this.props.handler}
          data-toggle="dropdown"
          disabled={this.props.disabled}
        >
          {this.renderIcon()}
          {this.props.text} <span className="caret" />
        </button>
        <ul className="dropdown-menu dropdown-menu-right">
          {this.props.items.map((i) => (
            <li>{i}</li>
          ))}
        </ul>
      </div>
    );
  }
}
