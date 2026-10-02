import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { TopPanel } from "./TopPanel";

const meta = {
  title: "Components/Panels/TopPanel",
  component: TopPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Page-level heading with an optional icon, documentation link, right-aligned action, and content below the toolbar.",
      },
    },
  },
  args: {
    title: "Systems",
    icon: "fa-server",
    helpUrl: "reference/systems/system-details.html",
    button: <Button className="btn-primary" icon="fa-plus" text="Create system" handler={action("create clicked")} />,
    children: <p>Page content begins below the top panel.</p>,
  },
  argTypes: {
    title: { control: "text", description: "Translated page heading." },
    icon: { control: "text", description: "Font Awesome icon class displayed before the title." },
    helpUrl: { control: "text", description: "Relative documentation URL used by HelpLink." },
    button: { control: false, description: "Optional action rendered on the right side of the heading." },
    children: { control: false, description: "Page content rendered below the heading." },
  },
} satisfies Meta<typeof TopPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
