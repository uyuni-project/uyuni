export const AccessMode = {
  NONE: "",
  READ: "R",
  WRITE: "W",
  READ_WRITE: "RW",
} as const;

export type AccessModeValue = (typeof AccessMode)[keyof typeof AccessMode];
