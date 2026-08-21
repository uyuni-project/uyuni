import { useState } from "react";

import { DEPRECATED_Check, DEPRECATED_Select, Form, Password, Text, TextArea } from "components/input";
import { Panel } from "components/panels/Panel";

import {
  LdapServerProperties,
  OrgOption,
  PROVISIONING_MODE_OPTIONS,
  SERVER_TYPE_ATTR_FIELDS,
  SERVER_TYPE_DEFAULTS,
  SERVER_TYPE_OPTIONS,
  TRANSPORT_DEFAULT_PORTS,
  TRANSPORT_OPTIONS,
} from "./ldap-types";

type Props = {
  model: LdapServerProperties;
  errors?: any;
  orgs: OrgOption[];
  editing?: boolean;
  onChange: (model: LdapServerProperties) => void;
  children?: React.ReactNode;
};

const TAB_HASHES = ["#server", "#account", "#attributes"] as const;
const TAB_LABELS = ["Server", "Account", "Attribute mappings"] as const;

const applyServerTypeDefaults = (previous: LdapServerProperties, next: LdapServerProperties): LdapServerProperties => {
  if (previous.serverType === next.serverType) {
    return next;
  }
  const prevDefaults = SERVER_TYPE_DEFAULTS[previous.serverType];
  const nextDefaults = SERVER_TYPE_DEFAULTS[next.serverType];
  if (!nextDefaults) {
    return next;
  }

  const updated = { ...next };
  for (const field of SERVER_TYPE_ATTR_FIELDS) {
    const current = next[field];
    const isEmpty = current === "" || current === null || current === undefined;
    const matchesPrevDefault = prevDefaults !== undefined && current === prevDefaults[field];
    if (isEmpty || matchesPrevDefault) {
      updated[field] = nextDefaults[field];
    }
  }
  return updated;
};

const applyTransportPortDefault = (
  previous: LdapServerProperties,
  next: LdapServerProperties
): LdapServerProperties => {
  if (previous.transport === next.transport) {
    return next;
  }
  const prevDefaultPort = TRANSPORT_DEFAULT_PORTS[previous.transport];
  const nextDefaultPort = TRANSPORT_DEFAULT_PORTS[next.transport];
  if (nextDefaultPort === undefined) {
    return next;
  }
  const portMatchesPrevDefault =
    next.port === "" ||
    next.port === null ||
    next.port === undefined ||
    (prevDefaultPort !== undefined && Number(next.port) === prevDefaultPort);
  if (portMatchesPrevDefault) {
    return { ...next, port: nextDefaultPort };
  }
  return next;
};

const LdapForm = (props: Props) => {
  const [activeHash, setActiveHash] = useState<(typeof TAB_HASHES)[number]>(TAB_HASHES[0]);
  const orgOptions = props.orgs.map((org) => ({ value: org.id, label: org.name }));

  const serverTab = (
    <Panel headingLevel="h2" title={t("Server")}>
      <Text required name="label" label={t("Label")} labelClass="col-md-3" divClass="col-md-6" />
      <DEPRECATED_Check name="enabled" label={t("Enabled")} divClass="col-md-6 col-md-offset-3 offset-md-3" />
      <Text name="priority" label={t("Priority")} labelClass="col-md-3" divClass="col-md-6" type="number" />
      <DEPRECATED_Select
        name="serverType"
        label={t("Server type")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required
        options={SERVER_TYPE_OPTIONS}
      />
      <Text required name="host" label={t("Host")} labelClass="col-md-3" divClass="col-md-6" />
      <Text name="port" label={t("Port")} labelClass="col-md-3" divClass="col-md-6" type="number" />
      <DEPRECATED_Select
        name="transport"
        label={t("Transport")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required
        options={TRANSPORT_OPTIONS}
      />
      <Text
        name="connectTimeout"
        label={t("Connect timeout (ms)")}
        labelClass="col-md-3"
        divClass="col-md-6"
        type="number"
      />
      <Text
        name="responseTimeout"
        label={t("Response timeout (ms)")}
        labelClass="col-md-3"
        divClass="col-md-6"
        type="number"
      />
      <TextArea
        name="rootCa"
        label={t("Root CA (PEM)")}
        labelClass="col-md-3"
        divClass="col-md-6"
        rows={6}
        placeholder={"-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"}
        hint={t("Optional PEM-encoded certificate used to trust the directory TLS endpoint.")}
      />
    </Panel>
  );

  const accountTab = (
    <Panel headingLevel="h2" title={t("Account")}>
      <Text
        name="bindDn"
        label={t("Bind DN")}
        labelClass="col-md-3"
        divClass="col-md-6"
        hint={t("Leave empty for anonymous bind.")}
      />
      <Password
        name="bindPassword"
        label={t("Bind password")}
        labelClass="col-md-3"
        divClass="col-md-6"
        hint={
          props.editing && props.model.hasBindPassword
            ? t("Leave empty to keep the existing password.")
            : t("Required when a bind DN is set.")
        }
      />
      <Text required name="userBaseDn" label={t("User base DN")} labelClass="col-md-3" divClass="col-md-6" />
      <Text name="groupBaseDn" label={t("Group base DN")} labelClass="col-md-3" divClass="col-md-6" />
      <Text
        name="userFilter"
        label={t("User filter")}
        labelClass="col-md-3"
        divClass="col-md-6"
        hint={t("Optional override. Use {login} as the placeholder.")}
      />
      <DEPRECATED_Select
        name="provisioningMode"
        label={t("Provisioning mode")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required
        options={PROVISIONING_MODE_OPTIONS}
      />
      <DEPRECATED_Select
        name="defaultOrgId"
        label={t("Default organization")}
        labelClass="col-md-3"
        divClass="col-md-6"
        isClearable
        options={orgOptions}
        hint={t("Required for just-in-time provisioning.")}
      />
      <DEPRECATED_Check
        name="autoJoinRegularUser"
        label={t("Auto-join regular user group")}
        divClass="col-md-6 col-md-offset-3 offset-md-3"
      />
    </Panel>
  );

  const attributesTab = (
    <Panel headingLevel="h2" title={t("Attribute mappings")}>
      <Text name="loginAttribute" label={t("Login attribute")} labelClass="col-md-3" divClass="col-md-6" />
      <Text name="firstNameAttribute" label={t("First name attribute")} labelClass="col-md-3" divClass="col-md-6" />
      <Text name="lastNameAttribute" label={t("Last name attribute")} labelClass="col-md-3" divClass="col-md-6" />
      <Text name="emailAttribute" label={t("Email attribute")} labelClass="col-md-3" divClass="col-md-6" />
      <Text
        name="groupFilter"
        label={t("Group filter")}
        labelClass="col-md-3"
        divClass="col-md-6"
        hint={t("Optional override. Use {userDn} as the placeholder. Ignored when memberOf is enabled.")}
      />
      <Text name="groupNameAttribute" label={t("Group name attribute")} labelClass="col-md-3" divClass="col-md-6" />
      <DEPRECATED_Check
        name="useMemberOf"
        label={t("Use memberOf attribute")}
        divClass="col-md-6 col-md-offset-3 offset-md-3"
        hint={t(
          "Read group membership from the user entry (including Active Directory memberOf;range= pages) instead of searching groups by member=."
        )}
      />
    </Panel>
  );

  // Keep every tab mounted (hide inactive with CSS). TabContainer only renders the
  // active panel, which unmounts inputs and InputBase clears their model values.
  const tabs = [serverTab, accountTab, attributesTab];

  return (
    <Form
      model={props.model}
      errors={props.errors}
      onChange={(newModel) => {
        let normalized: LdapServerProperties = {
          ...newModel,
          defaultOrgId:
            newModel.defaultOrgId === "" || newModel.defaultOrgId === undefined ? null : newModel.defaultOrgId,
          useMemberOf: Boolean(newModel.useMemberOf),
        };
        normalized = applyServerTypeDefaults(props.model, normalized);
        normalized = applyTransportPortDefault(props.model, normalized);
        props.onChange(normalized);
      }}
    >
      <div>
        <div className="spacewalk-content-nav mb-5">
          <ul className="nav nav-tabs">
            {TAB_HASHES.map((hash, i) => (
              <li className={activeHash === hash ? "active" : ""} key={hash}>
                <a
                  className="js-spa"
                  href={hash}
                  onClick={(event) => {
                    event.preventDefault();
                    setActiveHash(hash);
                  }}
                >
                  {t(TAB_LABELS[i])}
                </a>
              </li>
            ))}
          </ul>
        </div>
        {tabs.map((tab, i) => (
          <div key={TAB_HASHES[i]} hidden={activeHash !== TAB_HASHES[i]}>
            {tab}
          </div>
        ))}
      </div>
      {props.children}
    </Form>
  );
};

export default LdapForm;
