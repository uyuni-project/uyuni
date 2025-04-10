import * as React from "react";

import { AnsiblePathContent } from "manager/minion/ansible/ansible-path-content";

import { Button } from "components/buttons";
import { BootstrapPanel } from "components/panels";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";

type PropsType = {
  minionServerId: number;
  playbookPath: string;
  inventoryPath: string;
  flushCache: boolean;
  onSelectPlaybook: (playbook: any) => void;
};

type StateType = {
  editPlaybook: boolean;
};

class RecurringPlaybookPicker extends React.Component<PropsType, StateType> {
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
    let button = (
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
          isRecurring={true}
          onSelectPlaybook={this.onSelectPlaybook}
        />
      </div>
    ) : (
      <div>
        <SectionToolbar>
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
        </BootstrapPanel>
      </div>
    );
  }
}

export { RecurringPlaybookPicker };
