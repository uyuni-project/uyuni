import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { AsyncButton } from "./index";

const buttonTypes = ["btn-primary", "btn-default", "btn-danger", "btn-tertiary"];
const iconOptions = ["fa-floppy-o", "fa-refresh", "fa-download", "fa-trash"];

const resolveAfterDelay = () => new Promise<void>((resolve) => window.setTimeout(resolve, 800));
const rejectAfterDelay = () =>
  new Promise<void>((_resolve, reject) => window.setTimeout(() => reject(new Error("Example failure")), 800));

const meta = {
  title: "Components/Buttons/AsyncButton",
  component: AsyncButton,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Button for promise-based actions. It disables itself and displays a spinner while the promise is pending, then reflects success or failure.",
      },
    },
  },
  args: {
    action: resolveAfterDelay,
    text: "Save changes",
    icon: "fa-floppy-o",
    title: "Save changes",
    defaultType: "btn-primary",
    initialValue: "initial",
    type: "button",
    disabled: false,
  },
  argTypes: {
    action: {
      control: false,
      description:
        "Action invoked on click. Return a promise to enable pending and result states, or `false`/void to reset.",
      table: { type: { summary: "(...args: any[]) => Promise<any> | false | void" } },
    },
    text: {
      control: "text",
      description: "Content displayed after the icon. `children` can be used instead.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: "text",
      description: "Alternative button content used when `text` is omitted.",
      table: { type: { summary: "ReactNode" } },
    },
    icon: {
      control: "select",
      options: iconOptions,
      description: "Font Awesome class displayed before the button text.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text.",
      table: { type: { summary: "string" } },
    },
    defaultType: {
      control: "select",
      options: buttonTypes,
      description: "Uyuni button variant used for the normal and pending states.",
      table: { type: { summary: "string" }, defaultValue: { summary: "btn-default" } },
    },
    initialValue: {
      control: "select",
      options: ["initial", "failure"],
      description: "Initial visual state of the button.",
      table: { type: { summary: "string" }, defaultValue: { summary: "initial" } },
    },
    type: {
      control: "select",
      options: ["button", "submit", "reset"],
      description: "Native HTML button type.",
      table: { type: { summary: '"button" | "submit" | "reset"' }, defaultValue: { summary: "button" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents the action from being triggered.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes appended to the button.",
      table: { type: { summary: "string" } },
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred placement of the tooltip.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
  },
} satisfies Meta<typeof AsyncButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const RejectedAction: Story = {
  args: {
    action: rejectAfterDelay,
    text: "Run failing action",
  },
  parameters: {
    docs: { description: { story: "The button changes to its failure style when the promise rejects." } },
  },
};

export const InitialFailure: Story = {
  args: {
    initialValue: "failure",
    text: "Retry",
    icon: "fa-refresh",
  },
};
