import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { MigrationProductList } from "./MigrationProductList";
import type { MigrationProduct } from "./types";

const product: MigrationProduct = {
  id: 15,
  name: "SUSE Linux Enterprise Server 15 SP6",
  addons: [
    {
      id: 151,
      name: "Basesystem Module 15 SP6",
      addons: [],
    },
    {
      id: 152,
      name: "Server Applications Module 15 SP6",
      addons: [
        {
          id: 1521,
          name: "Containers Module 15 SP6",
          addons: [],
        },
      ],
    },
  ],
};

const meta = {
  title: "Components/Data Display/MigrationProductList",
  component: MigrationProductList,
  parameters: {
    docs: {
      description: {
        component:
          "Displays a migration product and its recursively nested add-ons. It is shared by the target, channel-selection, and confirmation steps of product migration.",
      },
    },
  },
  args: {
    product,
  },
  argTypes: {
    className: {
      control: "text",
      description: "Additional class applied to the outer product list.",
    },
    product: {
      control: "object",
      description: "Product hierarchy to display.",
    },
    customAddonRenderer: {
      control: false,
      description: "Optional renderer used for every add-on product.",
    },
  },
} satisfies Meta<typeof MigrationProductList>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const CustomAddonRenderer: Story = {
  args: {
    customAddonRenderer: (id, name) => (
      <span>
        {name} <span className="label label-info">ID {id}</span>
      </span>
    ),
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Uses the customization hook to annotate each add-on while preserving the recursive hierarchy.",
      },
    },
  },
};
