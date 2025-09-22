export interface PagedData {
  items: any[];
  total: number;
}

export type Comparator = (a: any, b: any, key: string, direction: number) => number;
