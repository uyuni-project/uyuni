import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useRef, useState } from "react";

import AccessGroupTabContainer from "manager/admin/access-control/access-group-tab-container";

import withPageWrapper from "components/general/with-page-wrapper";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { StepsProgressBar } from "components/steps-progress-bar";

import Network from "utils/network";

import AccessGroupDetails, { AccessGroupDetailsHandle } from "./access-group-details";
import AccessGroupPermissions from "./access-group-permissions";
import AccessGroupUsers from "./access-group-user";

type Permission = {
  id: number;
  namespace: string;
  description: string;
  accessMode: string;
  view: boolean;
  modify: boolean;
};

type User = { id: number; username: string; email: string; orgId: string };

type AccessGroupType<P> = {
  id: number | undefined;
  name: string;
  description: string;
  orgId: number | undefined;
  orgName: string;
  accessGroups: string[];
  permissions: P;
  users: User[];
  errors: any;
};

export type AccessGroupPropsType = AccessGroupType<Permission[]>;

export type AccessGroupState = AccessGroupType<Record<string, Permission>>;

type AccessGroupProps = {
  accessGroup?: AccessGroupPropsType;
};

const defaultAccessGroupState: AccessGroupState = {
  id: undefined,
  name: "",
  description: "",
  orgId: undefined,
  orgName: "",
  accessGroups: [],
  permissions: {},
  users: [],
  errors: {},
};

const parsePermissions = (accessGroupProps: AccessGroupPropsType): AccessGroupState => {
  const newPermissions: AccessGroupState["permissions"] = accessGroupProps.permissions.reduce(
    (accumulator, currentPermission) => {
      accumulator[currentPermission.namespace] = currentPermission;
      return accumulator;
    },
    {}
  );

  return {
    ...accessGroupProps,
    permissions: newPermissions,
  };
};

const AccessGroup = (props: AccessGroupProps) => {
  // TODO: Handle displaying success messages on create / update on access-group-list
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [accessGroupState, setAccessGroupState] = useState<AccessGroupState>(
    props.accessGroup ? parsePermissions(props.accessGroup) : defaultAccessGroupState
  );

  const detailsTabRef = useRef<AccessGroupDetailsHandle>(null);

  const validateDetailsTab = async () => {
    if (detailsTabRef.current) {
      return await detailsTabRef.current.validate();
    }
    return false;
  };

  const handleFormChange = (newAccessGroupState) => {
    /* TODO: using the validate prop to update the form change messes with setting the users to empty on org change
     **  that's why accessGroupState.users.length === 0 ? [] is needed here. Once onChange is used to update it it can be
     **  removed */
    setAccessGroupState((prevState) => ({
      ...prevState,
      name: newAccessGroupState.name,
      description: newAccessGroupState.description,
      orgId: newAccessGroupState.orgId,
      orgName: newAccessGroupState.orgName,
      users:
        accessGroupState.orgId !== newAccessGroupState.orgId || accessGroupState.users.length === 0
          ? []
          : newAccessGroupState.users,
      accessGroups: newAccessGroupState.accessGroups,
    }));
  };

  const handlePermissionsChange = (changes: Record<string, AccessGroupState["permissions"][0] | undefined>) => {
    setAccessGroupState((prevState) => {
      const newPermissions = { ...prevState.permissions };

      for (const namespace in changes) {
        const change = changes[namespace];
        if (change) {
          newPermissions[namespace] = change;
        } else {
          delete newPermissions[namespace];
        }
      }

      return {
        ...prevState,
        permissions: newPermissions,
      };
    });
  };

  const handleUsers = (user, action) => {
    setAccessGroupState((prevState) => {
      if (action === "add") {
        return {
          ...prevState,
          users: [...prevState.users, user], // Add user
        };
      } else if (action === "remove") {
        return {
          ...prevState,
          users: prevState.users.filter((u) => u.id !== user.id), // Remove user
        };
      }
      return prevState;
    });
  };

  const handleSaveAccessGroup = () => {
    const payload = {
      ...accessGroupState,
      permissions: Object.values(accessGroupState.permissions),
    };

    Network.post("/rhn/manager/api/admin/access-group/save", payload)
      .then((_) => {
        setMessages(MessagesUtils.info(t("Access Group successfully created.")));
        window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/access-group`);
      })
      .catch((error) => setMessages(Network.responseErrorMessage(error)));
  };

  const steps = [
    {
      title: "Details",
      content: (
        <AccessGroupDetails
          ref={detailsTabRef}
          state={accessGroupState}
          onChange={handleFormChange}
          errors={accessGroupState.errors}
        />
      ),
      validate: validateDetailsTab,
    },
    {
      title: "Namespaces & Permissions",
      content: (
        <AccessGroupPermissions
          state={accessGroupState}
          onChange={handlePermissionsChange}
          errors={accessGroupState.errors}
        />
      ),
      validate: null,
    },
    {
      title: "Users",
      content: <AccessGroupUsers state={accessGroupState} onChange={handleUsers} errors={accessGroupState.errors} />,
      validate: null,
    },
  ];
  return (
    <>
      {props.accessGroup && props.accessGroup.id ? (
        <TopPanel title={t("Access Group Details")}>
          <Messages items={messages} />
          <AccessGroupTabContainer
            tabs={steps}
            onUpdate={handleSaveAccessGroup}
            onCancel={"/rhn/manager/admin/access-group"}
          />
        </TopPanel>
      ) : (
        <TopPanel title={t("Create: Access Group")}>
          <Messages items={messages} />
          <StepsProgressBar
            steps={steps}
            onCreate={handleSaveAccessGroup}
            onCancel={"/rhn/manager/admin/access-group"}
          />
        </TopPanel>
      )}
    </>
  );
};

export default hot(withPageWrapper<AccessGroupProps>(AccessGroup));
