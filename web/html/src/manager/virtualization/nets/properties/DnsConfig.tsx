import * as React from "react";
import Validation from "components/validation";
import { Panel } from "components/panels/Panel";
import { FormContext } from "components/input/Form";
import { FormMultiInput } from "components/input/FormMultiInput";
import { Text } from "components/input/Text";
import { Select } from "components/input/Select";
import * as utils from "./utils";

type Props = {};

/** DNS configuration input component */
export function DnsConfig(props: Props) {
  const formContext = React.useContext(FormContext);
  const model = formContext.model || {};
  return (
    <Panel title={t("DNS")} headingLevel="h3">
      <div className="col-md-12">
        <div className="col-md-7 col-md-offset-2">
          <FormMultiInput
            id={"dns_forwarders"}
            title={t("Forwarders")}
            prefix={"dns_forwarders"}
            onAdd={(index) => {
              formContext.setModelValue?.(`dns_forwarders${index}_domain`, "");
              formContext.setModelValue?.(`dns_forwarders${index}_address`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`dns_forwarders${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
            header={
              <div className="row multi-input-table-row">
                <div className="column-title col-md-6">{t("Domain")}</div>
                <div className="column-title col-md-6">{t("DNS server address")}</div>
              </div>
            }
            rowClass="multi-input-table-row"
          >
            {(index: number) => (
              <>
                <Text
                  name={`dns_forwarders${index}_domain`}
                  divClass="col-md-12"
                  className="col-md-6"
                  title={t(`DNS forwarder ${index} domain name`)}
                  validators={[Validation.matches(utils.dnsNamePattern)]}
                  invalidHint={t("Has to be a valid qualified host name")}
                />
                <Text
                  name={`dns_forwarders${index}_address`}
                  divClass="col-md-12"
                  className="col-md-6"
                  title={t(`DNS forwarder ${index} address`)}
                  validators={[Validation.matches(utils.ipPattern)]}
                  invalidHint={t("Has to be an IP address")}
                />
              </>
            )}
          </FormMultiInput>
          <FormMultiInput
            id={"dns_hosts"}
            title={t("Hosts")}
            prefix={"dns_hosts"}
            onAdd={(index) => {
              formContext.setModelValue?.(`dns_hosts${index}_address`, "");
              formContext.setModelValue?.(`dns_hosts${index}_names`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`dns_hosts${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
            header={
              <div className="row multi-input-table-row">
                <div className="column-title col-md-6">
                  {t("Address")}
                  <span className="required-form-field"> *</span>
                </div>
                <div className="column-title col-md-6">
                  {t("hostnames (comma-separated)")}
                  <span className="required-form-field"> *</span>
                </div>
              </div>
            }
            rowClass="multi-input-table-row"
          >
            {(index: number) => (
              <>
                <Text
                  name={`dns_hosts${index}_address`}
                  divClass="col-md-12"
                  className="col-md-6"
                  title={t(`DNS host ${index} address`)}
                  required
                  validators={[Validation.matches(utils.ipPattern)]}
                  invalidHint={t("Has to be an IP address")}
                />
                <Text
                  name={`dns_hosts${index}_names`}
                  divClass="col-md-12"
                  className="col-md-6"
                  title={t(`DNS host ${index} names`)}
                  required
                  validators={[
                    (value) =>
                      value.split(",").every((item) => item === "" || item.match(utils.dnsNamePattern) != null),
                  ]}
                  invalidHint={t("Has to be a comma-separated list of host names")}
                />
              </>
            )}
          </FormMultiInput>
          <FormMultiInput
            id={"dns_srvs"}
            title={t("SRV records")}
            prefix={"dns_srvs"}
            onAdd={(index) => {
              formContext.setModelValue?.(`dns_srvs${index}_name`, "");
              formContext.setModelValue?.(`dns_srvs${index}_protocol`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`dns_srvs${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
            panelTitle={(index) => {
              const name = model[`dns_srvs${index}_name`];
              const protocol = (model[`dns_srvs${index}_protocol`] || "").toUpperCase();
              return `${name} - ${protocol}`;
            }}
          >
            {(index: number) => (
              <>
                <Text
                  label={t("Service name")}
                  title={t(`DNS SRV record ${index} service`)}
                  name={`dns_srvs${index}_name`}
                  required
                  labelClass="col-md-3"
                  divClass="col-md-6"
                />
                <Select
                  label={t("Protocol")}
                  title={t(`DNS SRV record ${index} protocol`)}
                  name={`dns_srvs${index}_protocol`}
                  required
                  labelClass="col-md-3"
                  divClass="col-md-6"
                  options={[
                    { value: "tcp", label: "TCP" },
                    { value: "udp", label: "UDP" },
                  ]}
                />
                <Text
                  label={t("Service domain name")}
                  title={t(`DNS SRV record ${index} domain name`)}
                  name={`dns_srvs${index}_domain`}
                  labelClass="col-md-3"
                  divClass="col-md-6"
                  validators={[Validation.matches(utils.dnsNamePattern)]}
                  invalidHint={t("Has to be a valid qualified host name")}
                />
                <Text
                  label={t("Target hostname")}
                  title={t(`DNS SRV record ${index} target hostname`)}
                  name={`dns_srvs${index}_target`}
                  labelClass="col-md-3"
                  divClass="col-md-6"
                />
                <Text
                  label={t("Port")}
                  title={t(`DNS SRV record ${index} port`)}
                  name={`dns_srvs${index}_port`}
                  validators={[Validation.isInt({ min: 0 })]}
                  invalidHint={t("The value has to be a positive integer")}
                  labelClass="col-md-3"
                  divClass="col-md-6"
                />
                <Text
                  label={t("Target priority")}
                  title={t(`DNS SRV record ${index} priority`)}
                  name={`dns_srvs${index}_priority`}
                  validators={[Validation.isInt({ min: 0 })]}
                  invalidHint={t("The value has to be a positive integer")}
                  labelClass="col-md-3"
                  divClass="col-md-6"
                />
                <Text
                  label={t("Target weight")}
                  title={t(`DNS SRV record ${index} weight`)}
                  name={`dns_srvs${index}_weight`}
                  validators={[Validation.isInt({ min: 0 })]}
                  invalidHint={t("The value has to be a positive integer")}
                  labelClass="col-md-3"
                  divClass="col-md-6"
                />
              </>
            )}
          </FormMultiInput>
          <FormMultiInput
            id={"dns_txts"}
            title={t("TXT records")}
            prefix={"dns_txts"}
            onAdd={(index) => {
              formContext.setModelValue?.(`dns_txts${index}_name`, "");
              formContext.setModelValue?.(`dns_txts${index}_value`, "");
            }}
            onRemove={(index) => {
              Object.keys(model)
                .filter((key) => key.startsWith(`dns_txts${index}_`))
                .forEach((key) => formContext.setModelValue?.(key, undefined));
            }}
            header={
              <div className="row multi-input-table-row">
                <div className="column-title col-md-6">
                  {t("Name")}
                  <span className="required-form-field"> *</span>
                </div>
                <div className="column-title col-md-6">
                  {t("Value")}
                  <span className="required-form-field"> *</span>
                </div>
              </div>
            }
            rowClass="multi-input-table-row"
          >
            {(index: number) => (
              <>
                <Text
                  name={`dns_txts${index}_name`}
                  title={t(`DNS TXT record ${index} name`)}
                  divClass="col-md-12"
                  className="col-md-6"
                  required
                  validators={[Validation.matches(utils.dnsNamePattern)]}
                  invalidHint={t("Has to be a valid qualified host name")}
                />
                <Text
                  name={`dns_txts${index}_value`}
                  title={t(`DNS TXT record ${index} value`)}
                  required
                  divClass="col-md-12"
                  className="col-md-6"
                />
              </>
            )}
          </FormMultiInput>
        </div>
      </div>
    </Panel>
  );
}
