import * as React from "react";

import { SubmitButton } from "components/buttons";

import { FocusGroup } from "./focus-group";

const SEARCH_TYPES = [
  {
    value: "systems",
    name: t("Systems"),
  },
  {
    value: "packages",
    name: t("Packages"),
  },
  {
    value: "errata",
    name: t("Patches"),
  },
  {
    value: "docs",
    name: t("Documentation"),
  },
];

export class HeaderSearch extends React.PureComponent {
  private readonly initialState = {
    isOpen: false,
    searchString: "",
    searchType: SEARCH_TYPES[0].value,
  };
  state = this.initialState;

  onSPAEndNavigation() {
    this.setState(this.initialState);
  }

  onChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    this.setState({
      [event.target.name]: event.target.value,
    });
  };

  onSubmit = (event: React.SyntheticEvent) => {
    event.preventDefault();
    window.pageRenderers?.spaengine?.navigate?.(
      `/rhn/Search.do?csrf_token=${window.csrfToken}&submitted=true&search_string=${this.state.searchString}&search_type=${this.state.searchType}`
    );
    this.setState({ isOpen: false });
  };

  render() {
    return (
      <FocusGroup onFocusOut={() => this.setState({ isOpen: false })}>
        <button
          aria-label={t("Open search")}
          className={`is-plain header-non-link manual-toggle-box ${this.state.isOpen ? "open" : ""}`}
          onClick={() => this.setState({ isOpen: !this.state.isOpen })}
        >
          <i className="fa fa-search" aria-hidden="true" />
        </button>
        {this.state.isOpen ? (
          <form id="search-form" name="form1" className="box-wrapper form-inline" onSubmit={this.onSubmit}>
            <div className="form-group">
              <input
                name="searchString"
                value={this.state.searchString}
                onChange={this.onChange}
                onKeyDown={(event) => {
                  if (event.key === "Escape") {
                    this.setState({ isOpen: false });
                    if (document.activeElement instanceof HTMLElement) {
                      document.activeElement.blur();
                    }
                  }
                }}
                type="search"
                className="form-control"
                size={20}
                autoFocus
                placeholder={t("Search")}
              />
              <select name="searchType" value={this.state.searchType} onChange={this.onChange} className="form-control">
                {SEARCH_TYPES.map(({ value, name }) => (
                  <option value={value} key={value}>
                    {name}
                  </option>
                ))}
              </select>
              <SubmitButton icon="fa-search" text={t("Search")} className="btn-default-inverse" />
            </div>
          </form>
        ) : null}
      </FocusGroup>
    );
  }
}
