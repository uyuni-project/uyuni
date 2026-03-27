import * as React from "react";
import { AsyncButton } from "../buttons";
import { Messages } from "../messages";
import { Network } from "../utils/network";

interface Props {
  userId?: number;
  currentEmail: string;
  onSuccess?: () => void;
}

interface State {
  email: string;
  messages: any[];
  loading: boolean;
}

/**
 * Component for changing user email address
 * Supports changing own email or admin changing other user's email
 */
export const AccountEmailForm: React.FC<Props> = (props) => {
  const [email, setEmail] = React.useState(props.currentEmail);
  const [messages, setMessages] = React.useState<any[]>([]);
  const [loading, setLoading] = React.useState(false);

  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
    // Clear messages when user starts typing
    setMessages([]);
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessages([]);

    // Validate email is not empty
    if (!email || email.trim() === "") {
      setMessages([
        {
          severity: "error",
          text: t("error.email_required"),
        },
      ]);
      return;
    }

    // Validate email format using HTML5 validation
    const emailInput = document.getElementById("emailInput") as HTMLInputElement;
    if (!emailInput.validity.valid) {
      setMessages([
        {
          severity: "error",
          text: t("error.addr_invalid", email),
        },
      ]);
      return;
    }

    // Validate email is different from current
    if (email === props.currentEmail) {
      setMessages([
        {
          severity: "error",
          text: t("error.same_email"),
        },
      ]);
      return;
    }

    setLoading(true);

    try {
      const response = await Network.post(
        "/rhn/account/changeemail",
        JSON.stringify({
          email: email.trim(),
          uid: props.userId,
        }),
        "application/json"
      );

      if (response.success) {
        setMessages([
          {
            severity: "success",
            text: response.data || t("email.verified"),
          },
        ]);
        // Auto-reload after delay
        setTimeout(() => {
          location.reload();
        }, 2000);
      } else {
        setMessages([
          {
            severity: "error",
            text: response.data || t("error.unknown"),
          },
        ]);
      }
    } catch (error: any) {
      setMessages([
        {
          severity: "error",
          text: `Network error: ${error.message}`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form id="emailChangeForm" className="form-horizontal" onSubmit={handleSubmit}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">{t("yourchangeemail.instructions")}</h3>
        </div>
        <div className="panel-body">
          {messages.length > 0 && <Messages items={messages} />}

          <div className="form-group">
            <label className="col-sm-3 control-label">
              {t("channel.edit.jsp.emailaddress")}:
            </label>
            <div className="col-sm-6">
              <input
                id="emailInput"
                type="email"
                name="email"
                className="form-control"
                value={email}
                onChange={handleEmailChange}
                maxLength={256}
                required
                placeholder="user@example.com"
              />
            </div>
          </div>

          <div className="form-group">
            <div className="col-sm-offset-3 offset-sm-3 col-sm-6">
              <AsyncButton
                id="submitBtn"
                className="btn btn-primary"
                text={t("message.Update")}
                title={t("message.Update")}
                disabled={loading}
                handler={() =>
                  new Promise((resolve) => {
                    const form = document.getElementById(
                      "emailChangeForm"
                    ) as HTMLFormElement;
                    form.dispatchEvent(
                      new Event("submit", { cancelable: true, bubbles: true })
                    );
                    resolve(null);
                  })
                }
              />
            </div>
          </div>
        </div>
      </div>
    </form>
  );
};

