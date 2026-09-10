import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { PaginationBlock } from "./pagination";

type PaginationBlockProps = React.ComponentProps<typeof PaginationBlock>;

const StatefulPaginationBlock = (props: PaginationBlockProps) => {
  const [currentPage, setCurrentPage] = useState(props.currentPage);
  const lastPage = Math.max(1, props.lastPage);

  useEffect(() => setCurrentPage(Math.min(Math.max(1, props.currentPage), lastPage)), [props.currentPage, lastPage]);

  return (
    <PaginationBlock
      {...props}
      currentPage={currentPage}
      lastPage={lastPage}
      onPageChange={(page) => {
        const nextPage = Math.min(Math.max(1, page), lastPage);
        setCurrentPage(nextPage);
        props.onPageChange(nextPage);
      }}
    />
  );
};

const meta = {
  title: "Components/Navigation/PaginationBlock",
  component: PaginationBlock,
  parameters: {
    docs: {
      description: {
        component:
          "Controlled pagination containing a page selector and first, previous, next, and last navigation buttons.",
      },
    },
  },
  args: {
    currentPage: 3,
    lastPage: 8,
    onPageChange: action("page changed"),
  },
  argTypes: {
    currentPage: {
      control: { type: "number", min: 1, step: 1 },
      description: "Currently selected one-based page number.",
      table: { type: { summary: "number" } },
    },
    lastPage: {
      control: { type: "number", min: 1, step: 1 },
      description: "Number of the final available page. Pagination is hidden when this is one.",
      table: { type: { summary: "number" } },
    },
    onPageChange: {
      action: "page changed",
      description: "Called with the requested one-based page number.",
      table: { type: { summary: "(page: number) => any" } },
    },
  },
  render: (args) => <StatefulPaginationBlock {...args} />,
} satisfies Meta<typeof PaginationBlock>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const FirstPage: Story = {
  args: { currentPage: 1 },
};

export const LastPage: Story = {
  args: { currentPage: 8 },
};
