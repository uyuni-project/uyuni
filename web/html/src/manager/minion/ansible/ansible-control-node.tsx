import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import NewAnsiblePath from "./new-ansible-path";
import EditAnsiblePath from "./edit-ansible-path";
import { AnsiblePath, createNewAnsiblePath } from "./ansible-path-type";
import { Loading } from "components/utils/Loading";

type PropsType = {
  minionServerId: number;
};

type StateType = {
  minionServerId: number;
  playbooksPaths: AnsiblePath[];
  inventoriesPaths: AnsiblePath[];
  newPlaybookPath: string;
  newInventoryPath: string;
  editPlaybookPath: Partial<AnsiblePath>;
  editInventoryPath: Partial<AnsiblePath>;
  errors: string[];
  loading: boolean;
};

export class AnsibleControlNode extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      minionServerId: props.minionServerId,
      playbooksPaths: [],
      inventoriesPaths: [],
      newPlaybookPath: "",
      newInventoryPath: "",
      editPlaybookPath: {},
      editInventoryPath: {},
      errors: [],
      loading: true,
    };

    Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.minionServerId)
    .then(blob => {
      if (blob.success) {
        const data: AnsiblePath[] = blob.data;
        this.setState({ playbooksPaths: data.filter(p => p.type === "playbook"), inventoriesPaths: data.filter(p => p.type === "inventory"), loading: false });
      }
      else {
        this.setState({ errors: [t("An error occurred while loading data. Please check server logs.")], loading: false});
      }
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
      path.id?.toString()
    ).then(blob => {
      if (blob.success) {
        if (path.type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.filter(p => p.id !== path.id) });
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.filter(p => p.id !== path.id) });
        }
      }
      else {
        this.setState({ errors: blob.errors.path });
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

  saveEditPath(type: string) {
    const editPath: Partial<AnsiblePath> = type === "playbook" ? this.state.editPlaybookPath : this.state.editInventoryPath;
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/save",
      {
        minionServerId: editPath?.minionServerId,
        type: editPath?.type,
        path: editPath?.path,
        id: editPath?.id
      },
    ).then(blob => {
      if (blob.success) {
        const newPath = createNewAnsiblePath({ id: editPath.id, minionServerId: editPath.minionServerId, type: editPath.type, path: editPath.path});
        if (type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.filter(p => p.id !== editPath?.id).concat(newPath), editPlaybookPath: {}});
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.filter(p => p.id !== editPath?.id).concat(newPath), editInventoryPath: {} });
        }
      }
      else {
        this.setState({ errors: blob.errors.path });
      }
    });
  }

  savePath(type: string) {
    const newPath = type === "playbook" ? this.state.newPlaybookPath : this.state.newInventoryPath;
    Network.post(
      "/rhn/manager/api/systems/details/ansible/paths/save",
      {
        minionServerId: this.state.minionServerId,
        type: type,
        path: newPath
      },
    ).then(blob => {
      if (blob.success) {
        const newAnsiblePath = { id: blob.data.pathId, minionServerId: this.state.minionServerId, type: type, path: newPath};
        if (type === "playbook") {
          this.setState({ playbooksPaths: this.state.playbooksPaths.concat(newAnsiblePath), newPlaybookPath: "" });
        }
        else {
          this.setState({ inventoriesPaths: this.state.inventoriesPaths.concat(newAnsiblePath), newInventoryPath: "" });
        }
      }
      else {
        this.setState({ errors: blob.errors.path });
      }
    });
  }

  render () {
    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div>
        {errors}
        <p>
          {t("Ansible Control Node Configuration: add paths for Playbook discovery and Inventory files introspection.")}
        </p>
        {
          this.state.loading?
            <Loading text="Loading.." />
            :
            <div>
              <div className="col-md-6">
                <Panel
                  headingLevel="h3"
                  title={t("Playbook Directories")}
                >
                  {this.state.playbooksPaths.map(p =>
                      this.state.editPlaybookPath?.path === p.path ?
                        <EditAnsiblePath
                          key={p.id}
                          ansiblePath={p}
                          editPath={(newValue: string) => this.editPath(p, newValue)}
                          saveEditPath={() => this.saveEditPath(p.type)}
                          editPlaybookPath={this.state.editPlaybookPath}
                          cancelHandler={() => this.setState({ editPlaybookPath: {} })}
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
                    title={t("Add a Playbook directory")}
                    pathType="playbook"
                    newPathValue={this.state.newPlaybookPath}
                    placeholder={t("e.g., /srv/playbooks")}
                    newPath={(path: string) => this.newPath("playbook", path)}
                    savePath={() => this.savePath("playbook")}
                  />
                </Panel>
              </div>
              <div className="col-md-6">
                <Panel
                  headingLevel="h3"
                  title={t("Inventory Files")}
                >
                  {this.state.inventoriesPaths.map(p =>
                      this.state.editInventoryPath?.path === p.path ?
                        <EditAnsiblePath
                          key={p.id}
                          ansiblePath={p}
                          editPath={(newValue: string) => this.editPath(p, newValue)}
                          saveEditPath={() => this.saveEditPath(p.type)}
                          editEntity={this.state.editInventoryPath}
                          cancelHandler={() => this.setState({ editInventoryPath: {} })}
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
                    title={t("Add an Inventory file")}
                    pathType="inventory"
                    newInventoryPath={this.state.newInventoryPath}
                    placeholder={t("e.g., /etc/ansible/testing/hosts")}
                    newPath={(path: string) => this.newPath("inventory", path)}
                    savePath={() => this.savePath("inventory")}
                  />
                </Panel>
              </div>
            </div>
        }
      </div>
    );
  }
}

export const renderer = (renderId: string, { id }) => SpaRenderer.renderNavigationReact(<AnsibleControlNode minionServerId={ id } />, document.getElementById(renderId));
