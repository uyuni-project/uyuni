import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages } from "components/messages";
import { TextField } from "components/fields";
import { Panel } from "components/panels/Panel";
import { AsyncButton } from "components/buttons";

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

  addPath(type: string, newPath: string) {
    if (type === "playbook") {
      this.setState({ playbooksPaths: this.state.playbooksPaths.concat(newPath)});
    }
    else {
      this.setState({ inventoriesPaths: this.state.inventoriesPaths.concat(newPath)});
    }
  }
  
  render () {
    console.log(this.state.playbooksPaths);
    console.log(this.state.inventoriesPaths);
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;
    return (
      <div>
        {messages}
        <div className="col-md-6">
          <Panel
            headingLevel="h3"
            title="Playbooks Paths"
          >
            {this.state.playbooksPaths.map(p =>
              <pre>
                {p}
              </pre>
            )}
            <hr/>
            <div className="form-group">
              <TextField placeholder={t("New playbook path")} onPressEnter={(e) => this.addPath("playbook", e.target.value.toString())} />
            </div>
            <div className="pull-right btn-group">
              <AsyncButton text={t("Save")} icon="fa-save" className="btn-success" />
            </div>
          </Panel>
        </div>
        <div className="col-md-6">
          <Panel
            headingLevel="h3"
            title="Inventories Paths"
          >
            {this.state.inventoriesPaths.map(p =>
              <pre>
                {p}
              </pre>
            )}
            <hr/>
            <div className="form-group">
              <TextField placeholder={t("New inventory path")} onPressEnter={(e) => this.addPath("inventory", e.target.value.toString())} />
            </div>
            <div className="pull-right btn-group">
              <AsyncButton text={t("Save")} icon="fa-save" className="btn-success" />
            </div>
          </Panel>
        </div>
      </div>
    );
  }
}

export const renderer = (id: string, system: any) => SpaRenderer.renderNavigationReact(<AnsibleControlNode system={system} />, document.getElementById(id));
