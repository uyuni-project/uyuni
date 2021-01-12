import * as React from "react";
import Network from "utils/network";
import { Utils as MessageUtils, MessageType } from "components/messages";

type Props = {
  hostId: string;
  children: Function;
};

export function VirtualizationPoolCapsApi(props: Props) {
  const [capabilities, setCapabilities] = React.useState<unknown | null>(null);
  const [messages, setMessages] = React.useState<MessageType[][]>([]);

  React.useEffect(() => {
    let subscribded = true;
    Network.get(
      `/rhn/manager/api/systems/details/virtualization/pools/${props.hostId}/capabilities`,
      "application/json"
    ).promise.then(
      response => {
        if (subscribded) {
          setCapabilities(response);
        }
      },
      xhr => {
        const errMessages =
          xhr.status === 0
            ? [MessageUtils.error(t("Could not get storage pool capabilities from the server. Please try again."))]
            : [MessageUtils.error(Network.errorMessageByStatus(xhr.status))];
        setMessages(errMessages);
      }
    );
    return () => {
      subscribded = false;
    };
  }, [props.hostId]);

  return (
    <>
      {props.children({
        capabilities,
        messages,
      })}
    </>
  );
}
