import * as React from "react";

/** @module toggler */

type Props = {
  /** Callback function to execute on toggle switch. */
  handler: (...args: any[]) => any;

  /** Text to display on the toggler. */
  text?: React.ReactNode;

  /** The boolean value represented by the toggler. */
  value?: boolean;

  /** If true, the component will be rendered as disabled. */
  disabled?: boolean;

  /** className of the component. */
  className?: string;
};

/**
 * A customized toggle switch element to represent boolean values.
 */
class Toggler extends React.Component<Props> {
  render() {
    let classes = this.props.disabled ? "text-muted" : "pointer";

    if (this.props.className) {
      classes += " " + this.props.className;
    }
    return (
      <span onClick={this.props.handler} className={classes}>
        <i className={"v-middle fa " + (this.props.value ? "fa-toggle-on text-success" : "fa-toggle-off")} />
        &nbsp;
        <span className="v-middle">{this.props.text}</span>
      </span>
    );
  }
}

export { Toggler };
