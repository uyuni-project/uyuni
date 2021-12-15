/**
 * NB! Workers must import polyfills
 * If we want to use more workers in the future, using a dedicated library such as Comlink or something similar might make sense
 */
import "manager/polyfills.ts";

import WorkerMessages from "./channels-selection-messages";
import { isBaseChannel } from "core/channels/type/channels.type";
import { rawChannelsToDerivedChannels } from "./channels-selection-transforms";
import ChannelProcessor, { WorkerPayload } from "./channels-processor";

// eslint-disable-next-line no-restricted-globals
const context: Worker = self as any;

const state = new ChannelProcessor();

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
      const select: number[] | undefined = data.initialSelectedChannelIds;
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
        select,
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
      payload = { selectedBaseChannelId, select: [selectedBaseChannelId], open: [selectedBaseChannelId] };
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
    case WorkerMessages.TOGGLE_IS_CHANNEL_OPEN: {
      const channelId = data.channelId;
      if (typeof channelId === "undefined") {
        throw new TypeError("Channel not found");
      }
      if (state.isOpen(channelId)) {
        payload = { close: [channelId] };
      } else {
        payload = { open: [channelId] };
      }
      break;
    }
    case WorkerMessages.TOGGLE_IS_CHANNEL_SELECTED: {
      const channelId = data.channelId;
      const channel = state.channelsMap.get(channelId);
      if (typeof channel === "undefined") {
        throw new TypeError("Channel not found");
      }

      if (state.isSelected(channelId)) {
        payload.deselect = [channelId];
      } else {
        payload.select = [channelId];
        // If we selected a parent on the first level, also select all recommended children
        const channel = state.channelsMap.get(channelId);
        if (isBaseChannel(channel)) {
          channel.children.forEach((child) => {
            if (child.recommended) {
              payload.select?.push(child.id);
            }
          });
        }
      }
      break;
    }
    case WorkerMessages.SET_RECOMMENDED_CHILDREN_ARE_SELECTED: {
      const channelId = data.channelId;
      const channel = state.channelsMap.get(channelId);
      if (typeof channel === "undefined" || !isBaseChannel(channel)) {
        throw new TypeError("Channel is not a base channel or is not found");
      }

      if (data.selected) {
        payload.select = [];
        channel.recommendedChildrenIds.forEach((id) => payload.select?.push(id));
      } else {
        payload.deselect = [];
        channel.recommendedChildrenIds.forEach((id) => payload.deselect?.push(id));
      }
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
  const { rows, selectedChannelsCount, selectedChannelLabels } = result;
  context.postMessage({
    type: WorkerMessages.STATE_CHANGED,
    rows,
    selectedChannelsCount,
    selectedChannelLabels,
  });
});
