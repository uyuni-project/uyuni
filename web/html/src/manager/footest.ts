import Admin from "./admin";
import Appstreams from "./appstreams";

// Define a type for the import map
const importMap = {
  ...Admin,
  ...Appstreams,
} as const;

type PageName = keyof typeof importMap;
type Page<T extends PageName> = typeof importMap[T];
type RendererParams<T extends PageName> = Parameters<Awaited<ReturnType<Page<T>>>["renderer"]>;

// Drop first item of tuple type, e.g. [foo, bar, tea] -> [bar, tea]
type Tail<T extends any[]> = T extends [any, ...infer Rest] ? Rest : never;

type PageNamePageTuple = {
  [K in PageName]: [K, Page<K>, Tail<RendererParams<K>>];
}[PageName];

const getRenderer = async <T extends PageNamePageTuple>(pageName: T[0], ...params: T[2]) => {
  const module = await importMap[pageName]();
  return module.renderer("foo", ...params);
};

getRenderer("appstreams/appstreams", {
  channelsAppStreams: "asd",
});
