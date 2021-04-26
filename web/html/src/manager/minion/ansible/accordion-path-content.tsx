import * as React from "react";
import { AnsiblePath } from "./ansible-path-type";
import Network from "utils/network";
import { Messages, Utils } from "components/messages";
import { Loading } from "components/utils/Loading";
import { AceEditor } from "components/ace-editor";

type PropsType = {
  path: AnsiblePath;
};

type StateType = {
  open: boolean; 
  content: any;
  errors: string[];
  loading: boolean;
};

function getURL(path: AnsiblePath) {
  let baseUrl: string;
  if (path.type === "playbook") {
    baseUrl = "/rhn/manager/api/systems/details/ansible/discover-playbooks/";
  }
  else {
    baseUrl = "/rhn/manager/api/systems/details/ansible/introspect-inventory/";
  }
  return baseUrl + path.id;
}

interface PlaybookDetails {
  path: AnsiblePath,
  fullPath: string,
  customInventory?: string
  name: string,
}

class AccordionPathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      open: false,
      content: null,
      errors: [],
      loading: false,
    };
  }

  onToggle() {
    const path: AnsiblePath = this.props.path;
    if (!this.state.open) {
      if (this.state.content === null) {
        this.setState({ loading: true });
        Network.get(getURL(path))
        .promise.then(data => {
          if (!Object.keys(data).includes("success") || data.success) {
            this.setState({
              content: this.props.path.type === "playbook" ?
                this.digestPlaybookPathContent(data)
                :
                this.digestInventoryPathContent(data)
            });
          }
          else {
            this.setState({ errors: data.messages });
          }
          this.setState({ open: true, loading: false });
        });
      }
      else {
        this.setState({ open: true });
      }
    }
    else {
      this.setState({ open: false, errors: [] });
    }
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

  renderPlaybookPathContent() {
    if (this.state.content === null || this.state.content.length === 0) {
      return <div>{t("No Playbook found.")}</div>
    }

    const content: PlaybookDetails[] = this.state.content;
    return content.map((p, i) =>
      <div>
        { i === 0 ? <br/> : null }
        <dl className="row">
          <dt className="col-xs-2">{t('Playbook File Name')}:</dt>
          <dd className="col-xs-8">{p.name}</dd>
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

  renderInventoryPathContent() {
    if (this.state.content === null || this.state.content.length === 0) {
      return <div>{t("Inventory file not found or empty.")}</div>
    }

    return <AceEditor
              className="form-control"
              id="content-state"
              minLines={20}
              maxLines={40}
              readOnly={true}
              mode="yaml"
              content={this.state.content}
            ></AceEditor>;
  }

  render() {
    const header =
      <div className="panel-heading pointer" onClick={() => this.onToggle()}>
        <h6>
          <i className={this.state.open || this.state.loading ? "fa fa-chevron-down" : "fa fa-chevron-right"} />
          { this.props.path.path }
        </h6>
      </div>;

    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div className="panel panel-default">
        {header}
        <div>
          {
            this.state.loading?
              <Loading text={t("Loading content..")} />
              :
              this.state.open ?
                <>
                  {
                    this.state.errors.length > 0 ?
                      errors
                      :
                      this.props.path.type === "playbook" ? this.renderPlaybookPathContent() : this.renderInventoryPathContent()
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
