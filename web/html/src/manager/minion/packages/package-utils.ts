import { OptionalValue, Package } from "./package.type";

export const UNMANAGED: any = {};
export const INSTALLED: OptionalValue = { value: 0 };
export const REMOVED: OptionalValue = { value: 1 };
export const PURGED: OptionalValue = { value: 2 };

export const LATEST: OptionalValue = { value: 0 };
export const ANY: OptionalValue = { value: 1 };

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
  return ps.value !== undefined ? ps.value : -1;
}

export function versionConstraints2selectValue(vc: OptionalValue): number {
  return vc.value === undefined ? 1 : vc.value;
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
