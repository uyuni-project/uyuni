import * as React from "react";
import { ActionApi } from "./ActionApi";

type Props = {
  urlType: string;
  idName: string;
  hostid: string;
  children: Function;
  bounce?: string;
  callback?: Function;
};

export function SimpleActionApi(props: Props) {
  return (
    <ActionApi
      urlTemplate={`/rhn/manager/api/systems/details/virtualization/${props.urlType}/${props.hostid}/`}
      bounce={props.bounce}
      callback={props.callback}
    >
      {({ onAction: apiAction, messages }) => {
        const onAction = (action: string, ids: Array<string>, parameters: any) => {
          const messageData = Object.assign({}, parameters, { [props.idName]: ids });
          apiAction((urlTemplate) => `${urlTemplate}${action}`, action, messageData);
        };
        return props.children({ onAction, messages });
      }}
    </ActionApi>
  );
}
SimpleActionApi.defaultProps = {
  bounce: undefined,
  callback: undefined,
};
