import { type CheckProps, Check } from "components/input";

import { type ProductSelectionState } from "./product-selection.utils";

type ProductCheckBaseProps = Omit<CheckProps, "checked" | "indeterminate">;

type ProductCheckWithSelectionState = ProductCheckBaseProps & {
  selectionState: ProductSelectionState;
  checked?: never;
  indeterminate?: never;
};

type ProductCheckWithExplicitState = ProductCheckBaseProps & {
  selectionState?: never;
  checked: boolean;
  indeterminate?: boolean;
};

export type ProductCheckProps = ProductCheckWithSelectionState | ProductCheckWithExplicitState;

export const ProductCheck = ({ checked, indeterminate, selectionState, ...rest }: ProductCheckProps) => {
  const isChecked = selectionState ? selectionState === "checked" : checked;
  const isIndeterminate = selectionState ? selectionState === "partially" : indeterminate;

  return <Check {...rest} noLabel inputClass="" checked={isChecked} indeterminate={isIndeterminate} />;
};
