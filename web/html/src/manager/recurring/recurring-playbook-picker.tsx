import { Component } from "react";

import yaml from "js-yaml";

import { AnsiblePathContent } from "manager/minion/ansible/ansible-path-content";

import { AceEditor } from "components/ace-editor";
import { Button } from "components/buttons";
import { BootstrapPanel } from "components/panels";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";

type PropsType = {
  minionServerId: number;
  playbookPath: string;
  inventoryPath: string;
  flushCache: boolean;
  variables?: string;
  onSelectPlaybook: (playbook: any) => void;
};

type StateType = {
  editPlaybook: boolean;
};

class RecurringPlaybookPicker extends Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      editPlaybook: true,
    };
  }

  componentDidMount() {
    if (this.props.playbookPath) {
      this.setState({ editPlaybook: !this.props.playbookPath });
    }
  }

  loadVariables = () => {
    if (!this.props.variables) return "";

    let varsObj;
    if (typeof this.props.variables === "string") {
      try {
        varsObj = JSON.parse(this.props.variables);
      } catch {
        varsObj = {};
      }
    } else {
      varsObj = this.props.variables;
    }
    return yaml.dump(
      { vars: varsObj },
      {
        quotingType: '"',
        forceQuotes: true,
      }
    );
  };

  onEditPlaybook = () => {
    this.setState({
      editPlaybook: true,
    });
  };

  onSelectPlaybook = (playbook) => {
    this.setState({
      editPlaybook: false,
    });
    this.props.onSelectPlaybook(playbook);
  };

  render() {
    const button = (
      <Button
        id="change-btn"
        className="btn-default"
        text={t("Change Playbook")}
        handler={() => this.onEditPlaybook()}
      />
    );

    return this.state.editPlaybook ? (
      <div>
        <h3>{t("Select a Playbook")}</h3>
        <AnsiblePathContent
          minionServerId={this.props.minionServerId}
          pathContentType="playbook"
          recurringDetails={{ fullPath: this.props.playbookPath, variables: this.props.variables }}
          isRecurring={true}
          onSelectPlaybook={this.onSelectPlaybook}
        />
      </div>
    ) : (
      <div>
        <SectionToolbar top="50">
          <div className="action-button-wrapper">{button}</div>
        </SectionToolbar>
        <BootstrapPanel title={t("Playbook details")}>
          <table className="table">
            <tbody>
              <tr>
                <td className="col-sm-3 text-right">
                  <b>{t("Playbook path:")}</b>
                </td>
                <td>{this.props.playbookPath}</td>
              </tr>
              <tr>
                <td className="col-sm-3 text-right">
                  <b>{t("Inventory path:")}</b>
                </td>
                <td>{this.props.inventoryPath ? this.props.inventoryPath : "-"}</td>
              </tr>
              <tr>
                <td className="col-sm-3 text-right">
                  <b>{t("Flush fact cache:")}</b>
                </td>
                <td>{this.props.flushCache ? t("true") : t("false")}</td>
              </tr>
            </tbody>
          </table>
          {this.props.variables && (
            <>
              <h5>{t("Variables")}</h5>
              <AceEditor
                className="form-control"
                id="variables-content"
                minLines={20}
                maxLines={40}
                readOnly={true}
                mode="yaml"
                content={this.loadVariables()}
              />
            </>
          )}
        </BootstrapPanel>
      </div>
    );
  }
}

export { RecurringPlaybookPicker };
