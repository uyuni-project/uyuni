import { useCallback, useEffect, useMemo, useState } from "react";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type Props = {
  state: any;
  onChange?: () => void;
  errors?: any;
};

const AccessGroupReview = (props: Props) => {
  const [namespaces, setNamespaces] = useState([]);
  const [expandCollapseAll, setExpandCollapseAll] = useState(true);
  const [expandedKeys, setExpandedKeys] = useState<Set<string>>(new Set());

  const isItemDisabled = useCallback((item, type) => {
    const requiredAccessMode = type === "view" ? "R" : "W";

    if (!item.children || item.children.length === 0) {
      return !item.accessMode.includes(requiredAccessMode);
    }

    return item.children.every((child) => isItemDisabled(child, type));
  }, []);

  const loadNamespaces = () => {
    let endpoint = "/rhn/manager/api/admin/access-control/access-group/list_namespaces";

    const hasCopy = props.state.accessGroups && props.state.accessGroups.length > 0;
    if (hasCopy) {
      endpoint += `?copyFrom=${props.state.accessGroups.join(",")}`;
    }

    Network.get(endpoint).then((response) => {
      const namespacesToSet = response["namespaces"] || [];
      setNamespaces(namespacesToSet);
    });
  };

  useEffect(() => {
    loadNamespaces();
  }, []);

  const getSelectedNamespace = (items, selectedModes = ["view", "modify"]) => {
    return items
      .map((item) => {
        const children = item.children ? getSelectedNamespace(item.children, selectedModes) : [];

        const itemPermissions = props.state.permissions[item.namespace];
        const isSelected = selectedModes.some((mode) => itemPermissions?.[mode]);
        const hasSelectedChildren = children.length > 0;

        if (isSelected || hasSelectedChildren) {
          return { ...item, children };
        }
        return null;
      })
      .filter(Boolean);
  };

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

  const collectExpandableKeys = useCallback((items) => {
    const keys = new Set<string>();

    const collectExpandableNodes = (nodes) => {
      nodes.forEach((item) => {
        if (item.children && item.children.length > 0) {
          keys.add(item.namespace);
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
        const keys = collectExpandableKeys(selectedPermissionsTree);
        setExpandedKeys(keys);
      } else {
        setExpandedKeys(new Set());
      }
      return !prev;
    });
  };

  const selectedPermissionsTree = useMemo(() => {
    return getSelectedNamespace(namespaces);
  }, [namespaces, props.state.permissions]);

  useEffect(() => {
    if (selectedPermissionsTree.length > 0 && expandCollapseAll) {
      setExpandedKeys(collectExpandableKeys(selectedPermissionsTree));
    }
  }, [selectedPermissionsTree, expandCollapseAll, collectExpandableKeys]);

  const expandAllToggle = (
    <Button
      className="btn-default btn-sm"
      handler={handleExpandCollapseAll}
      text={!expandCollapseAll ? t("Expand All") : t("Collapse All")}
    />
  );
  return (
    <div>
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
      <h4>Users</h4>
      <Table
        data={props.state.users}
        identifier={(item) => item.id}
        initialSortColumnKey="name"
        hideHeaderFooter="both"
        emptyText={t("No Users selected.")}
      >
        <Column columnKey="login" comparator={Utils.sortByText} header={t("Username")} cell={(item) => item.login} />
        <Column columnKey="email" comparator={Utils.sortByText} header={t("Email")} cell={(item) => item.email} />
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Real Name")} cell={(item) => item.name} />
      </Table>
      <h4>Permissions</h4>
      <Table
        data={selectedPermissionsTree}
        identifier={(item) => item.namespace}
        expandable
        emptyText={t("No permissions selected.")}
        titleButtons={[expandAllToggle]}
        controlledExpandedKeys={expandedKeys}
      >
        <Column columnKey="name" header={t("Name")} cell={(row) => row.name} />
        <Column columnKey="description" header={t("Description")} cell={(row) => row.description} />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => {
            if (item.children && item.children.length > 0) {
              return null;
            }
            if (isItemDisabled(item, "view")) {
              return <span>-</span>;
            }
            const state = getCheckState(item, "view");
            return state === "checked" ? <i className="fa fa-check"></i> : <span>X</span>;
          }}
          width="10%"
        />
        <Column
          columnKey="modify"
          header={t("Modify")}
          cell={(item) => {
            if (item.children && item.children.length > 0) {
              return null;
            }
            if (isItemDisabled(item, "modify")) {
              return <span>-</span>;
            }
            const state = getCheckState(item, "modify");
            return state === "checked" ? <i className="fa fa-check"></i> : <span>X</span>;
          }}
          width="10%"
        />
      </Table>
    </div>
  );
};

export default AccessGroupReview;
