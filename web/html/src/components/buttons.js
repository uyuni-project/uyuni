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
    const style = this.state.value == "failure" ? "btn btn-danger" : "btn btn-default";
    const margin = this.props.name != undefined ? "" : " no-margin"
    return (
        <button id={this.props.id} title={t(this.props.title)} className={style} disabled={this.state.value == "waiting" || this.props.disabled} onClick={this.trigger}>
           {this.state.value == "waiting" ?
                <i className={"fa fa-circle-o-notch fa-spin" + margin}></i> :
                this.props.icon != undefined ?
                   <i className={"fa fa-" + this.props.icon + margin}></i> :
                   undefined}
           {t(this.props.name)}
        </button>
    );
  }

}


module.exports = {
    AsyncButton : AsyncButton
}
