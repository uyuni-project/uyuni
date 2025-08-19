export interface MigrationProduct {
  id: number;
  name: string;
  addons: MigrationProduct[];
}

export interface MigrationTarget {
  id: string;
  targetProduct: MigrationProduct;
  missingChannels: string[];
}
