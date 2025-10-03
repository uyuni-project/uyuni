import * as React from "react";
import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { DateTime, DEPRECATED_Select, Form, Text } from "components/input";
import { ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels";
import Validation from "components/validation";

import { localizedMoment } from "utils";
import Network from "utils/network";

export type UploadRegion = {
  label: string;
  description: string;
};

export type UploadRegionArray = [UploadRegion, ...UploadRegion[]];

class SupportDataRequest {
  public caseNumber: string;
  public region: string;
  public parameters: string;
  public earliest: moment.Moment;

  public constructor(caseNumber: string, region: string, parameters: string, earliest: moment.Moment) {
    this.caseNumber = caseNumber;
    this.region = region;
    this.parameters = parameters;
    this.earliest = earliest;
  }
}

type Props = {
  serverId: number;
  availableRegions: UploadRegionArray;
  supportProgramName: string | null;
};

export const SupportData: React.FC<Props> = ({ serverId, availableRegions, supportProgramName }): JSX.Element => {
  const [formModel, setFormModel] = useState(
    () => new SupportDataRequest("", availableRegions[0].label, "", localizedMoment())
  );
  const [loading, setLoading] = useState(false);
  const [validated, setValidated] = useState(false);
  const [messages, setMessages] = useState(
    // If the support program is null it means it was not possible to identify how to extract the support data.
    // The page will only show a static error message in this case.
    supportProgramName === null
      ? MessagesUtils.error(t("The OS of this system does not support the collection of support data."))
      : []
  );

  function getFormattedProgramName(programName: string): React.ReactNode {
    return <code>{programName}</code>;
  }

  function getActionLink(text: string, actionId: number): React.ReactNode {
    return <ActionLink id={actionId}>{text}</ActionLink>;
  }

  function onSubmit(): void {
    setLoading(true);

    Network.post(`/rhn/manager/api/systems/${serverId}/details/uploadSupportData`, formModel)
      .then(
        (response) => {
          let messages: MessageType[];
          if (!response.success) {
            messages = MessagesUtils.error(response.messages);
          } else {
            messages = MessagesUtils.info(
              <span>
                {t("The action has been <link>scheduled</link>.", {
                  link: (text: string) => getActionLink(text, response.data as number),
                })}
              </span>
            );
          }

          setMessages(messages);
        },
        (err) => setMessages(Network.responseErrorMessage(err))
      )
      .finally(() => setLoading(false));
  }

  return (
    <TopPanel title={t("Upload Support Data")} icon="fa fa-life-ring">
      <Messages items={messages} />
      {supportProgramName !== null && (
        <Form
          model={formModel}
          divClass="col-md-12"
          onValidate={(valid: boolean) => setValidated(valid)}
          onChange={(updated: Partial<SupportDataRequest>) => setFormModel((prev) => ({ ...prev, ...updated }))}
          onSubmit={() => onSubmit()}
        >
          <Text
            name="caseNumber"
            label={t("Support Case Number")}
            hint={t("The support case number to which this data will be attached.")}
            required
            placeholder={t("e.g. 12345")}
            labelClass="col-md-3"
            divClass="col-md-6"
            validators={Validation.isInt({ gt: 0 })}
            invalidHint={t("Has to be a valid support case number")}
          />
          <DEPRECATED_Select
            name="region"
            label={t("Upload Region")}
            hint={t("The location of the FTP server where the data is uploaded.")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
            options={availableRegions}
            getOptionLabel={(item: UploadRegion) => item.description}
            getOptionValue={(item: UploadRegion) => item.label}
          />
          <Text
            name="parameters"
            label={t("Command-line Arguments")}
            hint={t("Optional command line arguments for the execution of <programName></programName>.", {
              programName: () => getFormattedProgramName(supportProgramName),
            })}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <DateTime
            name="earliest"
            label={t("Earliest")}
            hint={t("The earliest moment the action can be executed.")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <SubmitButton
            id="submit-btn"
            className="btn-primary offset-md-3"
            text={t("Schedule")}
            disabled={!validated || loading}
          />
        </Form>
      )}
    </TopPanel>
  );
};
