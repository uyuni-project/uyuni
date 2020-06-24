// @flow

import * as React from 'react';
import * as Network from 'utils/network';
import { Utils as MessagesUtils } from 'components/messages';

import type {MessageType} from 'components/messages';

type Props = {
  /** URL path part with @ACTION@ as action placeholder. */
  urlTemplate: string,
  /**
   * Function rendering the children taking an object parameter with
   * an onAction and a messages property
   */
  children: ({onAction: (urlModifier: (string) => string, action: string, parameters: Object) => void, messages: Array<MessageType>}) => React.Node,
  /**
   * URL of the page to show after the action has been successfully performed.
   * If undefined, the page is not redirected at all.
   */
  bounce?: string,
  /** Function with the response as parameter in case of success of the HTTP request */
  callback?: Function,
};

/** Helper component offering an API to run a POST HTTP resquest */
export function ActionApi(props: Props) {
  const [messages, setMessages] = React.useState<Array<MessageType>>([]);

  const onAction = (urlModifier: (string) => string, action: string, parameters: Object) => {
    Network.post(urlModifier(props.urlTemplate),
      JSON.stringify(parameters), 'application/json').promise
      .then((response) => {
        if (Object.values(response).includes('Failed')) {
          setMessages(MessagesUtils.error(t(`Failed to trigger ${action}`)));
        } else {
          if (props.callback) {
            props.callback(response);
          }
          if (props.bounce) {
            window.location.replace(props.bounce);
          }
        }
      }, (xhr) => {
        const errMessages = xhr.status === 0
          ? MessagesUtils.error(
            t('Request interrupted or invalid response received from the server. Please try again.'),
          )
          : MessagesUtils.error(Network.errorMessageByStatus(xhr.status));
        setMessages(errMessages);
      });
  };

  return props.children({
    onAction: onAction,
    messages,
  });
}

ActionApi.defaultProps = {
  bounce: undefined,
  callback: undefined,
};
