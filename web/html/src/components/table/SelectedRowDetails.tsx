import { Button } from "components/buttons";

type SearchPanelPropss = {
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
export function SelectedRowDetails({ selectable = false, selectedCount = 0, ...props }: SearchPanelPropss) {
  return (
    <>
      {selectable && selectedCount > 0 && (
        <div className="selected-row-details">
          <span>
            {selectedCount === props.itemCount ? (
              <>
                {t(
                  `All {totalCount, plural,
                    one {1 item selected.}
                    other {{totalCount} items selected.}
                  }` as string,
                  { totalCount: props.itemCount }
                )}
                <button className="btn btn-tertiary ms-2" onClick={props.onClear}>
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
                  { itemCount: selectedCount }
                )}
                <Button className="btn btn-tertiary ms-2" handler={props.onSelectAll}>
                  {t(
                    `Select all {totalCount, plural,
                    one {1 item.}
                    other {{totalCount} items.}
                  }` as string,
                    { totalCount: props.itemCount }
                  )}
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
