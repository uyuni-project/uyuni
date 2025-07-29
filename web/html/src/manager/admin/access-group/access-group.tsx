import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import AccessGroupTabContainer from "manager/admin/access-group/access-group-tab-container";

import withPageWrapper from "components/general/with-page-wrapper";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { StepsProgressBar } from "components/steps-progress-bar";

import Network from "utils/network";

import AccessGroupDetails from "./access-group-details";
import AccessGroupPermissions from "./access-group-permissions";
import AccessGroupUsers from "./access-group-user";

export type AccessGroupState = {
  id: number | undefined;
  name: string;
  description: string;
  orgId: number | undefined;
  orgName: string;
  accessGroups: string[];
  permissions: {
    id: number;
    namespace: string;
    description: string;
    accessMode: string;
    view: boolean;
    modify: boolean;
  }[];
  users: { id: number; username: string; email: string; orgId: string }[];
  errors: any;
};

type AccessGroupProps = {
  accessGroup?: AccessGroupState;
};

const AccessGroup = (props: AccessGroupProps) => {
  // TODO: Handle displaying success messages on create / update on access-group-list
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [accessGroupState, setAccessGroupState] = useState<AccessGroupState>(
    props.accessGroup
      ? props.accessGroup
      : {
          id: undefined,
          name: "",
          description: "",
          orgId: undefined,
          orgName: "",
          accessGroups: [],
          permissions: [],
          users: [],
          errors: {},
        }
  );

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

  const handleNamespace = (newname, type) => {
    type === "view" ? (newname.view = !newname.view) : (newname.modify = !newname.modify);
    setAccessGroupState((prevState) => {
      if (newname.view || newname.modify) {
        let add = true;
        // Modify existing item
        prevState = {
          ...prevState,
          permissions: prevState.permissions.map((p) => {
            if (p.namespace === newname.namespace) {
              add = false;
              return type === "view" ? { ...p, view: newname.view } : { ...p, modify: newname.modify };
            } else {
              return p;
            }
          }),
        };
        // Add new item
        if (add) {
          return {
            ...prevState,
            permissions: [...prevState.permissions, newname],
          };
        }
        return prevState;
        // Remove existing item
      } else {
        return {
          ...prevState,
          permissions: prevState.permissions.filter((p) => p.namespace !== newname.namespace),
        };
      }
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
    Network.post("/rhn/manager/api/admin/access-group/save", accessGroupState)
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
        <AccessGroupDetails state={accessGroupState} onChange={handleFormChange} errors={accessGroupState.errors} />
      ),
      validate: null,
    },
    {
      title: "Namespaces & Permissions",
      content: (
        <AccessGroupPermissions state={accessGroupState} onChange={handleNamespace} errors={accessGroupState.errors} />
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
