import * as React from "react";
import { useState, useEffect } from "react";

type HashContextType = {
  hash: string | null | undefined;
  switch?: boolean;
  match?: boolean;
  goTo: (arg0: string | null | undefined) => void;
  back: () => void;
  initial: () => void;
};

export const HashRouterContext = React.createContext<HashContextType>({
  hash: null,
  goTo: hash => {},
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
    window.addEventListener("popstate", event => {
      setHash(hashUrl());
    });
  }, []);

  const goTo = (hash: string | null | undefined): void => {
    if (hash) {
      window.history.pushState(null, "", "#/" + hash);
      setHash(hash);
    } else {
      initial();
    }
  };

  const replaceWith = (hash: string): void => {
    console.log("replace " + hash);
    window.history.replaceState(null, "", "#/" + hash);
    setHash(hash);
  };

  const back = () => {
    window.history.back();
  };

  const initial = () => {
    replaceWith(initialPath);
  };

  const value = { hash, goTo, back, initial };

  return <HashRouterContext.Provider value={value}>{children}</HashRouterContext.Provider>;
};

type RouterProps = {
  path: string;
  children: React.ReactNode | ((arg0: HashContextType) => React.ReactNode | void);
};

const Route = ({ path, children }: RouterProps) => {
  return (
    <HashRouterContext.Consumer>
      {context => {
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
      {context => (
        <HashRouterContext.Provider value={{ switch: true, ...context }}>{children}</HashRouterContext.Provider>
      )}
    </HashRouterContext.Consumer>
  );
};

export { HashRouter, Route, Switch };
