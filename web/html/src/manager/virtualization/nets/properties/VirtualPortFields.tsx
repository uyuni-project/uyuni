import * as React from "react";

import { FormContext } from "components/input/form/Form";
import { Radio } from "components/input/radio/Radio";
import { Text } from "components/input/text/Text";
import Validation from "components/validation";

import * as utils from "./utils";

type Props = {
  type: string | void | null;
};

export function VirtualPortFields(props: Props) {
  const formContext = React.useContext(FormContext);
  const model = formContext.model || {};
  return (
    <>
      {props.type === "802.1qbh" && (
        <Radio
          name="virtualport-params-type"
          inline={true}
          label={t("Parameters")}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
          defaultValue="profileid"
          items={[
            { label: t("By profile id"), value: "profileid" },
            { label: t("Virtual Station Interface (VSI) parameters"), value: "vsi" },
          ]}
        />
      )}
      {(props.type === "openvswitch" ||
        (props.type === "802.1qbh" && model["virtualport-params-type"] === "profileid")) && (
        <Text
          name="virtualport_profileid"
          label={t("Profile id")}
          required={props.type === "802.1qbh"}
          labelClass="col-md-3"
          divClass="col-md-6"
          maxLength={39}
        />
      )}
      {["openvswitch", "midonet"].includes(props.type || "") && (
        <Text
          name="virtualport_interfaceid"
          label={t("Interface id")}
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[Validation.matches(utils.uuidPattern)]}
        />
      )}
      {props.type === "802.1qbh" && model["virtualport-params-type"] === "vsi" && (
        <>
          <Text name="virtualport_managerid" label={t("VSI manager id")} labelClass="col-md-3" divClass="col-md-6" />
          <Text name="virtualport_typeid" label={t("VSI type id")} labelClass="col-md-3" divClass="col-md-6" />
          <Text
            name="virtualport_typeidversion"
            label={t("VSI type id version")}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <Text
            name="virtualport_instanceid"
            label={t("VSI instance id")}
            labelClass="col-md-3"
            divClass="col-md-6"
            validators={[Validation.matches(utils.uuidPattern)]}
            invalidHint={t("Value needs to be a valid UUID")}
          />
        </>
      )}
    </>
  );
}
