import * as React from "react";

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
            text={t("First")}
          />
          <PaginationButton
            onClick={() => onPageChange(currentPage - 1)}
            disabled={DEPRECATED_unsafeEquals(currentPage, 1)}
            text={t("Prev")}
          />
          <PaginationButton
            onClick={() => onPageChange(currentPage + 1)}
            disabled={DEPRECATED_unsafeEquals(currentPage, lastPage)}
            text={t("Next")}
          />
          <PaginationButton
            onClick={() => onPageChange(lastPage)}
            disabled={DEPRECATED_unsafeEquals(currentPage, lastPage)}
            text={t("Last")}
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
  text: React.ReactNode;
};

const PaginationButton = (props: PaginationButtonProps) => (
  <button type="button" className="btn btn-default" disabled={props.disabled} onClick={props.onClick}>
    {props.text}
  </button>
);

type ItemsPerPageSelectorProps = {
  currentValue: number;
  onChange: (value: number) => any;
};

const ItemsPerPageSelector = (props: ItemsPerPageSelectorProps) => (
  <select
    name="pageSize"
    className="display-number"
    defaultValue={props.currentValue}
    onChange={(e) => props.onChange(parseInt(e.target.value, 10))}
  >
    {[5, 10, 15, 25, 50, 100, 250, 500].map((o) => (
      <option value={o} key={o}>
        {o}
      </option>
    ))}
  </select>
);

type PageSelectorProps = {
  lastPage: number;
  currentValue: number;
  onChange: (value: number) => any;
};

const PageSelector = (props: PageSelectorProps) => {
  if (props.lastPage > 1) {
    return (
      <div className="table-page-information">
        {t("Page <dropdown></dropdown> of {total}", {
          dropdown: () => (
            <select
              name="pageNumber"
              className="display-number small-select"
              value={props.currentValue}
              onChange={(e) => props.onChange(parseInt(e.target.value, 10))}
              key="select"
            >
              {Array.from(Array(props.lastPage)).map((_, i) => (
                <option value={i + 1} key={i + 1}>
                  {i + 1}
                </option>
              ))}
            </select>
          ),
          total: props.lastPage,
        })}
      </div>
    );
  } else {
    return (
      <div className="table-page-information">
        {t("Page {current} of {total}", { current: props.currentValue, total: props.lastPage })}
      </div>
    );
  }
};

export { PaginationBlock, ItemsPerPageSelector };
