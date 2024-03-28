import { OptionalValue, Package } from "./package.type";

export const UNMANAGED: OptionalValue = undefined;
export const INSTALLED: OptionalValue = 0;
export const REMOVED: OptionalValue = 1;
export const PURGED: OptionalValue = 2;

export const LATEST: OptionalValue = 0;
export const ANY: OptionalValue = 1;

export function selectValue2PackageState(value: number): OptionalValue {
  switch (value) {
    case -1:
      return UNMANAGED;
    case 0:
      return INSTALLED;
    case 1:
      return REMOVED;
    case 2:
      return PURGED;
    default:
      return UNMANAGED;
  }
}

export function packageState2selectValue(ps: OptionalValue): number {
  return ps !== undefined ? ps : -1;
}

export function versionConstraints2selectValue(vc: OptionalValue): number {
  return vc !== undefined ? vc : 1;
}

export function normalizePackageState(ps: OptionalValue): OptionalValue {
  return selectValue2PackageState(packageState2selectValue(ps));
}

export function normalizePackageVersionConstraint(vc: OptionalValue): OptionalValue {
  return selectValue2VersionConstraints(versionConstraints2selectValue(vc));
}

export function selectValue2VersionConstraints(value: number): OptionalValue {
  switch (value) {
    case 0:
      return LATEST;
    case 1:
      return ANY;
    default:
      return LATEST;
  }
}

export function packageStateKey(packageState: Package): string {
  const version: string = typeof packageState.version === "string" ? packageState.version : "null";
  const epoch: string = typeof packageState.epoch === "string" ? packageState.epoch : "null";
  const release: string =
    typeof packageState.release === "string" && packageState.release ? packageState.release : "null";
  return packageState.name + version + release + epoch + packageState.arch;
}
