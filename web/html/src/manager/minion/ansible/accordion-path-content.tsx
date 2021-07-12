import * as React from "react";
import { AnsiblePath } from "./ansible-path-type";
import Network from "utils/network";
import { Messages, Utils } from "components/messages";
import { Loading } from "components/utils/Loading";
import { AceEditor } from "components/ace-editor";
import { Button } from "components/buttons";

type PropsType = {
  path: AnsiblePath;
  onSelectPlaybook: (playbook: PlaybookDetails | null) => void
};

type StateType = {
  isOpen: boolean;
  content: PlaybookDetails[] | InventoryDetails | null;
  errors: string[];
  loading: boolean;
};

function getURL(path: AnsiblePath) {
  let baseUrl: string;
  if (isPlaybook(path)) {
    baseUrl = "/rhn/manager/api/systems/details/ansible/discover-playbooks/";
  }
  else {
    baseUrl = "/rhn/manager/api/systems/details/ansible/introspect-inventory/";
  }
  return baseUrl + path.id;
}

function isPlaybook(path: AnsiblePath) {
  return path.type === "playbook";
}

export interface PlaybookDetails {
  path: AnsiblePath,
  fullPath: string,
  customInventory?: string,
  name: string,
}

interface Server {
  id: number;
  name: string;
}

interface InventoryDetails {
  dump: String;
  knownSystems: Server[];
  unknownSystems: String[];
}

class AccordionPathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      isOpen: false,
      content: null,
      errors: [],
      loading: false,
    };
  }

  onToggle() {
    const path: AnsiblePath = this.props.path;
    if (!this.state.isOpen) {
      if (this.state.content === null) {
        this.setState({ loading: true });
        Network.get(getURL(path))
        .then(blob => {
          if (blob.success) {
            this.setState({ content:
              isPlaybook(path) ?
                this.digestPlaybookPathContent(blob.data)
                :
                this.digestInventoryPathContent(blob.data)
           });
          }
          else {
            this.setState({ errors: blob.messages });
          }
          this.setState({ isOpen: true, loading: false });
        });
      }
      else {
        this.setState({ isOpen: true });
      }
    }
    else {
      this.setState({ isOpen: false, errors: [] });
    }
  }

  isPlaybookContent(input: any): input is PlaybookDetails[] {
    return isPlaybook(this.props.path);
  }

  isInventoryContent(input: any): input is InventoryDetails {
    return true;
  }

  digestPlaybookPathContent(blob: any) {
    const content: Map<string, {}> = blob[this.props.path.path];
    const playbookNameList: string[] = Object.keys(content);

    const playbookDetailsList: PlaybookDetails[] = [];
    playbookNameList.forEach(playbookName => {
        const playbookObj = content[playbookName];
        const playbookObjKeys = Object.keys(playbookObj);
        const newPlaybookObject: PlaybookDetails = {
          name: playbookName,
          fullPath: playbookObjKeys.includes("fullpath") ? playbookObj["fullpath"] : "-",
          path: this.props.path,
          customInventory: playbookObjKeys.includes("custom_inventory") ? playbookObj["custom_inventory"] : "-"
        }
        playbookDetailsList.push(newPlaybookObject);
      }
    );

    return playbookDetailsList;
  }

  renderPlaybookPathContent(content: PlaybookDetails[] | null) {
    if (!content?.length) {
      return <div>{t("No Playbook found.")}</div>
    }

    return content.map((p, i) =>
      <div key={p.toString() + "_" + i.toString()}>
        { i === 0 ? <br/> : null }
        <dl className="row">
          <dt className="col-xs-2">{t('Playbook File Name')}:</dt>
          <dd className="col-xs-8">
            <Button
              icon="fa-file-text-o"
              text={p.name}
              handler={() => this.props.onSelectPlaybook(p)}
              className="btn-link btn-sm" />
          </dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t('Full Path')}:</dt>
          <dd className="col-xs-8">{p.fullPath}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t('Custom Inventory')}:</dt>
          <dd className="col-xs-8">{p.customInventory}</dd>
        </dl>
        { i < content.length -1 ? <hr/> : null }
      </div>
    );
  }

  digestInventoryPathContent(blob: any) {
    return blob
  }

  renderInventoryPathContent(content: InventoryDetails | null) {
    if (!content?.dump) {
      return <div>{t("Inventory file empty.")}</div>
    }

    return (
      <div>
        <br/>
        <dl className="row">
          <dt className="col-xs-2">{t("Registered Systems")}:</dt>
          <dd className="col-xs-8">
            <ul>
              { content?.knownSystems.map(s => <li key={s.id + "_" + s.name}><a href={"/rhn/systems/details/Overview.do?sid=" + s.id}>{s.name}</a></li>) }
            </ul>
          </dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t("Unknown Hostnames")}:</dt>
          <dd className="col-xs-8">
            <ul>
              { content?.unknownSystems.map(s => <li key={s + "_hostname"}>{s}</li>)}
            </ul>
          </dd>
        </dl>
        <AceEditor
              className="form-control"
              id="content-state"
              minLines={20}
              maxLines={40}
              readOnly={true}
              mode="yaml"
              content={content?.dump}
            ></AceEditor>
      </div>
    );
  }

  render() {
    const header =
      <button className="div-button panel-heading" onClick={() => this.onToggle()}>
        <i className={this.state.isOpen || this.state.loading ? "fa fa-chevron-down" : "fa fa-chevron-right"} />
        { this.props.path.path }
      </button>;

    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div className="panel panel-default">
        {header}
        <div>
          {
            this.state.loading?
              <Loading text={t("Loading content..")} />
              :
              this.state.isOpen ?
                <>
                  {
                    this.state.errors.length > 0 ?
                      errors
                      :
                      this.isPlaybookContent(this.state.content) ?
                        this.renderPlaybookPathContent(this.state.content)
                        :
                        this.isInventoryContent(this.state.content) ?
                          this.renderInventoryPathContent(this.state.content)
                          :
                          null
                  }
                </>
                : null
          }
        </div>
      </div>
    )
  }
};

export default AccordionPathContent;
