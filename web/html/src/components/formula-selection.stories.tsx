import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { FormulaSelection } from "./formula-selection";

type FormulaSelectionProps = React.ComponentProps<typeof FormulaSelection>;

const formulaData = {
  formulas: [
    {
      name: "timezone",
      group: "system",
      description: "Configures the system time zone and hardware clock.",
    },
    {
      name: "users",
      group: "system",
      description: "Manages local users and groups.",
    },
    {
      name: "openssh",
      group: "security",
      description: "Configures the OpenSSH server.",
    },
    {
      name: "auditd",
      group: "security",
      description: "Configures Linux audit rules.",
    },
    {
      name: "motd",
      description: "Provides a login message for managed systems.",
    },
  ],
  selected: ["timezone", "openssh", "motd"],
  active: ["timezone", "openssh", "motd"],
};

const mockNetworkGet = (() =>
  Utils.cancelable(
    Promise.resolve({
      formulas: formulaData.formulas.map((formula) => ({ ...formula })),
      selected: [...formulaData.selected],
      active: [...formulaData.active],
    })
  )) as typeof Network.get;

const FormulaSelectionStory = (props: FormulaSelectionProps) => {
  return (
    <div className="formula-selection-story" style={{ minHeight: "650px" }}>
      <style>{`.formula-selection-story .spacewalk-section-toolbar { top: 0 !important; }`}</style>
      <FormulaSelection {...props} />
    </div>
  );
};

const meta = {
  title: "Compositions/Configuration/FormulaSelection",
  component: FormulaSelection,
  beforeEach: () => {
    const originalNetworkGet = Network.get;
    Network.get = mockNetworkGet;

    return () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
    };
  },
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Assigns grouped and ungrouped Salt formulas to a system or system group. This story uses local data and does not contact a backend.",
      },
    },
  },
  args: {
    dataUrl: "/storybook/formulas",
    warningMessage: "",
    addFormulaNavBar: () => undefined,
    saveRequest: async (_component, selectedFormulas) => {
      action("formulas saved")(selectedFormulas);
    },
  },
  argTypes: {
    dataUrl: {
      control: false,
      description: "Formula endpoint; intercepted by the story and backed by local fixture data.",
    },
    warningMessage: {
      control: "text",
      description: "Optional warning displayed through the standard notification container.",
    },
    addFormulaNavBar: {
      control: false,
      description: "Updates the surrounding page navigation with the active formulas.",
    },
    saveRequest: {
      control: false,
      description: "Persists the selected formula names.",
    },
  },
  render: (args) => <FormulaSelectionStory {...args} />,
} satisfies Meta<typeof FormulaSelection>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
