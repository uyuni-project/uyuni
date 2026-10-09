import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { BootstrapPanel } from "./BootstrapPanel";

const meta = {
  title: "Components/Panels/BootstrapPanel",
  component: BootstrapPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Compatibility panel with an h2 heading. It supports the same title, icon, header, footer, and action areas as Panel.",
      },
    },
  },
  args: {
    title: "System overview",
    icon: "fa-server",
    children: <p>Reusable content appears in the panel body.</p>,
    header: <span>Updated a moment ago</span>,
    footer: <span>3 systems</span>,
    buttons: <Button className="btn-default btn-sm" text="Refresh" handler={action("refresh clicked")} />,
  },
  argTypes: {
    title: { control: "text", description: "Panel heading." },
    icon: { control: "text", description: "Font Awesome icon class displayed before the title." },
    children: { control: false, description: "Panel body content." },
    header: { control: false, description: "Additional content below the title." },
    footer: { control: false, description: "Optional panel footer." },
    buttons: { control: false, description: "Actions aligned to the right side of the heading." },
  },
} satisfies Meta<typeof BootstrapPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
