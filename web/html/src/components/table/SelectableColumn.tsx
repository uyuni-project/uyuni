import { Column, ColumnProps } from "./Column";
import { UseSelected } from "./useSelected";

type Props = ColumnProps & {
  selected: UseSelected;
};

export const SelectableColumn = (props: Props) => {
  const { selected, ...rest } = props;
  return (
    <Column
      {...rest}
      width={typeof props.width !== "undefined" || props.header ? props.width : "30px"}
      columnClass={`text-center ${props.columnClass ?? ""}`}
      onClick={() => props.selected.toggle(props.data)}
      cell={
        // TODO: Use the new Checkbox here once that PR is merged
        <input
          type="checkbox"
          checked={props.selected.isSelected(props.data)}
          // indeterminate={props.selected.isIndeterminate(props.data)}
          style={props.selected.isIndeterminate(props.data) ? { boxShadow: "0 0 0 2px red" } : undefined}
          readOnly
        />
      }
    />
  );
};
