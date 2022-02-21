import _find from "lodash/find";

type stateType = {
  key: string;
  description: string;
  deletion: boolean;
  edited: boolean;
  sign: string;
};

type statesEnumType = {
  [key: string]: stateType;
};

const statesEnum: statesEnumType = {
  ATTACHED: { key: "ATTACHED", description: "added", deletion: false, edited: true, sign: "+" },
  DETACHED: { key: "DETACHED", description: "deleted", deletion: true, edited: true, sign: "-" },
  EDITED: { key: "EDITED", description: "edited", deletion: false, edited: true, sign: " " },
  BUILT: { key: "BUILT", description: "built", deletion: false, edited: false, sign: " " },
};

function findByKey(key: string): stateType | Partial<stateType> {
  return _find(statesEnum, (entry) => entry.key === key) || {};
}

function isDeletion(key: string) {
  return findByKey(key).deletion;
}

function isEdited(key: string) {
  return findByKey(key).edited;
}

export default {
  enum: statesEnum,
  findByKey,
  isDeletion,
  isEdited,
};
