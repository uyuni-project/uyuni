import * as React from "react";

import * as Messages from "components/messages";
import { MessageType } from "components/messages";

import Network from "utils/network";

type Props = {
  /** Virtual host server ID */
  hostid: string;

  /** Name of the network for which to get the defintion*/
  networkName: string;

  /** Children function rendering the content depending on the request result */
  children: (arg0: { definition: any; messages: MessageType[] }) => React.ReactNode;
};

/** Component calling the Uyuni REST API to get the XML definition of a virtual storage pool */
export function VirtualizationNetworkDefinitionApi(props: Props) {
  const [messages, setMessages] = React.useState<MessageType[]>([]);
  const [definition, setDefinition] = React.useState(null);

  React.useEffect(() => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/nets/${props.hostid}/net/${props.networkName}`).then(
      (response) => {
        setDefinition(response);
      },
      (xhr) => {
        const errMessages =
          xhr.status === 0
            ? Messages.Utils.error(
                t("Request interrupted or invalid response received from the server. Please try again.")
              )
            : Messages.Utils.error(Network.errorMessageByStatus(xhr.status));
        setMessages(errMessages);
      }
    );
  }, [props.hostid, props.networkName]);

  return (
    <>
      {props.children({
        definition: definition,
        messages: messages,
      })}
    </>
  );
}
