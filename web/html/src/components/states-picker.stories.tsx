import { useEffect, useMemo } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import Network from "utils/network";

import { StatesPicker } from "./states-picker";

type StatesPickerProps = React.ComponentProps<typeof StatesPicker>;

const channels = [
  {
    id: 1,
    label: "security-baseline",
    name: "Security baseline",
    description: "Applies the common security configuration.",
    type: "state",
    assigned: true,
    position: 1,
  },
  {
    id: 2,
    label: "application-config",
    name: "Application configuration",
    description: "Configures the example application.",
    type: "state",
    assigned: true,
    position: 2,
  },
  {
    id: 3,
    label: "monitoring-agent",
    name: "Monitoring agent",
    description: "Installs and configures system monitoring.",
    type: "state",
    assigned: false,
  },
  {
    id: 4,
    label: "reboot-if-needed",
    name: "Reboot system if needed",
    description: "Reboots after applying changes when required.",
    type: "internal_state",
    assigned: false,
  },
];

const originalNetworkGet = Network.get;

const StatesPickerStory = (props: StatesPickerProps) => {
  const mockNetworkGet = useMemo(
    () =>
      ((url: string) =>
        Promise.resolve(
          url.includes("/content")
            ? "install_monitoring_agent:\n  pkg.installed:\n    - name: monitoring-agent"
            : channels.map((channel) => ({ ...channel }))
        )) as typeof Network.get,
    []
  );

  Network.get = mockNetworkGet;

  useEffect(
    () => () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
    },
    [mockNetworkGet]
  );

  return (
    <div
      className="states-picker-story"
      style={{ width: "100%", minHeight: "700px", padding: "16px", boxSizing: "border-box" }}
    >
      <style>{`.states-picker-story .spacewalk-section-toolbar { top: 0 !important; }`}</style>
      <StatesPicker {...props} />
    </div>
  );
};

const meta = {
  title: "Components/Data Display/StatesPicker",
  component: StatesPicker,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Searches, assigns, and ranks Salt states or configuration channels. This story uses a small in-memory dataset and does not contact a backend.",
      },
    },
  },
  args: {
    type: "state",
    matchUrl: (filter = "") => `/storybook/states?filter=${encodeURIComponent(filter)}`,
    saveRequest: async (selectedChannels) => {
      action("states saved")(selectedChannels);
      return selectedChannels;
    },
    messages: action("messages changed"),
  },
  argTypes: {
    type: {
      control: "select",
      options: ["state", "channel"],
      description: "Changes labels and state-specific validation behavior.",
    },
    matchUrl: {
      control: false,
      description: "Builds the search endpoint. The story intercepts requests and returns local sample data.",
    },
    saveRequest: {
      control: false,
      description: "Persists the current assignment and ranking.",
    },
    applyRequest: {
      control: false,
      description: "Optionally executes assigned states against selected systems.",
    },
    messages: {
      action: "messages changed",
      description: "Receives validation and save messages instead of rendering them internally.",
    },
  },
  render: (args) => <StatesPickerStory {...args} />,
} satisfies Meta<typeof StatesPicker>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
