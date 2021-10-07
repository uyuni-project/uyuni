import * as React from "react";
import _isEmpty from "lodash/isEmpty";

import { Messages } from "components/messages";
import { ProjectMessageType } from "../../type";

type ValidationMessagesType = {
  panelClass: string;
  messages: React.ReactNode | null | undefined;
};

const msgClassMap = {
  warning: {
    text: "text-warning",
    panel: "panel-warning",
  },
  error: {
    text: "text-danger",
    panel: "panel-danger",
  },
  info: {
    text: "text-default",
    panel: "panel-default",
  },
};

const sortMessages = (messages: Array<ProjectMessageType>) => {
  const msgPriorities = { error: 0, warning: 1, info: 2 };
  return messages.slice().sort((a, b) => msgPriorities[a.type] - msgPriorities[b.type]);
};

const getRenderedMessages = (messages: Array<ProjectMessageType>): ValidationMessagesType => {
  if (_isEmpty(messages)) {
    return {
      panelClass: "panel-default",
      messages: null,
    };
  }

  const sortedMsgs = sortMessages(messages);
  return {
    panelClass: msgClassMap[sortedMsgs[0].type].panel,
    messages: <Messages items={sortedMsgs.map((m) => ({ severity: m.type, text: m.text }))} />,
  };
};

export default getRenderedMessages;
