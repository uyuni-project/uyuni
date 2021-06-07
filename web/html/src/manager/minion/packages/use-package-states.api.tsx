import Network from "utils/network";
import * as React from "react";
import { useState } from "react";
import { MessageType, Utils as MessagesUtils } from "components/messages";
import { ChangesMapObject, Package } from "./package.type";
import * as packageHelpers from "./package-utils";

type ActionType =
  | { type: "Save"; serverId: string; changed: ChangesMapObject }
  | { type: "Apply"; serverId: string }
  | { type: "GetServerPackages"; serverId: string }
  | { type: "Search"; serverId: string; filter: string };

const usePackageStatesApi = () => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [packageStates, setPackageStates] = useState<Array<Package>>([]);
  const [searchResults, setSearchResults] = useState<Array<Package>>([]);

  function onActionPackageStatesApi(action: ActionType): Promise<any> {
    switch (action.type) {
      case "Save": {
        const toSave: any[] = [];
        const changed = action.changed;
        for (const state in changed) {
          if (changed.hasOwnProperty(state) && typeof changed[state].value === "object") {
            toSave.push(changed[state].value);
          } else {
            console.log("Cannot save empty object.");
          }
        }
        return Network.post(
          "/rhn/manager/api/states/packages/save",
          JSON.stringify({
            sid: action.serverId,
            packageStates: toSave,
          })
        ).then((data: Array<Package>) => {
          updateAfterSave(data, changed);
          setMessages(MessagesUtils.info(t("Package states have been saved.")));
        });
      }
      case "Apply": {
        return Network.post(
          "/rhn/manager/api/states/apply",
          JSON.stringify({
            id: action.serverId,
            type: "SERVER",
            states: ["packages"],
          })
        ).then(data => {
          setMessages(
            MessagesUtils.info(
              <span>
                {t("Applying the packages states has been ")}
                <a href={"/rhn/systems/details/history/Event.do?sid=" + action.serverId + "&aid=" + data}>
                  {t("scheduled")}
                </a>
              </span>
            )
          );
        });
      }
      case "GetServerPackages": {
        return Network.get("/rhn/manager/api/states/packages?sid=" + action.serverId).then(
          (data: Array<Package>) => {
            updateAfterServerGetPackages(data);
          }
        );
      }
      case "Search": {
        return Network.get(
          "/rhn/manager/api/states/packages/match?sid=" + action.serverId + "&target=" + action.filter
        ).then((data: Array<Package>) => {
          updateAfterSearch(data);
          return null;
        });
      }
      default:
        return Promise.reject();
    }
  }

  function updateAfterSearch(serverSearchResults: Array<Package>): void {
    const newSearchResults = serverSearchResults.map(state => {
      state.packageStateId = packageHelpers.normalizePackageState(state.packageStateId);
      state.versionConstraintId = packageHelpers.normalizePackageVersionConstraint(state.versionConstraintId);
      return state;
    });
    setSearchResults(newSearchResults);
  }

  function updateAfterServerGetPackages(serverPackages: Array<Package>): void {
    const newPackageStates = serverPackages.map(state => {
      state.packageStateId = packageHelpers.normalizePackageState(state.packageStateId);
      state.versionConstraintId = packageHelpers.normalizePackageVersionConstraint(state.versionConstraintId);
      return state;
    });
    setPackageStates(newPackageStates);
  }

  function updateAfterSave(newServerPackages: Array<Package>, changed: ChangesMapObject): void {
    const newPackageStates: any = newServerPackages.map((state: Package) => {
      state.packageStateId = packageHelpers.normalizePackageState(state.packageStateId);
      return state;
    });
    const newSearchResults = searchResults.map<Package>((state: Package) => {
      const key = packageHelpers.packageStateKey(state);
      const tempchanged = changed[key];
      if (tempchanged !== undefined && typeof tempchanged.value === "object") {
        return tempchanged.value;
      } else {
        return state;
      }
    });
    setPackageStates(newPackageStates);
    setSearchResults(newSearchResults);
  }

  return {
    messages: messages,
    packageStates,
    searchResults,
    onActionPackageStatesApi,
  };
};

export default usePackageStatesApi;
