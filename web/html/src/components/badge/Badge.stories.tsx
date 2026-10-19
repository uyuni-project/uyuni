import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { BootstrapPanel } from "components/panels/BootstrapPanel";

import { Badge } from "./Badge";

const statusColorOptions = ["default", "success", "warning", "error", "info", "running"];

const specialColorOptions = ["gray", "green", "yellow", "blue", "red"];

const iconOptions = [
  "fa-check",
  "fa-exclamation-triangle",
  "fa-ban",
  "fa-info-circle",
  "fa-spinner fa-spin",
  "fa-clock-o",
  "fa-star",
];

const meta = {
  title: "Components/Badge/Badge",
  component: Badge,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Short, non-interactive label used to communicate status, category, or metadata. Two variants are available: `status` for system and application states, and `special` for attention-grabbing labels. Each variant has its own set of colors, and the first letter of `text` is capitalized automatically.",
      },
    },
  },
  args: {
    text: "Default",
    variant: "status",
    color: "default",
    icon: undefined,
    small: false,
  },
  argTypes: {
    text: { control: "text" },
    variant: {
      control: "inline-radio",
      options: ["status", "special"],
    },
    color: {
      control: "select",
      options: [...statusColorOptions, ...specialColorOptions],
      description:
        "Pair status colors with the `status` variant and special colors with the `special` variant. Mismatched combinations produce no styling.",
    },
    icon: {
      control: "select",
      options: iconOptions,
    },
    small: {
      control: "boolean",
      description:
        "Adds the `badge--sm` modifier. No styling is currently attached to that class, so this has no visual effect yet.",
    },
  },
} satisfies Meta<typeof Badge>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Use the controls to try out `text`, `variant`, `color`, and `icon`. Remember that each variant accepts only its own colors.",
      },
    },
  },
};

export const StatusVariant: Story = {
  name: "Status variant",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Use the `status` variant for informational or system states: application state, status indicators, background process states, and read-only metadata. Typical labels are Active, Inactive, Success, Error, Warning, Running, Failed, and Pending.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Badge text="Default" />
        <Badge text="Success" icon="fa-check" color="success" />
        <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
        <Badge text="Error" icon="fa-ban" color="error" />
        <Badge text="Info" icon="fa-info-circle" color="info" />
        <Badge text="Running" icon="fa-spinner fa-spin" color="running" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const SpecialVariant: Story = {
  name: "Special variant",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Use the `special` variant for attention-grabbing labels: important or promotional labels, feature flags, standout UI elements, and non-system descriptive tags. Typical labels are New, Premium, Featured, and Beta.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Badge text="Default" variant="special" />
        <Badge text="New" variant="special" color="green" />
        <Badge text="Removed" variant="special" color="red" />
        <Badge text="Beta" variant="special" color="yellow" />
        <Badge text="Internal" variant="special" color="blue" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const WithoutIcons: Story = {
  name: "Without icons",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "The `icon` property is optional. Omit it when the label alone carries the meaning.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Badge text="Default" />
        <Badge text="Success" color="success" />
        <Badge text="Warning" color="warning" />
        <Badge text="Error" color="error" />
        <Badge text="Info" color="info" />
        <Badge text="Running" color="running" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const InPanel: Story = {
  name: "In a panel",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Badges in context, inside a panel alongside other content.",
      },
    },
  },
  render: () => (
    <BootstrapPanel title="Panel">
      <Badge text="Default" />
      <Badge text="Success" icon="fa-check" color="success" />
      <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
      <Badge text="Error" icon="fa-ban" color="error" />
      <Badge text="Info" icon="fa-info-circle" color="info" />
      <Badge text="Running" icon="fa-spinner fa-spin" color="running" />
      <h4 className="mt-5">Special</h4>
      <Badge text="Default" variant="special" />
      <Badge text="New" variant="special" color="green" />
      <Badge text="Removed" variant="special" color="red" />
      <Badge text="Beta" variant="special" color="yellow" />
      <Badge text="Internal" variant="special" color="blue" />
    </BootstrapPanel>
  ),
};
