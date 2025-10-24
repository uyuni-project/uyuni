import * as React from "react";

import { AddTokenButton, TokenTable } from "components/hub";
import { TopPanel } from "components/panels";

type Props = Record<never, never>;

export class TokenList extends React.Component<Props> {
  private tokenTable: React.RefObject<TokenTable>;

  public constructor(props: Props) {
    super(props);

    this.tokenTable = React.createRef();
  }

  public render(): React.ReactNode {
    return (
      <TopPanel
        title={t("Access Tokens")}
        icon="fa fa-shield"
        button={
          <div className="btn-group pull-right">
            <AddTokenButton onCreated={() => this.tokenTable.current?.refresh()} />
          </div>
        }
      >
        <TokenTable ref={this.tokenTable} allowToggleValidity={true} allowDeletion={true} />
      </TopPanel>
    );
  }
}
