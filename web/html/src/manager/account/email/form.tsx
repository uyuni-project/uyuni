import * as React from "react";

import { SubmitButton } from "components/buttons";
import { showSuccessToastr, showErrorToastr } from "components/toastr";
import Network from "utils/network";

interface Props {
  userId?: number;
  userName?: string;
  currentEmail: string;
  contextMode?: "own" | "admin";
  onSuccess?: () => void;
}

/**
 * Component for changing user email address.
 * Supports changing own email or admin changing other user's email.
 */
export const AccountEmailForm: React.FC<Props> = (props) => {
  const [email, setEmail] = React.useState(props.currentEmail);
  const [submitting, setSubmitting] = React.useState(false);

  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
  };

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedEmail = email?.trim() ?? "";

    if (trimmedEmail === "") {
      showErrorToastr(t("error.email_required"));
      return;
    }

    // Validate email format using HTML5 validation
    const emailInput = document.getElementById("emailInput") as HTMLInputElement;
    if (emailInput && !emailInput.validity.valid) {
      showErrorToastr(t("error.addr_invalid", trimmedEmail));
      return;
    }

    // Validate email is different from current
    if (trimmedEmail === props.currentEmail) {
      showErrorToastr(t("error.same_email"));
      return;
    }

    setSubmitting(true);

    Network.post(
      "/rhn/manager/api/account/changeemail",
      JSON.stringify({
        email: trimmedEmail,
        uid: props.userId,
      }),
      "application/json"
    ).then(
      (data: any) => {
        setSubmitting(false);
        if (data.success) {
          showSuccessToastr(data.data || t("email.verified"));
          setTimeout(() => {
            location.reload();
          }, 2000);
        } else {
          showErrorToastr(data.messages?.[0] || data.data || t("error.unknown"));
        }
      },
      (xhr: any) => {
        setSubmitting(false);
        try {
          const parsed = JSON.parse(xhr.responseText);
          showErrorToastr(parsed.messages?.[0] || parsed.data || t("error.unknown"));
        } catch {
          showErrorToastr(Network.errorMessageByStatus(xhr.status)?.[0] || t("error.unknown"));
        }
      }
    );
  };

  return (
    <>
      {props.contextMode === "admin" && props.userName && (
        <h2>
          <i className="fa fa-pencil" />
          {" " + t("Change Email Address")}
        </h2>
      )}

      <div className="panel panel-default">
        <div className="panel-heading">
          <h4>{t("yourchangeemail.instructions")}</h4>
        </div>
        <div className="panel-body">
          <form className="form-horizontal" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="col-lg-3 control-label" htmlFor="emailInput">
                {t("channel.edit.jsp.emailaddress")}:
              </label>
              <div className="col-lg-6">
                <input
                  id="emailInput"
                  type="email"
                  name="email"
                  className="form-control"
                  value={email}
                  onChange={handleEmailChange}
                  maxLength={256}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <div className="col-lg-offset-3 col-lg-6">
                <SubmitButton
                  id="submitBtn"
                  className="btn-success"
                  text={t("message.Update")}
                  disabled={submitting}
                />
              </div>
            </div>
          </form>
        </div>
      </div>
    </>
  );
};





