import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { DeregisterServer } from "./DeregisterServer";
import { IssRole } from "./types";

const mockNetworkDelete = (() => Utils.cancelable(Promise.resolve())) as typeof Network.del;

const meta = {
  title: "Compositions/Hub/DeregisterServer",
  component: DeregisterServer,
  beforeEach: () => {
    const originalNetworkDelete = Network.del;
    Network.del = mockNetworkDelete;

    return () => {
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
          "Confirms deregistration of a Hub or peripheral server before issuing the request. The Storybook request succeeds locally and reports completion in the Actions panel.",
      },
    },
  },
  args: {
    role: IssRole.Peripheral,
    id: 42,
    fqdn: "peripheral.example.com",
    onDeregistered: action("server deregistered"),
  },
  argTypes: {
    role: {
      control: "select",
      options: [IssRole.Hub, IssRole.Peripheral],
      description: "Role used to select the endpoint and confirmation copy.",
    },
    id: {
      control: "number",
      description: "Server identifier used by the deregistration endpoint.",
    },
    fqdn: {
      control: "text",
      description: "Server name included in the confirmation dialog.",
    },
    onDeregistered: {
      control: false,
      description: "Called after the local deregistration request succeeds.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "900px", minHeight: "520px", padding: "24px" }}>
      <DeregisterServer key={`${args.role}-${args.id}-${args.fqdn}`} {...args} />
    </div>
  ),
} satisfies Meta<typeof DeregisterServer>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
