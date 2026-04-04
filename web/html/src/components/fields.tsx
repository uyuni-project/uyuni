import { type InputHTMLAttributes, type KeyboardEvent, Component } from "react";
type Props = InputHTMLAttributes<HTMLInputElement> & {
  onPressEnter?: (event: KeyboardEvent<HTMLInputElement>) => void;
};

class TextField extends Component<Props> {
  onKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter" && this.props.onPressEnter) {
      this.props.onPressEnter(event);
    }
  };

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
