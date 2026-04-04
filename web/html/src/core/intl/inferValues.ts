/* eslint-disable @typescript-eslint/no-unused-vars */
/**
 * Infer possible placeholder and tag values for a given translatable string, e.g. for `"example {foo}"`, infer
 * `PartialRecord<"foo", any>`.
 * See https://stackoverflow.com/a/71906104/1470607
 *
 * If we ever find a case where this breaks, drop it and replace it with a simple `Record<string, any>`, however
 * currently it makes autocomplete work nicely
 */

type ValidKey<T extends string> = T extends `${infer A}${"{" | "}" | "," | " "}${infer B}` ? never : T;

type PlaceholderKeys<T extends string, Keys extends string = never> = T extends `${infer F}{${infer K}${
  | "}"
  | ","}${infer R}`
  ? PlaceholderKeys<R, K | Keys>
  : Keys;

type TagKeys<T extends string, KS extends string = never> = T extends `${infer F}</${infer K}>${infer R}`
  ? TagKeys<R, K | KS>
  : KS;
type PossibleKeysOf<T extends string> = ValidKey<PlaceholderKeys<T> | TagKeys<T>>;
type PartialRecord<K extends keyof any, T> = Partial<Record<K, T>>;
export type Values<T extends string> = PartialRecord<PossibleKeysOf<T>, any>;
