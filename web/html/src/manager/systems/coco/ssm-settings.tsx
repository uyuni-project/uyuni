import * as React from "react";

import CoCoSettingsForm from "components/coco-attestation/CoCoSettingsForm";
import { Settings } from "components/coco-attestation/Utils";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SystemData, TargetSystems } from "components/target-systems";

import Network from "utils/network";

type Props = {
  systemSupport: SystemData[];
  availableEnvironmentTypes: object;
};

type State = {
  messages: MessageType[];
};

class CoCoSSMSettings extends React.Component<Props, State> {
  private readonly emptySettings: Settings;

  constructor(props: Props) {
    super(props);

    this.state = {
      messages: [],
    };

    this.emptySettings = {
      enabled: false,
      environmentType: Object.values(this.props.availableEnvironmentTypes)[0],
      attestOnBoot: false,
      attestOnSchedule: false,
    };
  }

  onSave = (data: Settings) => {
    const request = {
      serverIds: this.props.systemSupport.filter((system) => system.cocoSupport).map((system) => system.id),
      ...data,
    };

    Network.post(`/rhn/manager/api/systems/coco/settings`, request).then(
      (response) =>
        this.setState({
          messages: response.success
            ? MessagesUtils.success(response.messages)
            : MessagesUtils.error(response.messages),
        }),
      (err) => this.setState({ messages: Network.responseErrorMessage(err) })
    );
  };

  render(): React.ReactNode {
    return (
      <>
        <TopPanel title="Confidential Computing Settings">
          <Messages items={this.state.messages} />
          <CoCoSettingsForm
            initialData={this.emptySettings}
            saveHandler={this.onSave}
            availableEnvironmentTypes={this.props.availableEnvironmentTypes}
            showOnScheduleOption={false}
          />
        </TopPanel>
        <TargetSystems systemsData={this.props.systemSupport}>
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey="cocoSupport"
            header={t("Confidential Computing Capability")}
            cell={(system: SystemData) => (system.cocoSupport ? t("Yes") : t("No"))}
          />
        </TargetSystems>
      </>
    );
  }
}

export default CoCoSSMSettings;
