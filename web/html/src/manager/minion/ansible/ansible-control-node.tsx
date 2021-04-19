import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages } from "components/messages";

type Minion = {
  id: Number;
  name: String;
}

type PropsType = {
  system: Minion;
};

type StateType = {
  system: Minion;
  messages: any[];
  playbooksPaths: string[];
  inventoriesPaths: string[];
};

class AnsibleControlNode extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      system: props.system,
      messages: [],
      playbooksPaths: ["/usr/share/playbooks", "/srv/playbooks"],
      inventoriesPaths: ["/usr/share/inventories", "/srv/inventories"],
    };
  }
  
  render () {
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;
    const buttons = [];
    const buttonsLeft = [];
    return (
      <div>
        {messages}
        {this.state.system.name} - {this.state.system.id}
        {this.state.playbooksPaths.map(p => <div>{p}</div>)}
        {this.state.inventoriesPaths.map(p => <div>{p}</div>)}
      </div>
    );
  }
}

export const renderer = (id: string, system: any) => SpaRenderer.renderNavigationReact(<AnsibleControlNode system={system} />, document.getElementById(id));
