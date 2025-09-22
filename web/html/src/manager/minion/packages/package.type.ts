export type OptionalValue = number | undefined;

export interface Package {
  arch: string;
  name: string;
  packageStateId: OptionalValue;
  versionConstraintId: OptionalValue;
  epoch?: string;
  release?: string;
  version?: string;
}

export interface PackagesObject {
  original: Package;
  value?: Package;
}

export type ChangesMapObject = Record<string, PackagesObject>;
