import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { SectionToolbar } from "./section-toolbar";

const actions = (
  <>
    <div className="selector-button-wrapper">
      <div className="btn-group">
        <Button className="btn-default" icon="fa-check-square-o" text="Select all" />
        <Button className="btn-default" icon="fa-square-o" text="Clear" />
      </div>
    </div>
    <div className="action-button-wrapper">
      <div className="btn-group">
        <Button className="btn-danger" icon="fa-trash" text="Delete" />
        <Button className="btn-primary" icon="fa-plus" text="Create" />
      </div>
    </div>
  </>
);

const meta = {
  title: "Components/Layout/SectionToolbar",
  component: SectionToolbar,
  parameters: {
    docs: {
      description: {
        component:
          "Sticky page-section toolbar that hosts selection and action controls. The optional top offset is expressed in pixels.",
      },
    },
  },
  args: {
    top: "0",
    children: actions,
  },
  argTypes: {
    top: {
      control: "text",
      description: "Numeric top offset in pixels, without the `px` suffix.",
      table: { type: { summary: "string" } },
    },
    children: {
      control: false,
      description: "Selection controls and action buttons rendered inside the toolbar.",
      table: { type: { summary: "ReactNode" } },
    },
  },
} satisfies Meta<typeof SectionToolbar>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
