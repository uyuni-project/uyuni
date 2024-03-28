export type OptionalValue = number | undefined;

export type Package = {
  arch: string;
  name: string;
  packageStateId: OptionalValue;
  versionConstraintId: OptionalValue;
  epoch?: string;
  release?: string;
  version?: string;
};

export type PackagesObject = {
  original: Package;
  value?: Package;
};

export type ChangesMapObject = {
  [key: string]: PackagesObject;
};
