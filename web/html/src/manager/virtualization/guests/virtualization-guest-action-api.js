// @flow
import * as React from 'react';
import { ActionApi } from '../ActionApi';

type Props = {
  hostid: string,
  children: Function,
  bounce?: string,
  callback?: Function,
};

export function VirtualizationGuestActionApi(props: Props) {
  return (
    <ActionApi
      urlTemplate={ `/rhn/manager/api/systems/details/virtualization/guests/${props.hostid}/`}
      bounce={props.bounce}
      callback={props.callback}
    >
    {
      ({
        onAction: apiAction,
        messages,
      }) => {
        const onAction = (action: string, uuids: Array<string>, parameters: Object) => {
          const messageData = Object.assign({ }, parameters, { uuids });
          apiAction((urlTemplate) => `${urlTemplate}${action}`, action, messageData);
        }
        return props.children({onAction, messages});
      }
    }
    </ActionApi>
  );
}
VirtualizationGuestActionApi.defaultProps = {
  bounce: undefined,
  callback: undefined,
};
