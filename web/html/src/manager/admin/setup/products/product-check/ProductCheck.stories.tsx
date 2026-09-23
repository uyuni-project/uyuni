import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { type ProductSelectionState } from "./product-selection.utils";
import { ProductCheck } from "./ProductCheck";

type ProductCheckProps = React.ComponentProps<typeof ProductCheck>;

const ControlledProductCheck = (props: ProductCheckProps & { selectionState: ProductSelectionState }) => {
  const [selectionState, setSelectionState] = useState(props.selectionState);

  useEffect(() => setSelectionState(props.selectionState), [props.selectionState]);

  return (
    <label>
      <ProductCheck
        {...props}
        selectionState={selectionState}
        onChange={(checked) => {
          setSelectionState(checked ? "checked" : "unchecked");
          props.onChange?.(checked);
        }}
      />{" "}
      Include product
    </label>
  );
};

const meta = {
  title: "Components/Inputs/ProductCheck",
  component: ProductCheck,
  parameters: {
    docs: {
      description: {
        component:
          "Tri-state product-selection checkbox. It translates checked, partially selected, and unchecked product state into native checkbox properties.",
      },
    },
  },
  args: {
    selectionState: "partially",
    disabled: false,
    onChange: action("selection changed"),
  },
  argTypes: {
    selectionState: {
      control: "select",
      options: ["checked", "partially", "unchecked"],
      description: "Product selection state rendered as checked, indeterminate, or unchecked.",
    },
    checked: { control: false },
    indeterminate: { control: false },
    disabled: { control: "boolean", description: "Prevents changing the selection." },
    onChange: { action: "selection changed", description: "Called with the next native checked state." },
  },
  render: (args) => <ControlledProductCheck {...args} selectionState={args.selectionState ?? "unchecked"} />,
} satisfies Meta<typeof ProductCheck>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <label>
          <ProductCheck selectionState="unchecked" onChange={() => undefined} /> Unchecked
        </label>
        <label>
          <ProductCheck selectionState="partially" onChange={() => undefined} /> Partially selected
        </label>
        <label>
          <ProductCheck selectionState="checked" onChange={() => undefined} /> Checked
        </label>
        <label>
          <ProductCheck selectionState="checked" disabled onChange={() => undefined} /> Disabled
        </label>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
  },
};
