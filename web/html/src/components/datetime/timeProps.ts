import type { HTMLProps } from "react";

export const getTimeProps = (value: moment.Moment): Partial<HTMLProps<HTMLTimeElement>> => {
  /**
   * NB! This needs to be in sync with java/core/src/main/java/com/redhat/rhn/frontend/taglibs/FormatDateTag.java
   * Convert the value to an ISO 8601, keeping the timezone offset.
   * This value is used by Cucumber to verify the results of different operations.
   */
  return {
    dateTime: value.toISOString(true),
    title: value.toUserString(),
  };
};
