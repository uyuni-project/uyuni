import * as React from "react";

import * as Messages from "components/messages/messages";

import Network from "utils/network";

type Props = {
  hostId: string;
  children: Function;
};

export function VirtualizationPoolCapsApi(props: Props) {
  const [capabilities, setCapabilities] = React.useState(null);
  const [messages, setMessages] = React.useState<any[]>([]);

  React.useEffect(() => {
    let subscribded = true;
    Network.get(`/rhn/manager/api/systems/details/virtualization/pools/${props.hostId}/capabilities`).then(
      (response) => {
        if (subscribded) {
          setCapabilities(response);
        }
      },
      (xhr) => {
        const errMessages =
          xhr.status === 0
            ? [Messages.Utils.error(t("Could not get storage pool capabilities from the server. Please try again."))]
            : [Messages.Utils.error(Network.errorMessageByStatus(xhr.status))];
        setMessages(errMessages);
      }
    );
    return () => {
      subscribded = false;
    };
  }, []);

  return props.children({
    capabilities,
    messages,
  });
}
