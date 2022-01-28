/**
 * NB! Workers must import polyfills
 * If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
 */
import "manager/polyfills.ts";

import WorkerMessages from "./channels-selection-messages";
import { rawChannelsToDerivedChannels } from "./channels-selection-transforms";
import ChannelProcessor, { WorkerPayload } from "./channels-processor";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

const state = new ChannelProcessor();

let bufferIdentifier = 0;

// React to messages from parent thread
context.addEventListener("message", async ({ data }) => {
  let payload: WorkerPayload = {};

  switch (data.type) {
    // The worker can't currently integrate with our network layer due to its reliance on jQuery, in the future the worker could do the request itself
    case WorkerMessages.SET_CHANNELS: {
      if (!Array.isArray(data.channels) || !data.mandatoryChannelsMap) {
        throw new TypeError("Insufficient channel data");
      }
      const selectedBaseChannelId: number | undefined = data.initialSelectedBaseChannelId;
      const { baseChannels, channelsMap, requiresMap, requiredByMap } = rawChannelsToDerivedChannels(
        data.channels,
        data.mandatoryChannelsMap
      );
      payload = {
        baseChannels,
        channelsMap,
        requiresMap,
        requiredByMap,
        selectedBaseChannelId,
      };
      break;
    }
    case WorkerMessages.SET_SEARCH: {
      const search = data.search;
      if (typeof search !== "string") {
        throw new TypeError("No search string");
      }
      payload = { search };
      break;
    }
    case WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID: {
      const selectedBaseChannelId = data.selectedBaseChannelId;
      if (typeof selectedBaseChannelId !== "number" || isNaN(selectedBaseChannelId)) {
        throw new TypeError("No base channel id");
      }
      payload = { selectedBaseChannelId };
      break;
    }
    case WorkerMessages.SET_ACTIVE_FILTERS: {
      const activeFilters = data.activeFilters;
      if (!Array.isArray(activeFilters)) {
        throw new TypeError("No valid active filters");
      }
      payload = { activeFilters };
      break;
    }
    default:
      throw new RangeError(`Unknown message type, got ${data.type}`);
  }

  const result = state.produceViewFrom(payload);
  if (!result) {
    // If we're still missing some data to produce a result, do nothing
    return;
  }

  // Apply filters, search etc and convert whatever we have remaining into row definitions and related info and pass to the main thread
  const { rows } = result;

  // Send rows in batches to give the UI thread a chance to do other work inbetween
  const bufferSize = 1000;
  for (let ii = 0; ii <= Math.floor(rows.length / bufferSize); ii++) {
    const identifier = bufferIdentifier;

    // eslint-disable-next-line no-restricted-globals
    self.setTimeout(() => {
      const start = ii * bufferSize;
      context.postMessage({
        type: WorkerMessages.ROWS_AVAILABLE,
        rows: rows.slice(start, start + bufferSize),
        rowCount: rows.length,
        bufferIdentifier: identifier,
      });
    });
  }
  bufferIdentifier += 1;
});
