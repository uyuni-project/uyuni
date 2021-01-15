export type PagedData = {
  items: Array<any>;
  total: number;
};

export type Comparator = (a: any, b: any, key: string, direction: number) => number;
