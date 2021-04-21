import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { TextField } from "components/fields";
import { Panel } from "components/panels/Panel";
import { AsyncButton } from "components/buttons";
import Network from "utils/network";

type AnsiblePath = {
  id: Number;
  minionServerId: Number;
  type: String;
  path: String;
}

type PropsType = {
  id: Number;
};

type StateType = {
  systemId: Number;
  playbooksPaths: AnsiblePath[];
  inventoriesPaths: AnsiblePath[];
  newPlaybookPath: string;
  newInventoryPath: string;
  errors: string[];
};

class AnsibleControlNode extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      systemId: props.system.id,
      playbooksPaths: [],
      inventoriesPaths: [],
      newPlaybookPath: "",
      newInventoryPath: "",
      errors: [],
    };

    Network.get("/rhn/manager/api/systems/details/ansible/paths/"+props.system.id)
    .promise.then(data => {
      this.setState({ playbooksPaths: data.filter(p => p.type === "playbook"), inventoriesPaths: data.filter(p => p.type === "inventory") });
    });
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
        minionServerId: this.state.systemId,
        type: type,
        path: newPath
      }),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        const newAnsiblePath = { id: data.newPathId, minionServerId: this.state.systemId, type: type, path: newPath};
        if (type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.concat(newAnsiblePath), newPlaybookPath: "" });
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.concat(newAnsiblePath), newInventoryPath: "" });
        }
      }
      else {
        this.setState({ errors: data.errors.path });
      }
    });
  }

  render () {
    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div>
        {errors}
        <div className="col-md-6">
          <Panel
            headingLevel="h3"
            title="Playbooks Paths"
          >
            {this.state.playbooksPaths.map(p =>
              <pre key={p.id.toString()}>
                {p.path}
              </pre>
            )}
            <hr/>
            <h4>{t("Add a Playbook path to discover")}</h4>
            <div className="form-group">
              <TextField placeholder={t("New playbook path")} value={this.state.newPlaybookPath} onChange={(e) => this.newPath("playbook", e.target.value.toString())} />
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
              <pre key={p.id.toString()}>
                {p.path}
              </pre>
            )}
            <hr/>
            <h4>{t("Add an Inventory path to discover")}</h4>
            <div className="form-group">
              <TextField placeholder={t("New inventory path")} value={this.state.newInventoryPath} onChange={(e) => this.newPath("inventory", e.target.value.toString())} />
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
