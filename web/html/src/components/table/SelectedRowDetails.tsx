import * as React from "react";

import { Button } from "components/buttons";

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
                <button className="btn btn-tertiary" onClick={props.onClear}>
                  {t("Clear")}
                </button>
              </>
            ) : (
              <>
                {t(
                  `{itemCount, plural,
                    one {1 item selected.}
                    other {{itemCount} items selected.}
                  }` as string,
                  { itemCount: props.selectedCount }
                )}
                <Button className="btn btn-tertiary" handler={props.onSelectAll}>
                  {t("Select All {totalCount} items", { totalCount: props.itemCount })}
                </Button>
                <span aria-hidden="true">&nbsp;|&nbsp;</span>
                <Button className="btn btn-tertiary" handler={props.onClear}>
                  {t("Clear")}
                </Button>
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
