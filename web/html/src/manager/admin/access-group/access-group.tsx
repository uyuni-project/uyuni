import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { StepsProgressBar } from "components/steps-progress-bar";

import AccessGroupDetails from "./access-group-details";
import AccessGroupPermissions from "./access-group-permissions";
import AccessGroupUsers from "./access-group-user";
import Network from "utils/network";
import {Messages, MessageType, Utils as MessagesUtils} from "components/messages/messages";

export type AccessGroupState = {
  name: string;
  description: string;
  accessGroups: string[];
  permissions: { id: number; namespace: string; description: string; accessMode: string; view: boolean; modify: boolean }[];
  users: { id: number; username: string; email: string; orgId: string }[];
  errors: any;
};

const CreateAccessGroup = () => {
  // TODO: Handle displaying success messages on create / update on access-group-list
  const [messages, setMessages] = useState<MessageType[]>([]);
  // const [messages, setMessages] = useState<any[]>([]);
  const [accessGroupState, setAccessGroupState] = useState<AccessGroupState>({
    name: "",
    description: "",
    accessGroups: [],
    permissions: [],
    users: [],
    errors: {},
  });

  const handleFormChange = (newAccessGroupState) => {
    setAccessGroupState(newAccessGroupState)
  };

  const handleNamespace = (newname, type) => {
    type === "view" ? newname.view = !newname.view : newname.modify = !newname.modify;
    setAccessGroupState((prevState) => {
      if (newname.view || newname.modify) {
        let add = true;
        // Modify existing item
        prevState = {
          ...prevState,
          permissions: prevState.permissions.map((p) => {
            if (p.id === newname.id) {
              add = false;
              return type === "view" ? {...p, view: newname.view} : {...p, modify: newname.modify};
            } else {
              return p;
            }
          })
        }
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
          permissions: prevState.permissions.filter((p) => p.id !== newname.id),
        };
      }
    })
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

  const handleCreateAccessGroup = () => {
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
        <AccessGroupDetails
          state={accessGroupState}
          onChange={handleFormChange}
          errors={accessGroupState.errors}
        />
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
    <TopPanel title={t("Create: Access Group")}>
      <Messages items={messages} />
      <StepsProgressBar steps={steps} onCreate={handleCreateAccessGroup} onCancel={"/rhn/manager/admin/access-group"} />
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateAccessGroup));
