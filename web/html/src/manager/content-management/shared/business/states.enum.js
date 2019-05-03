//@flow
import {objectDefaultValueHandler} from "core/utils/objects";
import _find from "lodash/find";

type stateType = {
  key: string,
    description: string
}

type statesEnumType = {
  [string]: stateType
}

const defaultState = {};

const statesEnum: statesEnumType = new Proxy({
    ATTACHED: {key: "ATTACHED", description: "added", deletion: false, edited: true},
    DETACHED: {key: "DETACHED", description: "deleted", deletion: true, edited: true},
    EDITED: {key: "EDITED", description: "edited", deletion: false, edited: true},
    BUILT: {key: "BUILT", description: "built", deletion: false, edited: false},
  },
  objectDefaultValueHandler(defaultState)
);

function findByKey(key: string) {
  return _find(statesEnum, entry => entry.key === key) || defaultState;
}

function isDeletion(key: string) {
  return findByKey(key).deletion;
}

function isEdited(key: string) {
  return findByKey(key).edited;
}


export default ({
  enum: statesEnum,
  findByKey,
  isDeletion,
  isEdited,
}: {
  enum: statesEnumType,
  findByKey: (string) => stateType,
  isDeletion: (string) => boolean,
  isEdited: (string) => boolean,
});
