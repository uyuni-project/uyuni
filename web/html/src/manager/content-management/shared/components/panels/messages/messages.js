// @flow
import React from 'react';
import {Panel} from "../../../../../../components/panels/Panel";
import _isEmpty from "lodash/isEmpty";

import type {ProjectMessageType} from '../../../type/project.type.js';

type MessagesProps = {
  messages: Array<ProjectMessageType>,
};

const msgClassMap = {
  'warning': {
    text: 'text-warning',
    panel: 'panel-warning',
    icon: 'fa-exclamation-circle'
  },
  'error': {
    text: 'text-danger',
    panel: 'panel-danger',
    icon: 'fa-exclamation-circle'
  },
  'info': {
    text: '',
    panel: '',
    icon: 'fa-info-circle'
  }
}

const sortMessages = (messages) => {
  const msgPrio = {'error': 0, 'warning': 1, 'info': 2};
  return messages.slice().sort((a, b) => msgPrio[a.type] - msgPrio[b.type]);
}

const renderMessageEntry = (message, index) => {
  return (
    <li key={index} className="list-group-item">
      <div className={msgClassMap[message.type].text}>
        <i className={`fa ` + msgClassMap[message.type].icon}/>
        {message.text}
      </div>
    </li>
  );
}

const Messages = (props: MessagesProps) => {
  if (_isEmpty(props.messages))
    return null;

  const messages = sortMessages(props.messages);
  return (
    <Panel
      collapseId="messages-panel"
      headingLevel="h2"
      customIconClass="fa-small"
      className={msgClassMap[messages[0].type].panel}
      title={t("Issues")}>

      <ul className="list-group">
        {messages.map((m, i) => renderMessageEntry(m, i))}
      </ul>

    </Panel>
  );
}

export default Messages;
