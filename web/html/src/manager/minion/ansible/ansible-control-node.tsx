import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages } from "components/messages";
import { TextField } from "components/fields";
import { Panel } from "components/panels/Panel";
import { AsyncButton } from "components/buttons";
import Network from "utils/network";

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
  newPlaybookPath: string;
  newInventoryPath: string;
};

class AnsibleControlNode extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      system: props.system,
      messages: [],
      playbooksPaths: ["/usr/share/playbooks", "/srv/playbooks"],
      inventoriesPaths: ["/usr/share/inventories", "/srv/inventories"],
      newPlaybookPath: "",
      newInventoryPath: "",
    };
  }

  newPath(type: string, newPath: string) {
    if (type === "playbook") {
      this.setState({ newPlaybookPath: newPath });
    }
    else {
      this.setState({ newInventoryPath: newPath });
    }
  }

  savePath(type: string) {
    const newPath = type === "playbook" ? this.state.newPlaybookPath : this.state.newInventoryPath;
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/save",
      JSON.stringify({
        minionId: this.state.system.id,
        type: type,
        path: newPath
      }),
      "application/json"
    ).promise.then(data => {
      if (data) {
        if (type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.concat(this.state.newPlaybookPath) });
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.concat(this.state.newInventoryPath) });
        }
      }
    });
  }

  render () {
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
            <h4>{t("Add a Playbook path to discover")}</h4>
            <div className="form-group">
              <TextField placeholder={t("New playbook path")} onChange={(e) => this.newPath("playbook", e.target.value.toString())} />
            </div>
            <div className="pull-right btn-group">
              <AsyncButton
                action={() => this.savePath("playbook")}
                defaultType="btn-success"
                text={t("Save")}
                icon="fa-save"
              />
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
            <h4>{t("Add an Inventory path to discover")}</h4>
            <div className="form-group">
              <TextField placeholder={t("New inventory path")} onChange={(e) => this.newPath("inventory", e.target.value.toString())} />
            </div>
            <div className="pull-right btn-group">
              <AsyncButton
                action={() => this.savePath("inventory")}
                defaultType="btn-success"
                text={t("Save")}
                icon="fa-save"
              />
            </div>
          </Panel>
        </div>
      </div>
    );
  }
}

export const renderer = (id: string, system: any) => SpaRenderer.renderNavigationReact(<AnsibleControlNode system={system} />, document.getElementById(id));
