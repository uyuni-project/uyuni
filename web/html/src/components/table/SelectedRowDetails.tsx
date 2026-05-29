import { Button } from "components/buttons";

type SelectedRowDetailsProps = {
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

/** Panel containing selected row details for a table */
export function SelectedRowDetails({ selectable = false, selectedCount = 0, ...props }: SelectedRowDetailsProps) {
  const isVisible = selectable && selectedCount > 0;
  const allSelected = selectedCount === props.itemCount;
  return (
    <div className={`selected-row-details ${isVisible ? "show-details" : "hide-details"}`}>
      {allSelected ? (
        <>
          {t(
            `All {totalCount, plural,
                    one {1 item}
                    other {{totalCount} items}
                  } across all pages selected.` as string,
            { totalCount: props.itemCount }
          )}
          <Button className="btn-tertiary ms-2" handler={props.onClear}>
            {t("Clear All")}
          </Button>
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
          <Button className="btn-tertiary ms-2" handler={props.onSelectAll}>
            {t(
              `Select all {totalCount, plural,
                    one {1 item}
                    other {{totalCount} items}
                  } across all pages` as string,
              { totalCount: props.itemCount }
            )}
          </Button>
          <span aria-hidden="true">&nbsp;|&nbsp;</span>
          <Button className="btn-tertiary" handler={props.onClear}>
            {t("Clear All")}
          </Button>
        </>
      )}
    </div>
  );
}
