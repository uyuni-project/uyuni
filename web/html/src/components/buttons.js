'use strict';

var React = require("react");

class AsyncButton extends React.Component {

  constructor(props) {
    super();
    ["trigger"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        value: "initial"
    };
  }

  trigger() {
    this.setState({
        value: "waiting"
    });
    const future = this.props.action();
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
    const style = this.state.value == "failure" ? "btn btn-danger" : "btn btn-default";
    return (
        <button id={this.props.id} className={style} disabled={this.state.value == "waiting" || this.props.disabled} onClick={this.trigger}>
           {this.state.value == "waiting" ? <i className="fa fa-circle-o-notch fa-spin"></i> : undefined}
           {t(this.props.name)}
        </button>
    );
  }

}


module.exports = {
    AsyncButton : AsyncButton
}
