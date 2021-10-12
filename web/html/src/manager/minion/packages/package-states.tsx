import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useEffect, useState, useRef } from "react";
import { useImmer } from "use-immer";
import { AsyncButton } from "components/buttons";
import { InnerPanel } from "components/panels/InnerPanel";
import { TextField } from "components/fields";
import { Messages } from "components/messages";
import withPageWrapper from "components/general/with-page-wrapper";
import { showErrorToastr } from "components/toastr/toastr";
import usePackageStatesApi from "./use-package-states.api";
import { ChangesMapObject, PackagesObject, Package, OptionalValue } from "./package.type";
import * as packageHelpers from "./package-utils";

type PropsType = { serverId: string };
type ViewType = "search" | "system" | "changes";

const PackageStates = ({ serverId }: PropsType) => {
  const [filter, setFilter] = useState<string>("");
  const [view, setView] = useState<ViewType | "">("system");
  const [tableRows, setTableRows] = useState<Array<PackagesObject>>([]);
  const [changed, setChanged] = useImmer<ChangesMapObject>({});
  const searchRef = useRef<AsyncButton | null>(null);

  const { messages, onActionPackageStatesApi, packageStates, searchResults } = usePackageStatesApi();

  useEffect(() => {
    onActionPackageStatesApi({ type: "GetServerPackages", serverId }).catch((error) => {
      showErrorToastr(error, { autoHide: false });
    });
  }, []);

  useEffect(() => {
    generateTableData();
  }, [changed, packageStates, searchResults, view]);

  useEffect(() => {
    if (view === "search") {
      triggerSearch();
    }
  }, [view]);

  function addChanged(
    original: Package,
    newPackageStateId: OptionalValue,
    newVersionConstraintId: OptionalValue
  ): void {
    const key = packageHelpers.packageStateKey(original);
    const currentState = changed[key];
    if (
      currentState !== undefined &&
      newPackageStateId === currentState.original.packageStateId &&
      newVersionConstraintId === currentState.original.versionConstraintId
    ) {
      setChanged((draft: ChangesMapObject) => {
        delete draft[key];
      });
    } else {
      setChanged((draft) => {
        draft[key] = {
          original: original,
          value: {
            arch: original.arch,
            epoch: original.epoch,
            version: original.version,
            release: original.release,
            name: original.name,
            packageStateId: newPackageStateId,
            versionConstraintId: newVersionConstraintId,
          },
        };
      });
    }
  }

  const applyPackageState = () => {
    onActionPackageStatesApi({ type: "Apply", serverId })
      .then((data) => {
        console.log("apply action queued:" + data);
      })
      .catch((error) => {
        showErrorToastr(error, { autoHide: false });
      });
  };

  const save = (): Promise<any> => {
    return onActionPackageStatesApi({ type: "Save", serverId, changed })
      .then(() => {
        setView("system");
        setChanged(() => {
          return {};
        });
      })
      .catch((error) => {
        showErrorToastr(error, { autoHide: false });
      });
  };

  const handleUndo = (packageState) => {
    return (): void => {
      setChanged((draft: ChangesMapObject) => {
        const key = packageHelpers.packageStateKey(packageState);
        delete draft[key];
      });
    };
  };

  const handleStateChangeEvent = (original) => {
    return (event): void => {
      const newPackageStateId: OptionalValue = packageHelpers.selectValue2PackageState(
        parseInt(event.target.value, 10)
      );
      const newPackageConstraintId: OptionalValue =
        newPackageStateId === packageHelpers.INSTALLED ? packageHelpers.LATEST : original.versionConstraintId;
      addChanged(original, newPackageStateId, newPackageConstraintId);
    };
  };

  const handleConstraintChangeEvent = (original) => {
    return (event): void => {
      const newPackageConstraintId: OptionalValue = packageHelpers.selectValue2VersionConstraints(
        parseInt(event.target.value, 10)
      );
      const key = packageHelpers.packageStateKey(original);
      const currentState: PackagesObject = changed[key];
      const currentPackageStateId: OptionalValue =
        currentState !== undefined && typeof currentState.value === "object"
          ? currentState.value.packageStateId
          : original.packageStateId;
      addChanged(original, currentPackageStateId, newPackageConstraintId);
    };
  };

  const onSearchChange = (event): void => {
    setFilter(event.target.value);
  };

  const triggerSearch = (): void => {
    searchRef.current?.trigger();
  };

  const search = (): Promise<any> => {
    return onActionPackageStatesApi({ type: "Search", serverId, filter })
      .then(() => {
        setView("search");
      })
      .catch((error) => {
        showErrorToastr(error, { autoHide: false });
      });
  };

  const changeTabUrl = (currentTab) => {
    setView(currentTab);
  };

  const generateTableData = (): void => {
    let rows: Array<PackagesObject> = [];
    if (view === "system") {
      for (const state of packageStates) {
        const key = packageHelpers.packageStateKey(state);
        const changedPackage = changed[key];
        if (changedPackage !== undefined) {
          rows.push(changedPackage);
        } else {
          rows.push({
            original: state,
          });
        }
      }
    } else if (view === "search") {
      for (const state of searchResults) {
        const key = packageHelpers.packageStateKey(state);
        const changedPackage = changed[key];
        if (changedPackage === undefined) {
          rows.push({
            original: state,
          });
        } else {
          rows.push(changedPackage);
        }
      }
    } else if (view === "changes") {
      for (const state in changed) {
        if (changed.hasOwnProperty(state)) {
          rows.push(changed[state]);
        } else {
          console.log("Cannot display emtpy object.");
        }
      }
    }
    setTableRows(rows);
  };

  const isApplyButtonDisabled = Object.keys(changed).length > 0;
  const buttons = [
    <AsyncButton id="save" action={save} text={t("Save")} disabled={!isApplyButtonDisabled} key={"save"} />,
    <span {...(isApplyButtonDisabled ? { title: t("Please save all your changes before applying!") } : {})} key="apply">
      <AsyncButton id="apply" action={applyPackageState} text={t("Apply changes")} disabled={isApplyButtonDisabled} />
    </span>,
  ];

  const headerTabs = () => {
    const length = Object.keys(changed).length;
    let changesText = t("Changes");
    if (length === 1) {
      changesText = t("1 Change");
    } else if (length > 1) {
      changesText = length + " " + t("Changes");
    }

    return (
      <div className="spacewalk-content-nav" id="package-states-tabs">
        <ul className="nav nav-tabs">
          <li className={view === "search" || view === "" ? "active" : ""}>
            <a href="#search" onClick={() => changeTabUrl("search")}>
              {t("Search")}
            </a>
          </li>
          <li className={view === "changes" ? "active" : ""}>
            <a href="#changes" onClick={() => changeTabUrl("changes")}>
              {changesText}
            </a>
          </li>
          <li className={view === "system" ? "active" : ""}>
            <a href="#system" onClick={() => changeTabUrl("system")}>
              {t("System")}
            </a>
          </li>
        </ul>
      </div>
    );
  };

  const renderSearchBar = () => {
    return (
      <div className={"row"} id={"search-row"}>
        <div className={"col-md-5"}>
          <div style={{ paddingBottom: 0.7 + "em" }}>
            <div className="input-group">
              <TextField
                id="package-search"
                value={filter}
                placeholder={t("Search package")}
                onChange={onSearchChange}
                onPressEnter={triggerSearch}
                className="form-control"
              />
              <span className="input-group-btn">
                <AsyncButton id="search" text={t("Search")} action={search} ref={searchRef} key={"searchButton"} />
              </span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const tableBody = () => {
    const elements: React.ReactNode[] = [];
    for (const row of tableRows) {
      const currentState = row.value !== undefined ? row.value : row.original;

      elements.push(
        <tr
          key={currentState.name}
          id={currentState.name + "-row"}
          className={row.value !== undefined ? "warning" : ""}
        >
          <td key={currentState.name}>{t(currentState.name)}</td>
          <td>{renderState(row, currentState)}</td>
        </tr>
      );
    }
    return (
      <tbody className="table-content">
        {elements.length > 0 ? (
          elements
        ) : (
          <tr>
            <td colSpan={2}>{view === "changes" ? t("No current changes.") : t("No package states.")}</td>
          </tr>
        )}
      </tbody>
    );
  };

  const renderState = (row, currentState) => {
    let versionConstraintSelect: React.ReactNode = null;
    let undoButton: React.ReactNode = null;

    if (currentState.packageStateId === packageHelpers.INSTALLED) {
      versionConstraintSelect = (
        <select
          id={currentState.name + "-version-constraint"}
          className="form-control"
          value={packageHelpers.versionConstraints2selectValue(currentState.versionConstraintId)}
          onChange={handleConstraintChangeEvent(row.original)}
        >
          <option value="0">{t("Latest")}</option>
          <option value="1">{t("Any")}</option>
        </select>
      );
    }

    const key = packageHelpers.packageStateKey(currentState);
    if (changed[key] !== undefined) {
      undoButton = (
        <button id={currentState.name + "-undo"} className="btn btn-default" onClick={handleUndo(row.original)}>
          {t("Undo")}
        </button>
      );
    }

    return (
      <div className="row">
        <div className={"col-md-3"}>
          <select
            key={currentState.name}
            id={currentState.name + "-pkg-state"}
            className="form-control"
            value={packageHelpers.packageState2selectValue(currentState.packageStateId)}
            onChange={handleStateChangeEvent(row.original)}
          >
            <option value="-1">{t("Unmanaged")}</option>
            <option value="0">{t("Installed")}</option>
            <option value="1">{t("Removed")}</option>
          </select>
        </div>
        <div className={"col-md-3"}>{versionConstraintSelect}</div>
        <div className={"col-md-3"}>{undoButton}</div>
      </div>
    );
  };

  return (
    <div>
      {messages ? <Messages items={messages} /> : null}
      <InnerPanel title={t("Package States")} icon="spacewalk-icon-package-add" buttons={buttons}>
        {headerTabs()}
        {view === "search" ? renderSearchBar() : null}
        <div className="row">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>{t("Package Name")}</th>
                <th>{t("State")}</th>
              </tr>
            </thead>
            {tableBody()}
          </table>
        </div>
      </InnerPanel>
    </div>
  );
};

export default hot(withPageWrapper<PropsType>(PackageStates));
