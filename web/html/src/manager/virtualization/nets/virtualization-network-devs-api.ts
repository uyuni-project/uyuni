import * as React from "react";

import * as Messages from "components/messages/messages";

import Network from "utils/network";

type Props = {
  hostId: string;
  children: Function;
};

export function VirtualizationNetworkDevsApi(props: Props) {
  // const [netDevices, setNetDevices] = React.useState(null);
  // const [messages, setMessages] = React.useState<Messages.MessageType[]>([]);

  // React.useEffect(() => {
  //   let subscribded = true;
  //   Network.get(`/rhn/manager/api/systems/details/virtualization/nets/${props.hostId}/devices`).then(
  //     (response) => {
  //       if (subscribded) {
  //         setNetDevices(response);
  //       }
  //     },
  //     (xhr) => {
  //       const errMessages =
  //         xhr.status === 0
  //           ? Messages.Utils.error(t("Could not get network devices from the server. Please try again."))
  //           : Messages.Utils.error(Network.errorMessageByStatus(xhr.status));
  //       setMessages(errMessages);
  //     }
  //   );
  //   return () => {
  //     subscribded = false;
  //   };
  // }, []);

  const netDevices = [
    { name: "eth4", address: "42:8a:c6:98:8d:00", state: "down", VF: true, "PCI address": "0000:3d:02.6", PF: false },
    { name: "eth6", address: "76:fe:ce:4f:73:22", state: "down", VF: true, "PCI address": "0000:3d:02.4", PF: false },
    { name: "eth5", address: "e2:58:67:95:a3:a3", state: "down", VF: true, "PCI address": "0000:3d:02.5", PF: false },
    { name: "eth0", address: "a4:bf:01:1d:27:88", state: "up", VF: false, PF: true },
    { name: "virbr0-nic", address: "52:54:00:ae:52:36", state: "down" },
    { name: "eth8", address: "e6:86:48:46:c5:29", state: "down", VF: true, "PCI address": "0000:3d:02.2", PF: false },
    { name: "eth1", address: "a4:bf:01:1d:27:89", state: "down", VF: false, PF: false },
    { name: "eth7", address: "2e:ee:7b:ff:92:6a", state: "down", VF: true, "PCI address": "0000:3d:02.3", PF: false },
    { name: "eth2", address: "72:98:05:69:58:98", state: "down", VF: true, "PCI address": "0000:3d:02.1", PF: false },
  ];
  const messages = [];

  return props.children({
    netDevices,
    messages,
  });
}
