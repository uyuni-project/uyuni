import { Virtuoso } from "react-virtuoso";

import styles from "./VirtualList.module.scss";

type ListProps<T> = {
  renderItem: (item: T) => JSX.Element;
  items: T[];
  itemKey: (item: T) => string | number;
  defaultItemHeight?: number;
};

const VirtualList = <T,>(props: ListProps<T>) => {
  const itemContent = (index: number, item: T) => {
    return props.renderItem(item);
  };

  const computeItemKey = (index: number, item: T) => props.itemKey(item);

  return (
    <div className={styles.listWrapper}>
      <Virtuoso
        data={props.items}
        itemContent={itemContent}
        computeItemKey={computeItemKey}
        overscan={20}
        defaultItemHeight={props.defaultItemHeight}
      />
    </div>
  );
};

export default VirtualList;
