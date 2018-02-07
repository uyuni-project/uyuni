var React = require("react");

class AsyncButton extends React.Component {

  constructor(props) {
    super(props);
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
    let style = "btn ";
    switch (this.state.value) {
        case "failure": style += "btn-danger"; break;
        case "waiting": style += "btn-default"; break;
        case "initial": style += this.props.defaultType ? this.props.defaultType : "btn-default"; break;
        default: style += this.props.defaultType ? this.props.defaultType : "btn-default"; break;
    }
    const margin = this.props.name !== undefined ? "" : " no-margin"
    return (
        <button id={this.props.id} title={this.props.title} className={style} disabled={this.state.value === "waiting" || this.props.disabled} onClick={this.trigger}>
           {this.state.value === "waiting" ?
                <i className={"fa fa-circle-o-notch fa-spin" + margin}></i> :
                this.props.icon !== undefined ?
                   <i className={"fa fa-" + this.props.icon + margin}></i> :
                   undefined}
           {this.props.name}
        </button>
    );
  }

}

class Button extends React.Component {
    render() {
        const margin = this.props.text !== undefined ? "" : " no-margin"
        return (
            <button id={this.props.id} type="button" title={this.props.title} className={'btn ' + this.props.className} onClick={this.props.handler} disabled={this.props.disabled}>
                <i className={'fa ' + this.props.icon + margin}/>{this.props.text}
            </button>
        )
    }
}

class LinkButton extends React.Component {
    render() {
        const margin = this.props.text !== undefined ? "" : " no-margin"
        var icon = this.props.icon ?
            <i className={'fa ' + this.props.icon + margin}/> :
            null;
        return (
            <a id={this.props.id} title={this.props.title} className={'btn ' + this.props.className} href={this.props.href}>
                {icon}
                {this.props.text}
            </a>
        )
    }

}

class SubmitButton extends React.Component {
    render() {
        return (
            <button id={this.props.id} type="submit" className={'btn ' + this.props.className} disabled={this.props.disabled}>
                <i className={'fa ' + this.props.icon}/>{this.props.text}
            </button>
        )
    }
}

function DropdownButton(props) {
    return (
        <div className="dropdown">
            <button
                id={props.id}
                type="button"
                title={props.title}
                className={'dropdown-toggle btn ' + props.className}
                onClick={props.handler} data-toggle="dropdown"
            >
                <i className={'fa ' + props.icon}/>{props.text} <span className="caret"/>
            </button>
            <ul className="dropdown-menu dropdown-menu-right">
                {props.items.map(i => <li>{i}</li>)}
            </ul>
        </div>
    );
}

module.exports = {
    Button : Button,
    LinkButton : LinkButton,
    AsyncButton : AsyncButton,
    SubmitButton : SubmitButton,
    DropdownButton : DropdownButton
}
