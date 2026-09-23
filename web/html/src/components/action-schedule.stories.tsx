import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { localizedMoment } from "utils";

import { ActionSchedule } from "./action-schedule";

const meta = {
  title: "Components/Inputs/ActionSchedule",
  component: ActionSchedule,
  parameters: {
    docs: {
      description: {
        component:
          "Shared scheduler for choosing the earliest execution time or adding an operation to an existing or new action chain.",
      },
    },
  },
  args: {
    earliest: localizedMoment("2026-09-23T14:30:00Z"),
    actionChains: [
      { id: 11, text: "Monthly maintenance" },
      { id: 12, text: "Production rollout" },
    ],
    onDateTimeChanged: action("date and time changed"),
    onActionChainChanged: action("action chain changed"),
  },
  argTypes: {
    earliest: {
      control: false,
      description: "Initial earliest execution time as a localized Moment value.",
    },
    actionChains: {
      control: false,
      description: "Available action chains. Omit this prop when action-chain scheduling is unavailable.",
    },
    onDateTimeChanged: {
      action: "date and time changed",
      description: "Called whenever date-based scheduling is selected or changed.",
    },
    onActionChainChanged: {
      action: "action chain changed",
      description: "Called with the chosen action chain, or null when date scheduling is selected.",
    },
    systemIds: {
      control: false,
      description: "System IDs used to load maintenance windows when supplied with actionType.",
    },
    actionType: {
      control: "text",
      description: "Action type used when requesting maintenance windows.",
    },
  },
} satisfies Meta<typeof ActionSchedule>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
