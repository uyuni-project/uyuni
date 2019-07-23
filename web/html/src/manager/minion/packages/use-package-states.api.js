//@flow
import Network from "utils/network";
import React, {useState} from "react";
import {Utils as MessagesUtils} from "components/messages";
import type {
  ChangesMapObject,
  Package,
} from "./package.type";
import * as packageHelpers from "./package-utils";

const action = {
  SAVE: "Save",
  APPLY: "Apply",
  GETSERVERPACKAGES: "GetServerPackages",
  SEARCH: "Search"
};

const usePackageStatesApi = () => {
  const [messages, setMessages] = useState("");
  const [packageStates, setPackageStates] = useState<Array<Package>>([]);
  const [searchResults, setSearchResults] = useState<Array<Package>>([]);

  function fetchPackageStatesApi(apiAction: string,
                                 serverId: string,
                                 filter: string = "",
                                 toSave: Array<Package> = [],
                                 changed: ChangesMapObject = {}): Promise<any> {
    if (apiAction === action.SAVE) {
      console.log("Save posted");
      return Network.post(
        "/rhn/manager/api/states/packages/save",
        JSON.stringify({
          sid: serverId,
          packageStates: toSave
        }),
        "application/json"
      ).promise
        .then((data: Array<Package>) => {
            console.log("Save success: (data in next line)");
            console.log(data);
            updateAfterSave(data, changed);
            setMessages(MessagesUtils.info(t('Package states have been saved.')));
          }
        )
    } else if (apiAction === action.APPLY) {
      console.log("Apply posted");
      return Network.post(
        "/rhn/manager/api/states/apply",
        JSON.stringify({
          id: serverId,
          type: "SERVER",
          states: ["packages"]
        }),
        "application/json"
      ).promise
        .then((data) => {
          console.log("Apply success");
          setMessages(MessagesUtils.info(<span>{t("Applying the packages states has been ")}
            <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
              </span>));
        });
    } else if (apiAction === action.GETSERVERPACKAGES) {
      console.log("Getserverpackages executed");
      return Network.get(
        "/rhn/manager/api/states/packages?sid=" + serverId
      ).promise
        .then((data: Array<Package>) => {
          console.log("Successfully got server packages.");
          updateAfterServerGetPackages(data);
        });
    } else if (apiAction === action.SEARCH) {
      console.log("Search executed");
      return Network.get(
        "/rhn/manager/api/states/packages/match?sid=" + serverId + "&target=" + filter
      ).promise
        .then((data: Array<Package>) => {
          console.log("Search Results:");
          console.log(data);
          updateAfterSearch(data);
          return null;
        })
    }
    return Promise.reject();
  }

  function updateAfterSearch(serverSearchResults: Array<Package>): void {
    const newSearchResults = serverSearchResults.map((state) => {
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
    const newSearchResults =
      searchResults.map<Package>((state: Package) => {
        const key = packageHelpers.packageStateKey(state);
        const tempchanged = changed[key];
        if (tempchanged !== undefined && typeof tempchanged.value === 'object') {
          return tempchanged.value;
        } else {
          return state;
        }
      });
    setPackageStates(newPackageStates);
    setSearchResults(newSearchResults);
  }

  return {
    messages: messages, packageStates, searchResults, fetchPackageStatesApi
  }

};

export default usePackageStatesApi;
