import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { IconTag } from "./icontag";

const iconOptions = [
  "action-failed",
  "action-ok",
  "action-pending",
  "action-running",
  "errata-bugfix",
  "errata-security",
  "external-link",
  "file-directory",
  "file-file",
  "header-calendar",
  "header-system",
  "item-add",
  "item-del",
  "spinner",
  "system-crit",
  "system-ok",
  "system-unknown",
  "system-warn",
  "experimental",
];

const meta = {
  title: "Components/Display/IconTag",
  component: IconTag,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Maps Uyuni semantic icon names to their Font Awesome or Spacewalk icon classes. Prefer a semantic type over duplicating icon class strings at call sites.",
      },
    },
  },
  args: {
    type: "system-ok",
    className: "",
    title: "System is healthy",
    tooltipPlacement: "top",
  },
  argTypes: {
    type: {
      control: "select",
      options: iconOptions,
      description: "Semantic Uyuni icon identifier.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes appended to the mapped icon classes.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Optional accessible title and tooltip text.",
      table: { type: { summary: "string" } },
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred Bootstrap tooltip placement when a title is present.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
  },
} satisfies Meta<typeof IconTag>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const CommonStatuses: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <IconTag type="action-pending" title="Pending" />
        <IconTag type="action-running" title="Running" />
        <IconTag type="action-ok" title="Completed" />
        <IconTag type="action-failed" title="Failed" />
        <IconTag type="system-warn" title="Warning" />
        <IconTag type="system-unknown" title="Unknown" />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Frequently used action and system status icons." } },
  },
};
