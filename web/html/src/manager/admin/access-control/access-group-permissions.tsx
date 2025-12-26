import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import debounce from "lodash/debounce";

import { AccessGroupState } from "manager/admin/access-control/access-group";

import { Button } from "components/buttons";
import { DEPRECATED_Check, Form } from "components/input";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { MessagesContainer, showErrorToastr } from "components/toastr";

import Network from "utils/network";

import styles from "./AccessGroup.module.scss";

type Props = {
  state: AccessGroupState;
  onChange: (changes: Record<string, any | undefined>) => void;
  errors: any;
};

type NamespaceItem = {
  namespace: string;
  name: string;
  description?: string;
  isAPI: boolean;
  children?: NamespaceItem[];
  accessMode?: string[];
};

const AccessGroupPermissions = (props: Props) => {
  const [namespaces, setNamespaces] = useState<NamespaceItem[]>([]);
  const checkboxRefs = useRef({});
  const [apiNamespace, setApiNamespace] = useState(false);
  const [webNamespace, setWebNamespace] = useState(false);
  const [showOnlySelected, setShowOnlySelected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchValue, setSearchValue] = useState("");
  const [expandedKeys, setExpandedKeys] = useState<Set<string>>(new Set());
  const [expandCollapseAll, setExpandCollapseAll] = useState(false);
  const agAppliedRef = useRef(false);

  const isItemDisabled = useCallback((item, type) => {
    const requiredAccessMode = type === "view" ? "R" : "W";

    if (!item.children || item.children.length === 0) {
      return !item.accessMode.includes(requiredAccessMode);
    }

    return item.children.every((child) => isItemDisabled(child, type));
  }, []);

  const getCheckState = useCallback(
    (item, type) => {
      if (item.children && item.children.length > 0) {
        const enabledChildren = item.children.filter((child) => !isItemDisabled(child, type));

        if (enabledChildren.length === 0) {
          return "unchecked";
        }

        const childStates = enabledChildren.map((child) => getCheckState(child, type));
        if (childStates.every((s) => s === "checked")) return "checked";
        if (childStates.every((s) => s === "unchecked")) return "unchecked";
        return "partially";
      }
      const permission = props.state.permissions[item.namespace];
      return permission && permission[type] ? "checked" : "unchecked";
    },
    [props.state.permissions, isItemDisabled]
  );

  const handleChange = (item, type) => {
    const changes = {};

    const collectChanges = (currentItem, forceValue: boolean | null = null) => {
      if (isItemDisabled(currentItem, type)) {
        return;
      }

      const isParent = currentItem.children && currentItem.children.length > 0;

      if (isParent) {
        const newValue = forceValue !== null ? forceValue : getCheckState(currentItem, type) !== "checked";
        currentItem.children.forEach((child) => {
          if (!isItemDisabled(child, type)) {
            collectChanges(child, newValue);
          }
        });
      } else {
        const existingPermission = props.state.permissions[currentItem.namespace];
        const newPermission = {
          ...existingPermission,
          ...currentItem,
          [type]: forceValue !== null ? forceValue : !(existingPermission && existingPermission[type]),
        };

        if (!newPermission.view && !newPermission.modify) {
          changes[currentItem.namespace] = undefined;
        } else {
          changes[currentItem.namespace] = newPermission;
        }
      }
    };

    collectChanges(item);

    if (Object.keys(changes).length > 0) {
      props.onChange(changes);
    }
  };

  const getNamespaces = (filter: string) => {
    let endpoint = "/rhn/manager/api/admin/access-control/access-group/list_namespaces";
    const hasCopy = props.state.accessGroups && props.state.accessGroups.length > 0;
    const hasFilter = filter && filter.trim().length > 0;

    if (hasCopy && hasFilter) {
      endpoint += `?copyFrom=${props.state.accessGroups.join(",")}&filter=${filter}`;
    } else if (hasCopy) {
      endpoint += `?copyFrom=${props.state.accessGroups.join(",")}`;
    }
    if (hasFilter && !hasCopy) {
      endpoint += `?filter=${filter}`;
    }
    Network.get(endpoint)
      .then((response) => {
        const namespacesToSet = response["namespaces"] || [];
        setNamespaces(namespacesToSet);

        const shouldApplyAGPermissions =
          hasCopy && response["toCopy"] && !props.state.permissionsModified && !agAppliedRef.current;

        if (shouldApplyAGPermissions) {
          const changes = {};

          response["toCopy"].forEach((item) => {
            changes[item.namespace] = {
              ...item,
              view: item.accessMode.includes("R"),
              modify: item.accessMode.includes("W"),
            };
          });

          props.onChange(changes);
          agAppliedRef.current = true;
        }

        setIsLoading(false);
      })
      .catch(() => {
        setIsLoading(false);
        showErrorToastr(t("An unexpected error occurred while fetching namespaces."));
      });
  };

  useEffect(() => {
    agAppliedRef.current = false;
  }, [props.state.accessGroups]);

  const debouncedGetNamespaces = useRef(
    debounce((value: string) => {
      setIsLoading(true);
      getNamespaces(value);
    }, 200)
  ).current;

  useEffect(() => {
    debouncedGetNamespaces(searchValue);
  }, [searchValue, debouncedGetNamespaces]);

  useEffect(() => {
    namespaces.forEach((item) => {
      const updateRefsRecursively = (node) => {
        const state = getCheckState(node, "modify");
        const ref = checkboxRefs.current[node.namespace];
        if (ref) {
          ref.indeterminate = state === "partially";
        }
        if (node.children) {
          node.children.forEach(updateRefsRecursively);
        }
      };
      updateRefsRecursively(item);
    });
  }, [namespaces, props.state.permissions, getCheckState]);

  const setNamespacesCheck = (model) => {
    setApiNamespace(!!model.apiNamespace);
    setWebNamespace(!!model.webNamespace);
    setShowOnlySelected(!!model.showOnlySelected);
  };

  const filteredNamespaces = (namespaces || []).filter((item) => {
    if (apiNamespace && webNamespace) {
      return true;
    }
    if (apiNamespace) {
      return item.isAPI;
    }
    if (webNamespace) {
      return !item.isAPI;
    }
    return true;
  });

  const namespacesFilter = (
    <div className="d-flex">
      <div className="ms-4">
        <Form model={{ apiNamespace, webNamespace, showOnlySelected }} onChange={setNamespacesCheck}>
          <div className="d-flex">
            <span className="control-label me-3">Filter by:</span>
            <span className="me-4">
              <DEPRECATED_Check label={t("API")} name="apiNamespace" key="apiNamespace" />
            </span>
            <span className="me-4">
              <DEPRECATED_Check label={t("Web")} name="webNamespace" key="webNamespace" />
            </span>
            <span>
              <DEPRECATED_Check label={t("Only selected")} name="showOnlySelected" key="showOnlySelected" />
            </span>
          </div>
        </Form>
      </div>
    </div>
  );

  const getSelectedNamespace = (
    items: NamespaceItem[] = [],
    selectedModes: ("view" | "modify")[] = ["view", "modify"]
  ): NamespaceItem[] => {
    return items
      .map((item) => {
        const children = item.children ? getSelectedNamespace(item.children, selectedModes) : [];

        const itemPermissions = props.state.permissions?.[item.namespace] || {};
        const isSelected = selectedModes.some((mode) => itemPermissions?.[mode]);
        const hasSelectedChildren = children.length > 0;

        if (isSelected || hasSelectedChildren) {
          return { ...item, ...(children.length ? { children } : {}) };
        }
        return null;
      })
      .filter((x): x is NamespaceItem => x !== null);
  };

  const collectExpandableKeys = useCallback((items: NamespaceItem[]) => {
    const keys = new Set<string>();

    const collectExpandableNodes = (nodes: NamespaceItem[]) => {
      nodes.forEach((item) => {
        if (item.children && item.children.length > 0) {
          keys.add(`${item.namespace}-${item.isAPI ? "api" : "ui"}`);
          collectExpandableNodes(item.children);
        }
      });
    };

    collectExpandableNodes(items);
    return keys;
  }, []);

  const handleExpandCollapseAll = () => {
    setExpandCollapseAll((prev) => {
      if (!prev) {
        const keys = collectExpandableKeys(finalData);
        setExpandedKeys(keys);
      } else {
        setExpandedKeys(new Set());
      }
      return !prev;
    });
  };

  const expandAllToggle = (
    <Button
      className="btn-default btn-sm"
      handler={handleExpandCollapseAll}
      text={!expandCollapseAll ? t("Expand All") : t("Collapse All")}
    />
  );

  const finalData = useMemo<NamespaceItem[]>(() => {
    return showOnlySelected ? getSelectedNamespace(filteredNamespaces) : filteredNamespaces || [];
  }, [showOnlySelected, filteredNamespaces]);

  //setExpandedKeys on search
  useEffect(() => {
    if (isLoading) return;

    if (!searchValue.trim()) {
      setExpandedKeys(new Set());
      return;
    }

    const keys = new Set<string>();

    const collectExpandableNodes = (items: NamespaceItem[]) => {
      items.forEach((item) => {
        keys.add(`${item.namespace}-${item.isAPI ? "api" : "ui"}`);
        if (item.children) collectExpandableNodes(item.children);
      });
    };

    collectExpandableNodes(finalData);
    if (!isLoading) {
      setExpandedKeys(keys);
    }
  }, [searchValue, isLoading]);

  return (
    <div>
      <MessagesContainer />
      {!props.state.id ? (
        <>
          <div className="row">
            <div className="col-md-6">
              <strong className="me-1">Name:</strong>
              {props.state.name}
            </div>
            <div className="col-md-6">
              <strong className="me-1">Description:</strong>
              {props.state.description}
            </div>
          </div>
          <div className="row mt-3">
            <div className="col-md-12">
              <strong>Organization:</strong>
              {props.state.orgName}
            </div>
          </div>
          <hr></hr>
        </>
      ) : null}
      <p>{t("Review and modify the permissions for this custom group as needed.")}</p>
      <Table
        data={finalData}
        onSearch={setSearchValue}
        identifier={(item) => `${item.namespace}-${item.isAPI ? "api" : "ui"}`}
        expandable
        controlledExpandedKeys={expandedKeys}
        stickyHeader
        searchPanelInline
        searchField={<SearchField placeholder={t("Filter by name")} />}
        additionalFilters={[namespacesFilter]}
        titleButtons={[expandAllToggle]}
        tableClass="table-hover"
      >
        <Column
          columnKey="name"
          header={t("Name")}
          cell={(row, criteria, nestingLevel) => {
            if (nestingLevel) {
              return row.name;
            }
            return (
              <b>
                {row.name}
                {row.isAPI ? (
                  <i className={`fa fa-plug ${styles.apiIcon}`} data-bs-toggle="tooltip" title={t("API")} />
                ) : (
                  ""
                )}
              </b>
            );
          }}
          width="30%"
        />
        <Column
          columnKey="description"
          header={t("Description")}
          cell={(row) => {
            return row.description;
          }}
          width="50%"
        />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => {
            const state = getCheckState(item, "view");
            return (
              <input
                key={item.namespace}
                name="view"
                type="checkbox"
                checked={state === "checked"}
                ref={(el) => {
                  if (el) el.indeterminate = state === "partially";
                }}
                disabled={isItemDisabled(item, "view")}
                onChange={() => handleChange(item, "view")}
              />
            );
          }}
          width="10%"
        />
        <Column
          columnKey="modify"
          header={t("Modify")}
          cell={(item) => {
            const state = getCheckState(item, "modify");
            return (
              <input
                key={item.namespace}
                name="modify"
                type="checkbox"
                checked={state === "checked"}
                ref={(el) => {
                  if (el) {
                    checkboxRefs.current[item.namespace] = el;
                    el.indeterminate = state === "partially";
                  }
                }}
                disabled={isItemDisabled(item, "modify")}
                onChange={() => handleChange(item, "modify")}
              />
            );
          }}
          width="10%"
        />
      </Table>
    </div>
  );
};

export default AccessGroupPermissions;
