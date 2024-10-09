import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import PasswordPolicy from "./password-policy";

type PasswordPolicyRendererProps = {
  minLength: bigint,
  maxLength: bigint,
  digitsFlag: boolean,
  lowerCharFlag: boolean,
  upperCharFlag: boolean,
  consecutiveCharFlag: boolean,
  specialCharFlag: boolean,
  specialCharList: string | null,
  restrictedOccurrenceFlag: boolean,
  maxCharOccurrence: bigint
};

export const renderer = (
  id: string, props: PasswordPolicyRendererProps
) => {
  SpaRenderer.renderNavigationReact(
    <PasswordPolicy 
      minLength={props.minLength}
      maxLength= {props.maxLength}
      digitsFlag= {props.digitsFlag}
      lowerCharFlag= {props.lowerCharFlag}
      upperCharFlag= {props.upperCharFlag}
      consecutiveCharFlag= {props.consecutiveCharFlag}
      specialCharFlag= {props.specialCharFlag}
      specialCharList= {props.specialCharList}
      restrictedOccurrenceFlag= {props.restrictedOccurrenceFlag}
      maxCharOccurrence= {props.maxCharOccurrence}
    />,
    document.getElementById(id)
  );
};
