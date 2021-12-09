import * as React from "react";
import { useState, useEffect } from "react";

declare var history: any;

type HashContextType = {
  hash?: string | null;
  switch?: boolean;
  match?: boolean;
  goTo: (arg0?: string | null) => void;
  back: () => void;
  initial: () => void;
};

export const HashRouterContext = React.createContext<HashContextType>({
  hash: null,
  goTo: (hash) => {},
  back: () => {},
  initial: () => {},
});

const hashUrlRegex = /^#\/(.*)$/;

function hashUrl() {
  const match = window.location.hash.match(hashUrlRegex);
  return match ? match[1] : undefined;
}

type HashRouterProps = {
  initialPath: string;
  children: React.ReactNode;
};

const HashRouter = ({ initialPath, children }: HashRouterProps) => {
  const [hash, setHash] = useState<string | undefined>(initialPath);

  useEffect(() => {
    const hash = hashUrl();
    if (hash) {
      setHash(hash);
    } else {
      initial();
    }
    window.addEventListener("popstate", (event) => {
      setHash(hashUrl());
    });
  }, []);

  const goTo = (hash?: string | null): void => {
    if (hash) {
      history.pushState(null, "", "#/" + hash);
      setHash(hash);
    } else {
      initial();
    }
  };

  const replaceWith = (hash: string): void => {
    console.log("replace " + hash);
    history.replaceState(null, "", "#/" + hash);
    setHash(hash);
  };

  const back = () => {
    history.back();
  };

  const initial = () => {
    replaceWith(initialPath);
  };

  return (
    <HashRouterContext.Provider value={{ hash: hash, goTo: goTo, back: back, initial: initial }}>
      {children}
    </HashRouterContext.Provider>
  );
};

type RouterProps = {
  path: string;
  children: React.ReactNode | ((arg0: HashContextType) => React.ReactNode | void);
};

const Route = ({ path, children }: RouterProps) => {
  return (
    <HashRouterContext.Consumer>
      {(context) => {
        const match = path === context.hash;
        if (context.switch) {
          if (match) {
            if (typeof children === "function") {
              return children({ match: true, ...context });
            } else {
              return children;
            }
          } else {
            return null;
          }
        } else {
          if (typeof children === "function") {
            return children({ match: match, ...context });
          } else {
            return children;
          }
        }
      }}
    </HashRouterContext.Consumer>
  );
};

type SwitchProps = {
  children: React.ReactNode;
};

const Switch = ({ children }: SwitchProps) => {
  return (
    <HashRouterContext.Consumer>
      {(context) => (
        <HashRouterContext.Provider value={{ switch: true, ...context }}>{children}</HashRouterContext.Provider>
      )}
    </HashRouterContext.Consumer>
  );
};

export { HashRouter, Route, Switch };
