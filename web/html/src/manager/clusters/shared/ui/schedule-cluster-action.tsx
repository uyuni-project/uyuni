import * as React from "react";
import { useState } from "react";
import { Panel } from "components/panels/Panel";
import { AsyncButton, Button } from "components/buttons";
import { ActionSchedule } from "components/action-schedule";
import { withErrorMessages } from "../api/use-clusters-api";

import { ActionChain } from "components/action-schedule";
import { ErrorMessagesType } from "../api/use-clusters-api";
import { MessageType } from "components/messages";
import { localizedMoment } from "utils";

type Props = {
  title: string;
  panel: React.ReactNode;
  schedule: (earliest: moment.Moment, actionChain: string | null) => Promise<any>;
  onPrev?: () => void;
  setMessages: (arg0: Array<MessageType>) => void;
  scheduleButtonLabel: string;
  actionType?: string;
};

const ScheduleClusterAction = (props: Props) => {

  const [actionChain, setActionChain] = useState<ActionChain | null>(null);
  const [earliest, setEarliest] = useState(localizedMoment());
  const [disableSchedule, setDisableSchedule] = useState(false);

  const onSchedule = (): Promise<any> => {
    return props.schedule(earliest, actionChain ? actionChain.text : null).then(
      actionId => {
        setDisableSchedule(true);
      },
      (error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      }
    );
  };

  return (
    <Panel
      headingLevel="h4"
      title={props.title}
      footer={
        <div className="btn-group">
          {props.onPrev ? (
            <Button
              id="btn-prev"
              disabled={disableSchedule}
              text={t("Back")}
              className="btn-default"
              icon="fa-arrow-left"
              handler={props.onPrev}
            />
          ) : null}
          <AsyncButton
            id="btn-next"
            disabled={disableSchedule}
            text={props.scheduleButtonLabel}
            defaultType="btn-success"
            action={onSchedule}
          />
        </div>
      }
    >
      {props.panel}

      <ActionSchedule
        earliest={earliest}
        actionType={props.actionType}
        onDateTimeChanged={date => {
          setEarliest(date);
          setActionChain(null);
        }}
      />
    </Panel>
  );
};

export default withErrorMessages(ScheduleClusterAction);
