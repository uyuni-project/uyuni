import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { TextField } from "components/fields";
import { Panel } from "components/panels/Panel";
import { AsyncButton, Button } from "components/buttons";
import Network from "utils/network";
import NewAnsiblePath from "./new-ansible-path";
import EditAnsiblePath from "./edit-ansible-path";

type AnsiblePath = {
  id?: number;
  minionServerId?: number;
  type?: string;
  path?: string;
}

type PropsType = {
  minionServerId: number;
};

type StateType = {
  minionServerId: number;
  playbooksPaths: AnsiblePath[];
  inventoriesPaths: AnsiblePath[];
  newPlaybookPath: string;
  newInventoryPath: string;
  editPlaybookPath?: AnsiblePath;
  editInventoryPath?: AnsiblePath;
  errors: string[];
};

class AnsibleControlNode extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      minionServerId: props.minionServerId,
      playbooksPaths: [],
      inventoriesPaths: [],
      newPlaybookPath: "",
      newInventoryPath: "",
      editPlaybookPath: undefined,
      editInventoryPath: undefined,
      errors: [],
    };

    Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.minionServerId)
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

  deletePath(path: AnsiblePath) {
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/delete",
      JSON.stringify(path.id),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        if (path.type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.filter(p => p.id != path.id) });
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.filter(p => p.id != path.id) });
        }
      }
      else {
        this.setState({ errors: data.errors.path });
      }
    });
  }

  editPath(path: AnsiblePath, newValue: string ) {
    path.path = newValue;
    if (path.type === "playbook") {
      this.setState({ editPlaybookPath: path });
    }
    else {
      this.setState({ editInventoryPath: path });
    }
  }

  saveEditPath(type?: string) {
    const editPath = type === "playbook" ? this.state.editPlaybookPath : this.state.editInventoryPath;
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/save",
      JSON.stringify({
        minionServerId: editPath?.minionServerId,
        type: editPath?.type,
        path: editPath?.path,
        id: editPath?.id
      }),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        const newAnsiblePath: AnsiblePath = { id: editPath?.id, minionServerId: editPath?.minionServerId, type: editPath?.type, path: editPath?.path};
        if (type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.filter(p => p.id != editPath?.id).concat(newAnsiblePath), editPlaybookPath: undefined});
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.filter(p => p.id != editPath?.id).concat(newAnsiblePath), editInventoryPath: undefined });
        }
      }
      else {
        this.setState({ errors: data.errors.path });
      }
    });
  }

  savePath(type: string) {
    const newPath = type === "playbook" ? this.state.newPlaybookPath : this.state.newInventoryPath;
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/save",
      JSON.stringify({
        minionServerId: this.state.minionServerId,
        type: type,
        path: newPath
      }),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        const newAnsiblePath = { id: data.newPathId, minionServerId: this.state.minionServerId, type: type, path: newPath};
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
        <p>
          {t("Ansible Control Node Configuration: add Paths to discover Playbooks and Inventories.")}
        </p>
        <div className="col-md-6">
          <Panel
            headingLevel="h3"
            title="Playbooks Paths"
          >
            {this.state.playbooksPaths.map(p =>
                this.state.editPlaybookPath?.path == p.path ?
                  <EditAnsiblePath
                    key={p.id}
                    ansiblePath={p}
                    editPath={(newValue: string) => this.editPath(p, newValue)}
                    saveEditPath={() => this.saveEditPath(p.type)}
                    editPlaybookPath={this.state.editPlaybookPath}
                    cancelHandler={() => this.setState({ editPlaybookPath: undefined })}
                    deletePath={() => this.deletePath(p)}
                  />
                  :
                  <div className="d-block" key={p.id}>
                    <pre className="pointer" onClick={() => this.setState({ editPlaybookPath: p })}>
                      {p.path}<i className="fa fa-edit pull-right" />
                    </pre>
                  </div>
            )}
            <hr/>
            <NewAnsiblePath
              title={t("Add a Playbook path to discover")}
              pathType="playbook"
              newInventoryPath={this.state.newPlaybookPath}
              newPath={(path: string) => this.newPath("playbook", path)}
              savePath={() => this.savePath("playbook")}
            />
          </Panel>
        </div>
        <div className="col-md-6">
          <Panel
            headingLevel="h3"
            title="Inventories Paths"
          >
            {this.state.inventoriesPaths.map(p =>
                this.state.editInventoryPath?.path == p.path ?
                  <EditAnsiblePath
                    key={p.id}
                    ansiblePath={p}
                    editPath={(newValue: string) => this.editPath(p, newValue)}
                    saveEditPath={() => this.saveEditPath(p.type)}
                    editEntity={this.state.editInventoryPath}
                    cancelHandler={() => this.setState({ editInventoryPath: undefined })}
                    deletePath={() => this.deletePath(p)}
                  />
                  :
                  <div className="d-block" key={p.id}>
                    <pre className="pointer" onClick={() => this.setState({ editInventoryPath: p })}>
                      {p.path}<i className="fa fa-edit pull-right" />
                    </pre>
                  </div>
            )}
            <hr/>
            <NewAnsiblePath
              title={t("Add an Inventory path to discover")}
              pathType="inventory"
              newInventoryPath={this.state.newInventoryPath}
              newPath={(path: string) => this.newPath("inventory", path)}
              savePath={() => this.savePath("inventory")}
            />
          </Panel>
        </div>
      </div>
    );
  }
}

export const renderer = (renderId: string, { id }) => SpaRenderer.renderNavigationReact(<AnsibleControlNode minionServerId={ id } />, document.getElementById(renderId));
