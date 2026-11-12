/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import _isEmpty from "lodash/isEmpty";

// splits the criteria into the single terms the user typed, ignoring extra whitespace
function splitCriteria(criteria: string): string[] {
  return criteria.toLowerCase().split(/\s+/).filter(Boolean);
}

// returns true, if the text contains all the terms, in any order
function matchesAllTerms(text: string | undefined, terms: string[]) {
  return !!text && terms.every((term) => text.toLowerCase().includes(term));
}

function checkExtension(extension: any, terms: string[]) {
  const isCriteriaInLabel = matchesAllTerms(extension.label, terms);
  const isCriteriaInChannels =
    !_isEmpty(extension.channels) && extension.channels.some((c) => matchesAllTerms(c.summary, terms));
  return isCriteriaInLabel || isCriteriaInChannels;
}

// returns true, if at least one extension matches all the terms
function extensionRecursiveIterator(extension: any, terms: string[]) {
  return (
    checkExtension(extension, terms) ||
    (!_isEmpty(extension.extensions) && extension.extensions.some((ext) => extensionRecursiveIterator(ext, terms)))
  );
}

export function searchCriteriaInExtension(baseExtension: any, criteria?: string): boolean {
  const terms = criteria ? splitCriteria(criteria) : [];
  if (!_isEmpty(terms)) {
    return extensionRecursiveIterator(baseExtension, terms);
  }
  return true;
}

export function isProductRequestCancellation(error: JQueryXHR | Error | undefined): error is undefined {
  return error === undefined;
}
