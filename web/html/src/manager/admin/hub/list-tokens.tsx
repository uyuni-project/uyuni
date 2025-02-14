import * as React from "react";

import { TokenTable } from "components/hub";
import { TopPanel } from "components/panels";

type Props = {};

export class TokenList extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);
  }

  public render(): React.ReactNode {
    return (
      <TopPanel title={t("Access Tokens")} icon="fa fa-shield">
        <TokenTable />
      </TopPanel>
    );
  }
}
