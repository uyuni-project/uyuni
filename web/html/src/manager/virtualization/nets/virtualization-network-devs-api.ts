import * as React from "react";

import * as Messages from "components/messages/messages";

import Network from "utils/network";

type Props = {
  hostId: string;
  children: Function;
};

export function VirtualizationNetworkDevsApi(props: Props) {
  const [netDevices, setNetDevices] = React.useState(null);
  const [messages, setMessages] = React.useState<Messages.MessageType[]>([]);

  React.useEffect(() => {
    let subscribded = true;
    Network.get(`/rhn/manager/api/systems/details/virtualization/nets/${props.hostId}/devices`).then(
      (response) => {
        if (subscribded) {
          setNetDevices(response);
        }
      },
      (xhr) => {
        const errMessages =
          xhr.status === 0
            ? Messages.Utils.error(t("Could not get network devices from the server. Please try again."))
            : Messages.Utils.error(Network.errorMessageByStatus(xhr.status));
        setMessages(errMessages);
      }
    );
    return () => {
      subscribded = false;
    };
  }, []);

  return props.children({
    netDevices,
    messages,
  });
}
