import * as React from "react";

type Props = React.InputHTMLAttributes<HTMLInputElement> & {
  onPressEnter?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
};

class TextField extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    ["onKeyPress"].forEach((method) => (this[method] = this[method].bind(this)));
  }

  onKeyPress(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter" && this.props.onPressEnter) {
      this.props.onPressEnter(event);
    }
  }

  render() {
    const defaultClassName = this.props.className ? this.props.className : "form-control";

    return (
      <input
        id={this.props.id}
        className={defaultClassName}
        value={this.props.value}
        placeholder={this.props.placeholder}
        type="text"
        onChange={(e) => this.props.onChange?.(e)}
        onKeyPress={this.onKeyPress}
      />
    );
  }
}

export { TextField };
