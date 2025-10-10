import * as React from "react";
import { useEffect, useRef } from "react";

import { Button, DropdownButton } from "components/buttons";

import { DEPRECATED_unsafeEquals } from "utils/legacy";
type PaginationBlockProps = {
  currentPage: number;
  lastPage: number;
  onPageChange: (page: number) => any;
};

const PaginationBlock = (props: PaginationBlockProps) => {
  const currentPage = props.currentPage;
  const lastPage = props.lastPage;
  const onPageChange = props.onPageChange;
  const pagination =
    lastPage > 1 ? (
      <div className="spacewalk-list-pagination">
        <div className="spacewalk-list-pagination-btns btn-group">
          <PaginationButton
            onClick={() => onPageChange(1)}
            disabled={DEPRECATED_unsafeEquals(currentPage, 1)}
            icon="fa-angle-double-left"
            ariaLabel="Go to first page"
            title="First page"
          />
          <PaginationButton
            onClick={() => onPageChange(currentPage - 1)}
            disabled={DEPRECATED_unsafeEquals(currentPage, 1)}
            icon="fa-angle-left"
            ariaLabel="Go to previous page"
            title="Previous"
          />
          <PaginationButton
            onClick={() => onPageChange(currentPage + 1)}
            disabled={DEPRECATED_unsafeEquals(currentPage, lastPage)}
            icon="fa-angle-right"
            ariaLabel="Go to next page"
            title="Next"
          />
          <PaginationButton
            onClick={() => onPageChange(lastPage)}
            disabled={DEPRECATED_unsafeEquals(currentPage, lastPage)}
            icon="fa-angle-double-right"
            ariaLabel="Go to last page"
            title="Last page"
          />
        </div>
      </div>
    ) : null;

  return (
    <div>
      <PageSelector onChange={(p) => onPageChange(p)} currentValue={currentPage} lastPage={lastPage} />
      {pagination}
    </div>
  );
};

type PaginationButtonProps = {
  disabled?: boolean;
  onClick: (...args: any[]) => any;
  text?: React.ReactNode;
  icon?: React.ReactNode;
  title?: string;
  ariaLabel: string;
};
const PaginationButton = (props: PaginationButtonProps) => {
  const btnRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const el = btnRef.current;
    if (!el) return;
    if (props.disabled) {
      el.removeAttribute("data-bs-original-title");
    }
  }, [props.disabled]);

  return (
    <button
      ref={btnRef}
      className="btn btn-tertiary"
      data-bs-toggle={props.title && !props.disabled ? "tooltip" : undefined}
      title={props.title && !props.disabled ? props.title : undefined}
      aria-label={props.ariaLabel}
      disabled={props.disabled}
      onClick={props.onClick}
    >
      <i className={`pagination-icon fa ${props.icon}`} />
      {props.text}
    </button>
  );
};

type ItemsPerPageSelectorProps = {
  currentValue: number;
  onChange: (value: number) => any;
  itemCount: number;
  fromItem: number;
  toItem: number;
};

const ItemsPerPageSelector = (props: ItemsPerPageSelectorProps) => {
  const testIdPrefix = `data-testid-pageSize-child`;

  return (
    <div>
      <DropdownButton
        text={t("Items {from} - {to} of {total}", { from: props.fromItem, to: props.toItem, total: props.itemCount })}
        className={`page-selector ${testIdPrefix}__control`}
        items={[5, 10, 15, 25, 50, 100, 250, 500].map((o) => (
          <Button
            key={o}
            className={`dropdown-item justify-content-between ${testIdPrefix}__option`}
            handler={(e) => {
              e.preventDefault();
              props.onChange(o);
            }}
          >
            <div>{t(`${o} per page`)}</div>
            <div>{props.currentValue === o ? <i className="fa fa-check" /> : null}</div>
          </Button>
        ))}
      />
    </div>
  );
};

type PageSelectorProps = {
  lastPage: number;
  currentValue: number;
  onChange: (value: number) => any;
};

const PageSelector = (props: PageSelectorProps) => {
  return props.lastPage > 1 ? (
    <div className="table-page-information me-5">
      {t(
        `{total, plural,
            one {<dropdown></dropdown> of # page}
            other {<dropdown></dropdown> of # pages}
        }`,
        {
          dropdown: () => (
            <select
              key="page-selector-select"
              className="display-number small-select"
              value={props.currentValue}
              onChange={(e) => props.onChange(parseInt(e.target.value, 10))}
            >
              {Array.from(new Array(props.lastPage)).map((_, i) => (
                <option value={i + 1} key={i + 1}>
                  {i + 1}
                </option>
              ))}
            </select>
          ),
          total: props.lastPage,
        }
      )}
    </div>
  ) : null;
};

export { PaginationBlock, ItemsPerPageSelector };
