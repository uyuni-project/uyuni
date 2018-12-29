/* eslint-disable */
'use strict';

var React = require("react");

const PaginationBlock = (props) => {
  const currentPage = props.currentPage;
  const lastPage = props.lastPage;
  const onPageChange = props.onPageChange;

  const pagination = lastPage > 1 ?
    <div className="spacewalk-list-pagination">
      <div className="spacewalk-list-pagination-btns btn-group">
        <PaginationButton onClick={() => onPageChange(1)} toPage={1} disabled={currentPage == 1} text={t("First")} />
        <PaginationButton onClick={() => onPageChange(currentPage - 1)} disabled={currentPage == 1} text={t("Prev")} />
        <PaginationButton onClick={() => onPageChange(currentPage + 1)} disabled={currentPage == lastPage} text={t("Next")} />
        <PaginationButton onClick={() => onPageChange(lastPage)} disabled={currentPage == lastPage} text={t("Last")} />
      </div>
    </div> :
    null
  ;

  return (
    <div>
      <PageSelector
        onChange={(p) => onPageChange(p)}
        currentValue={currentPage}
        lastPage={lastPage}
      />
      {pagination}
    </div>
  );
}

const PaginationButton = (props) =>
  <button type="button" className="btn btn-default"
    disabled={props.disabled} onClick={props.onClick}>
    {props.text}
  </button>
;

const ItemsPerPageSelector = (props) =>
  <select className="display-number"
    defaultValue={props.currentValue}
    onChange={(e) => props.onChange(parseInt(e.target.value))}>
      {[5,10,15,25,50,100,250,500].map((o) => <option value={o} key={o}>{o}</option>)}
  </select>
;

const PageSelector = (props) => {
  if (props.lastPage > 1) {
    return (
      <div className="table-page-information">
        {t('Page')}
        &nbsp;
        <select className="display-number small-select"
          defaultValue={props.currentValue}
          value={props.currentValue}
          onChange={(e) => props.onChange(parseInt(e.target.value))}>
            {Array.from(Array(props.lastPage)).map((o, i) => <option value={i + 1} key={i + 1}>{i + 1}</option>)}
        </select>
        &nbsp;
        {t('of')}
        &nbsp;
        {props.lastPage}
      </div>
    )
  }
  else {
    return (
      <div className="table-page-information">
        {t('Page {0} of {1}', props.currentValue, props.lastPage)}
      </div>
    )
  }
}


module.exports = {
    PaginationBlock : PaginationBlock,
    ItemsPerPageSelector : ItemsPerPageSelector,
}
