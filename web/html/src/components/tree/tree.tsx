import * as React from "react";
import { useState } from "react";

import { CustomDiv } from "components/custom-objects";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

export type TreeItem = {
  id: string;
  data?: any;
  children?: string[];
};

export type TreeData = {
  rootId: string;
  items: TreeItem[];
};

export type Props = {
  data?: TreeData;
  renderItem: (item: TreeItem, renderNameColumn: Function) => React.ReactNode;
  header?: React.ReactNode;
  initiallyExpanded?: string[];
  onItemSelectionChanged?: (item: TreeItem, checked: boolean) => void;
  initiallySelected?: string[];
};

export const Tree = (props: Props) => {
  const [visibleSublists, setVisibleSublist] = useState(props.initiallyExpanded || []);
  const [selected, setSelected] = useState(props.initiallySelected || []);

  function isSublistVisible(id: string): boolean {
    return visibleSublists.indexOf(id) !== -1;
  }

  function handleVisibleSublist(id: string): void {
    setVisibleSublist((oldVisibleSublist) =>
      oldVisibleSublist.indexOf(id) !== -1
        ? oldVisibleSublist.filter((item) => item !== id)
        : oldVisibleSublist.concat([id])
    );
  }

  function isSelected(id: string): boolean {
    return selected.indexOf(id) !== -1;
  }

  function handleSelectionChange(changeEvent: React.ChangeEvent): void {
    if (changeEvent.target instanceof HTMLInputElement) {
      const { value: id, checked } = changeEvent.target;

      setSelected((oldSelected) =>
        checked ? oldSelected.concat([id]) : oldSelected.filter((itemId) => itemId !== id)
      );

      if (props.data) {
        const item = props.data.items.find((item) => item.id === id);
        if (!DEPRECATED_unsafeEquals(props.onItemSelectionChanged, null) && !DEPRECATED_unsafeEquals(item, null)) {
          props.onItemSelectionChanged(item, checked);
        }
      }
    }
  }

  function renderItem(item: TreeItem, idx: number) {
    const children = (!DEPRECATED_unsafeEquals(props.data, null) ? props.data.items : [])
      .filter((row) => (item.children || []).includes(row.id))
      .map((child, childIdx) => renderItem(child, childIdx));
    const sublistVisible = isSublistVisible(item.id);
    const openSubListIconClass = sublistVisible ? "fa-angle-down" : "fa-angle-right";

    const renderNameColumn = (name: string) => {
      const className = children.length > 0 ? "product-hover pointer" : "";
      return (
        <span className={`product-description ${className}`} onClick={() => handleVisibleSublist(item.id)}>
          {name}
        </span>
      );
    };

    return (
      <li key={item.id} className={idx % 2 === 1 ? "list-row-odd" : "list-row-even"}>
        <div className="product-details-wrapper" style={{ padding: ".7em" }}>
          {!DEPRECATED_unsafeEquals(props.onItemSelectionChanged, null) && (
            <CustomDiv className="col" width="2" um="em">
              <input
                type="checkbox"
                id={"checkbox-for-" + item.id}
                value={item.id}
                onChange={handleSelectionChange}
                checked={isSelected(item.id)}
              />
            </CustomDiv>
          )}
          <CustomDiv className="col" width="2" um="em">
            {children.length > 0 && (
              <i
                className={`fa ${openSubListIconClass} fa-1-5x pointer product-hover`}
                onClick={() => handleVisibleSublist(item.id)}
              />
            )}
          </CustomDiv>
          {props.renderItem(item, renderNameColumn)}
        </div>
        {children.length > 0 && sublistVisible && <ul className="product-list">{children}</ul>}
      </li>
    );
  }

  const data = props.data || { items: [{ id: "0" }], rootId: "0" };
  const rootNode = data.items.find((item) => item.id === data.rootId);

  if (DEPRECATED_unsafeEquals(rootNode, null)) {
    return <div>{t("Invalid data")}</div>;
  }

  const nodes = data.items
    .filter((item) => (rootNode.children || []).includes(item.id))
    .map((item, idx) => renderItem(item, idx));

  if (DEPRECATED_unsafeEquals(nodes, null) || nodes.length === 0) {
    return <div>{t("No data")}</div>;
  }

  return (
    <ul className="product-list">
      {!DEPRECATED_unsafeEquals(props.header, null) && (
        <li className="list-header">
          <div style={{ padding: ".7em" }}>
            {!DEPRECATED_unsafeEquals(props.onItemSelectionChanged, null) && (
              <CustomDiv key="header1" className="col" width="2" um="em" />
            )}
            <CustomDiv key="header2" className="col" width="2" um="em" />
            {props.header}
          </div>
        </li>
      )}
      {nodes}
    </ul>
  );
};
