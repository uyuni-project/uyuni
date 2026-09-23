import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { ServerDetailsForm } from "./ServerDetailsForm";
import { peripheralDetails } from "./story-fixtures";

const mockNetworkPost = ((url: string) => {
  const data = url.endsWith("/credentials") ? "mirror-peripheral-regenerated" : undefined;
  return Utils.cancelable(Promise.resolve({ data }));
}) as typeof Network.post;

const mockNetworkDelete = (() => Utils.cancelable(Promise.resolve())) as typeof Network.del;

const meta = {
  title: "Compositions/Hub/ServerDetailsForm",
  component: ServerDetailsForm,
  beforeEach: () => {
    const originalNetworkPost = Network.post;
    const originalNetworkDelete = Network.del;
    Network.post = mockNetworkPost;
    Network.del = mockNetworkDelete;

    return () => {
      if (Network.post === mockNetworkPost) {
        Network.post = originalNetworkPost;
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
          "Summarizes a registered Hub server. The peripheral fixture exposes editable CA data, mirror-credential regeneration, and synchronized-channel information; write requests stay local to Storybook.",
      },
    },
  },
  args: {
    model: peripheralDetails,
    editable: true,
  },
  argTypes: {
    model: {
      control: false,
      description: "Registered hub or peripheral details shown by the form.",
    },
    editable: {
      control: "boolean",
      description: "Enables editing the Root CA and synchronized channels.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "620px", padding: "24px" }}>
      <ServerDetailsForm key={`${args.model.role}-${args.model.id}-${args.editable}`} {...args} />
    </div>
  ),
} satisfies Meta<typeof ServerDetailsForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
