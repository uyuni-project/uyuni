export const AccessMode = {
  NONE: "",
  READ: "R",
  WRITE: "W",
  READ_WRITE: "RW",
} as const;

export type AccessModeValue = (typeof AccessMode)[keyof typeof AccessMode];

export const AccessModeByPermissionType = {
  view: AccessMode.READ,
  modify: AccessMode.WRITE,
} as const;

export type PermissionType = keyof typeof AccessModeByPermissionType;
