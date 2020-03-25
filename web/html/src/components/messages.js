/* eslint-disable */
import React  from "react";

type Severity = "info" | "success" | "warning" | "error";

type MessageType = {
  severity: Severity,
  text: React.Node
}

type Props = {
  /** Message objects to display */
  items: Array<MessageType>
}

/**
 * Component to render multiple alert messages.
 *
 * It takes the list of messages in the `items` array.
 * The message objects must be in the following form:
 *
 * ```
 * items = [
 *   {
 *     severity: 'error' | 'warning' | 'success' | 'info',
 *     text: "The message text to display."
 *   },
 *   ...
 * ]
 * ```
 * The `Messages` module additionally offers the `Utils` object that contains
 * helper methods to create a single message object of a specific severity:
 *
 *  - `Utils.info(msg)`
 *  - `Utils.success(msg)`
 *  - `Utils.warning(msg)`
 *  - `Utils.error(msg)`
 *
 * The return value of these methods can be directly fed into the `items` property
 * of the component:
 *
 * ```
 * <Messages items={Utils.info("My info message.")}/>
 * ```
 */
export const Messages = (props: Props) => {
    const _classNames = {
        "error": "danger",
        "success": "success",
        "info": "info",
        "warning": "warning",
    }

    var msgs = props.items.map((item, index) =>
      <div key={"msg" + index} className={'alert alert-' + _classNames[item.severity]}>
        {item.text}
      </div>
    );

    return (<div key={"messages-pop-up"}>{msgs}</div>);
}

function msg(severityIn: Severity, ...textIn: Array<React.Node>) {
    return textIn.map(function(txt) {return {severity: severityIn, text: textIn}});
}

/**
 * Helper methods to create a single message object of a specific severity
 *
 * The return value of these methods can be directly fed into the `items` property
 * of the `Messages` component.
 */
export const Utils = {
  info: function (textIn: React.Node) {
    return msg("info", textIn);
  },
  success: function (textIn: React.Node) {
    return msg("success", textIn);
  },
  warning: function (textIn: React.Node) {
    return msg("warning", textIn);
  },
  error: function (textIn: React.Node) {
    return msg("error", textIn);
  }
}
