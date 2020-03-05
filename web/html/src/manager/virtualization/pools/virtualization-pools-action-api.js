// @flow
import * as React from 'react';
import { ActionApi } from '../ActionApi';

type Props = {
  hostid: string,
  children: Function,
  bounce?: string,
  callback?: Function,
};

export function VirtualizationPoolsActionApi(props: Props) {
  return (
    <ActionApi
      urlTemplate={ `/rhn/manager/api/systems/details/virtualization/pools/${props.hostid}/@ACTION@`}
      bounce={props.bounce}
      callback={props.callback}
    >
    {
      ({
        onAction: apiAction,
        messages,
      }) => {
        const onAction = (action: string, poolNames: Array<string>, parameters: Object) => {
          const messageData = Object.assign({ }, parameters, { poolNames });
          apiAction(action, messageData);
        }
        return props.children({onAction, messages});
      }
    }
    </ActionApi>
  );
}
VirtualizationPoolsActionApi.defaultProps = {
  bounce: undefined,
  callback: undefined,
};
