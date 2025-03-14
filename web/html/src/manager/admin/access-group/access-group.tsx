import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { StepsProgressBar } from "components/steps-progress-bar";

import AccessGroupDetails from "./access-group-details";
import AccessGroupPermissions from "./access-group-permissions";
import AccessGroupUsers from "./access-group-user";

const CreateAccessGroup = () => {
  type AccessGroupState = {
    detailsproperties: {
      name: string;
      description: string;
      accessGroup: string[];
    };
    permissions: string[];
    users: { id: number; username: string; email: string; orgId: string }[];
    errors: any;
  };

  const [accessGroupState, setAccessGroupState] = useState<AccessGroupState>({
    detailsproperties: {
      name: "",
      description: "",
      accessGroup: [],
    },
    permissions: [],
    users: [],
    errors: {},
  });

  const handleFormChange = (newProperties) => {
    setAccessGroupState((prev) => ({
      ...prev,
      detailsproperties: { ...prev.detailsproperties, ...newProperties },
    }));
  };

  const handleNamespace = (newname) => { };
  console.log(accessGroupState)

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
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/access-group`);
  };

  const steps = [
    {
      title: "Details",
      content: (
        <AccessGroupDetails
          properties={accessGroupState.detailsproperties}
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
      <StepsProgressBar steps={steps} onCreate={handleCreateAccessGroup} onCancel={"/rhn/manager/admin/access-group"} />
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateAccessGroup));
