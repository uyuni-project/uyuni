import * as React from "react";

type SearchPanelPropss = {
  /** number representing the number of the first displayed item */
  fromItem: number;

  /** number representing the number of the last displayed item */
  toItem: number;

  /** total number of filtered items */
  itemCount: number;

  /** number of selected items */
  selectedCount: number;

  /** flag indicating whether to show the number of selected items */
  selectable?: boolean;

  /** function called when the clear button is clicked. This should reset the selection. */
  onClear: () => void;

  /** function called when the Select All button is clicked. Should set the selection */
  onSelectAll: () => void;
};

/** Panel containing the search fields for a table */
export function SelectedRowDetails(props: SearchPanelPropss) {
  return (
    <>
      {props.selectable && props.selectedCount > 0 && (
        <div className="selected-row-details">
          <span>
            {props.selectedCount === props.itemCount ? (
              <>
                {t("All {totalCount} items are selected.", { totalCount: props.itemCount })}&nbsp;
                <button className="btn btn-tertiary" onClick={props.onClear}>{t("Clear")}</button>
              </>
            ) : (
              <>
                {/* {t("{selectedCount} items selected.", { selectedCount: props.selectedCount })}&nbsp; */}
                {props.selectedCount === 1
                  ? t("1 item selected.")
                  : t("{selectedCount} items selected.", { selectedCount: props.selectedCount })}
                <button className="btn btn-tertiary" onClick={props.onSelectAll}>
                  {t("Select All {totalCount} items", { totalCount: props.itemCount })}
                </button>
                &nbsp;|&nbsp;
                <button className="btn btn-tertiary" onClick={props.onClear}>{t("Clear")}</button>
              </>
            )}
          </span>
        </div>
      )}
    </>
  );
}

SelectedRowDetails.defaultProps = {
  selectable: false,
  selectedCount: 0,
};
