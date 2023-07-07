/**
 * Infer possible placeholder and tag values for a given translatable string
 * See https://stackoverflow.com/a/71906104/1470607
 *
 * If we ever find a case where this breaks, drop it and replace it with a simple `Record<string, any>`, however
 * currently it makes autocomplete work nicely
 */

type PlaceholderKeys<T extends string, KS extends string = never> = T extends `${infer F}{${infer K}}${infer R}`
  ? PlaceholderKeys<R, K | KS>
  : KS;
type TagKeys<T extends string, KS extends string = never> = T extends `${infer F}</${infer K}>${infer R}`
  ? TagKeys<R, K | KS>
  : KS;
type PossibleKeysOf<T extends string> = PlaceholderKeys<T> | TagKeys<T>;
type PartialRecord<K extends keyof any, T> = {
  [P in K]?: T;
};
export type Values<T extends string> = PartialRecord<PossibleKeysOf<T>, any>;
