import * as React from "react";
import _isNil from "lodash/isNil";
import Validation from "components/validation";
import { Panel } from "components/panels/Panel";
import { FormContext } from "components/input/Form";
import { FormMultiInput } from "components/input/FormMultiInput";
import { NetworkAddress } from "./NetworkAddress";
import { Range } from "components/input/Range";
import { Text } from "components/input/Text";
import * as utils from "./utils";

type Props = {
  /** Set to true to display the fields for IPv6 */
  ipv6?: boolean;
};

/** IP configuration input components */
export function IpConfig(props: Props) {
  const formContext = React.useContext(FormContext);
  const model = formContext.model || {};
  const ip_version = props.ipv6 ? "6" : "4";
  const address_pattern = props.ipv6 ? utils.ipv6Pattern : utils.ipv4Pattern;
  const prefix = `ipv${ip_version}def`;
  return (
    <Panel title={t(`IPv${ip_version}`)} headingLevel="h3">
      <NetworkAddress
        label={t("Network address")}
        ipv6={props.ipv6}
        prefix={prefix}
        labelClass="col-md-3"
        divClass="col-md-6"
        required
      />
      <div className="col-md-12">
        <div className="col-md-7 col-md-offset-2">
          <FormMultiInput
            id={`${prefix}-dhcp-range`}
            title={t(`DHCP${props.ipv6 ? "v6" : ""} Ranges`)}
            prefix={`${prefix}_dhcpranges`}
            onAdd={(index) => {
              formContext.setModelValue?.(`${prefix}_dhcpranges${index}_start`, "");
              formContext.setModelValue?.(`${prefix}_dhcpranges${index}_end`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`${prefix}_dhcpranges${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
          >
            {(index: number) => (
              <Range
                prefix={`${prefix}_dhcpranges${index}`}
                title={t(`DHCP${props.ipv6 ? "v6" : ""} address range ${index}`)}
                divClass="col-md-11"
                required
                validators={[
                  utils.allOrNone,
                  (value) =>
                    Object.values(value).every((item) => {
                      return typeof item === "string" && (item === "" || !_isNil(item.match(address_pattern)));
                    }),
                ]}
                invalidHint={t(`Both values need to be IPv${ip_version} addresses`)}
              />
            )}
          </FormMultiInput>
          <FormMultiInput
            id={`${prefix}-hosts`}
            title={t(`DHCP${props.ipv6 ? "v6" : ""} Hosts`)}
            prefix={`${prefix}_hosts`}
            onAdd={(index) => {
              formContext.setModelValue?.(`${prefix}_hosts${index}_ip`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`${prefix}_hosts${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
            header={
              <div className="row multi-input-table-row">
                <div className="column-title col-md-4">
                  {t("IP address")}
                  <span className="required-form-field"> *</span>
                </div>
                {!props.ipv6 && (
                  <div className="column-title col-md-4">
                    {t("MAC address")}
                    <span className="required-form-field"> *</span>
                  </div>
                )}
                {props.ipv6 && <div className="column-title col-md-4">{t("DHCP Unique Identifier (DUID)")}</div>}
                <div className="column-title col-md-4">{t("Name")}</div>
              </div>
            }
            rowClass="multi-input-table-row"
          >
            {(index) => (
              <>
                <Text
                  name={`${prefix}_hosts${index}_ip`}
                  divClass="col-md-12"
                  title={t(`DHCP${props.ipv6 ? "v6" : ""} host ${index} address`)}
                  required
                  validators={[Validation.matches(address_pattern)]}
                  invalidHint={t(`Needs to be an IPv${ip_version} address`)}
                  className="col-md-4"
                />
                {!props.ipv6 && (
                  <Text
                    name={`${prefix}_hosts${index}_mac`}
                    divClass="col-md-12"
                    title={t(`DHCP host ${index} MAC address`)}
                    maxLength={17}
                    required
                    validators={[Validation.matches(utils.macPattern)]}
                    invalidHint={t("Needs to be a MAC address")}
                    className="col-md-4"
                  />
                )}
                {props.ipv6 && (
                  <Text
                    name={`${prefix}_hosts${index}_id`}
                    divClass="col-md-12"
                    className="col-md-4"
                    title={t(`DHCPv6 host ${index} DUID`)}
                  />
                )}
                <Text
                  name={`${prefix}_hosts${index}_name`}
                  divClass="col-md-12"
                  className="col-md-4"
                  title={t(`DHCP${props.ipv6 ? "v6" : ""} host ${index} name`)}
                />
              </>
            )}
          </FormMultiInput>
        </div>
      </div>
      {!props.ipv6 && (
        <>
          <Text name={`${prefix}_bootpfile`} label={t("BOOTP image file")} labelClass="col-md-3" divClass="col-md-6" />
          <Text name={`${prefix}_bootpserver`} label={t("BOOTP server")} labelClass="col-md-3" divClass="col-md-6" />
          <Text name={`${prefix}_tftp`} label={t("TFTP root path")} labelClass="col-md-3" divClass="col-md-6" />
        </>
      )}
    </Panel>
  );
}

IpConfig.defaultProps = {
  ipv6: false,
};
