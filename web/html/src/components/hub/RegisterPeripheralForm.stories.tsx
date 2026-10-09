import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Utils } from "utils/functions";
import Network from "utils/network";
import { mockNetwork } from "utils/storybook/mock-network";

import { RegisterPeripheralForm } from "./RegisterPeripheralForm";

const mockNetworkPost = (() => Utils.cancelable(Promise.resolve({ data: 42 }))) as typeof Network.post;

const meta = {
  title: "Compositions/Hub/RegisterPeripheralForm",
  component: RegisterPeripheralForm,
  beforeEach: () => mockNetwork({ post: mockNetworkPost }),
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Registers a peripheral server by existing token or administrator credentials, with an optional Root CA. Submission is handled by a local Storybook response.",
      },
    },
  },
  render: () => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "900px", padding: "24px" }}>
      <RegisterPeripheralForm />
    </div>
  ),
} satisfies Meta<typeof RegisterPeripheralForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
