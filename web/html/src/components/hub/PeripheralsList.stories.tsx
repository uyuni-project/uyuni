import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { PeripheralsList } from "./PeripheralsList";
import { peripherals } from "./story-fixtures";

const mockNetworkGet = ((url: string) => {
  const criteria = new URL(url, window.location.origin).searchParams.get("q")?.toLocaleLowerCase() ?? "";
  const items = peripherals
    .filter((peripheral) => peripheral.fqdn.toLocaleLowerCase().includes(criteria))
    .map((peripheral) => ({ ...peripheral }));

  return Utils.cancelable(Promise.resolve({ items, total: items.length }));
}) as typeof Network.get;

const mockNetworkDelete = (() => Utils.cancelable(Promise.resolve())) as typeof Network.del;

const meta = {
  title: "Compositions/Hub/PeripheralsList",
  component: PeripheralsList,
  beforeEach: () => {
    const originalNetworkGet = Network.get;
    const originalNetworkDelete = Network.del;
    Network.get = mockNetworkGet;
    Network.del = mockNetworkDelete;

    return () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
      if (Network.del === mockNetworkDelete) {
        Network.del = originalNetworkDelete;
      }
    };
  },
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Lists registered peripheral servers with synchronized-content counts, Root CA downloads, detail links, filtering, and deregistration. Requests are served from local fixtures.",
      },
    },
  },
  render: () => (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "700px", padding: "24px" }}>
      <PeripheralsList />
    </div>
  ),
} satisfies Meta<typeof PeripheralsList>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
