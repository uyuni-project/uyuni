import { type ReactNode, Component } from "react";
export type Severity = "info" | "success" | "warning" | "error";

export type ServerMessageType = {
  severity: Severity;
  text: string;
  args: string[];
};

export type MessageType = {
  severity: Severity;
  text: ReactNode;
};

type Props = {
  /** Message objects to display */
  items: MessageType[] | MessageType;
  autoScroll?: boolean;
};

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
const _classNames = {
  error: "danger",
  success: "success",
  info: "info",
  warning: "warning",
};

export class Messages extends Component<Props> {
  static info(text: ReactNode): MessageType {
    return Messages.message("info", text);
  }

  static success(text: ReactNode): MessageType {
    return Messages.message("success", text);
  }

  static error(text: ReactNode): MessageType {
    return Messages.message("error", text);
  }

  static warning(text: ReactNode): MessageType {
    return Messages.message("warning", text);
  }

  static message(severityIn: Severity, textIn: ReactNode): MessageType {
    return { severity: severityIn, text: textIn };
  }

  render() {
    const items: MessageType[] = Array.isArray(this.props.items) ? this.props.items : [this.props.items];
    if (items.length === 0) return null;

    const msgs = items.map((item, index) => (
      <div key={"msg" + index} className={"alert alert-" + _classNames[item.severity]}>
        {item.text}
      </div>
    ));

    return (
      <div className="alert-container" key={"messages-pop-up"}>
        {msgs}
      </div>
    );
  }
}

export const fromServerMessage = (
  message: ServerMessageType,
  messageMap?: Record<string, ReactNode>
): MessageType | null | undefined => {
  let messageText: ReactNode = message.text;
  if (messageMap && message.text in messageMap) {
    const mappedMessage = messageMap[message.text];
    if (typeof mappedMessage === "function") {
      messageText = mappedMessage(message.args);
    } else {
      messageText = mappedMessage;
    }
  }
  let msg: MessageType | null | undefined;
  switch (message.severity) {
    case "info":
      msg = Messages.info(messageText);
      break;
    case "success":
      msg = Messages.success(messageText);
      break;
    case "warning":
      msg = Messages.warning(messageText);
      break;
    case "error":
      msg = Messages.error(messageText);
      break;
  }
  return msg;
};

function msg(severityIn: Severity, textIn: ReactNode, listMultiple: boolean, header?: string) {
  if (textIn === null || textIn === undefined) {
    return [];
  }

  if (Array.isArray(textIn)) {
    if (listMultiple && textIn.length > 1) {
      return [
        {
          severity: severityIn,
          text: (
            <>
              {header && <p>{header}</p>}
              <ul>
                {textIn.map((msg) => (
                  <li key={msg}>{msg}</li>
                ))}
              </ul>
            </>
          ),
        },
      ];
    }

    return textIn.map((txt) => ({ severity: severityIn, text: txt }));
  }

  return [{ severity: severityIn, text: textIn }];
}

/**
 * Helper methods to create a single/multiple message object of a specific severity
 *
 * The return value of these methods can be directly fed into the `items` property
 * of the `Messages` component.
 *
 * When the optional parameter `listMultipleMessages` is set to true, multiple entries
 * will be rendered in the same message using a list. If specified, the `headerMessage`
 * will be displayed before the list.
 *
 * Otherwise if the parameter `listMultipleMessages` is omitted or set to false, multiple
 * entries will generate multiple separated messages.
 */
export const Utils = {
  info: function (text: ReactNode, listMultiple: boolean = false, header?: string): MessageType[] {
    return msg("info", text, listMultiple, header);
  },
  success: function (text: ReactNode, listMultiple: boolean = false, header?: string): MessageType[] {
    return msg("success", text, listMultiple, header);
  },
  warning: function (text: ReactNode, listMultiple: boolean = false, header?: string): MessageType[] {
    return msg("warning", text, listMultiple, header);
  },
  error: function (text: ReactNode, listMultiple: boolean = false, header?: string): MessageType[] {
    return msg("error", text, listMultiple, header);
  },
};
