import {
  canonicalizeBase,
  canonicalizeChild,
} from "manager/content-management/shared/components/panels/sources/channels/channels-api-transforms";

import { BaseChannelType, ChannelTreeType, ChannelType, ChildChannelType } from "core/channels/type/channels.type";
import { computeReverseDependencies } from "core/channels/utils/channels-dependencies.utils";

import { ActionChainLink, ActionLink } from "components/links";
import { MessageType, Utils as MessagesUtils } from "components/messages/messages";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { MigrationScheduleRequest } from "./types";

/**
 * Process the channels and the mandatory data to extract the information needed by the frontend logic
 * @param baseChannelTrees the array of channel trees
 * @param mandatoryMap a record mapping the requirements for each channel
 * @returns an object containing:
 *  - channelTrees, a clone of the baseChannelTree with all the fields of {@link BaseChannelType} and {@link ChildChannelType} populated
 *  - channelsMap, a map of {@link ChannelType} where the key is the channel id
 *  - baseChannels, an array containing all the base channels
 *  - requiresMap a mapping detailing for each channel id which other channels it requries
 *  - requiredByMap a mapping detailing for each channel id, which other channels requires it
 */
function processChannelData(baseChannelTrees: ChannelTreeType[], mandatoryMap: Record<string, number[]>) {
  const channelsMap: Map<number, ChannelType> = new Map();
  const baseChannels: BaseChannelType[] = [];

  // Process the base channel trees
  const channelTrees: ChannelTreeType[] = baseChannelTrees.map((channelTree) => {
    const updatedBase: BaseChannelType = Utils.deepCopy(channelTree.base);

    // Compute the base specific properties
    canonicalizeBase(updatedBase);
    // Add to the channels map
    channelsMap.set(updatedBase.id, updatedBase);
    baseChannels.push(updatedBase);

    const updatedChildren = channelTree.children.map((child) => {
      const updateChild: ChildChannelType = Utils.deepCopy(child);
      // Compute the children specific properties
      canonicalizeChild(updateChild, updatedBase);
      // If it's a reccomended child update the base
      if (updateChild.recommended) {
        updatedBase.recommendedChildren.push(updateChild);
      }

      // Add to the channels map
      channelsMap.set(updateChild.id, updateChild);

      return updateChild;
    });

    return { base: updatedBase, children: updatedChildren };
  });

  // Compute the dependencies
  const [requiresMap, requiredByMap] = processMandatoryMap(mandatoryMap, channelsMap);

  return { channelTrees, channelsMap, baseChannels, requiresMap, requiredByMap };
}

// Buildst the requiresMap and the requiredByMap
function processMandatoryMap(mandatoryData: Record<string, number[]>, channelsMap: Map<number, ChannelType>) {
  const requiresMap: Map<number, Set<number>> = new Map();

  Object.entries(mandatoryData).forEach(([id, mandatoryIds]) => {
    requiresMap.set(parseInt(id, 10), new Set(mandatoryIds));
  });

  return [requiresMap, computeReverseDependencies(requiresMap)].map(
    (map) =>
      new Map(
        Array.from(map.entries()).map(([channelId, dependentSet]) => [
          channelId,
          new Set([...dependentSet].map((id) => channelsMap.get(id)).filter((channel) => channel !== undefined)),
        ])
      )
  );
}

/**
 * Removes the circular dependencies from the channel intances, introduced by calling processChannelData and construct a
 * new ChannelTreeType instance.
 * @param channelTree the channel tree object
 * @returns a new instance of {@link ChannelTreeType} with the computed fields removed
 */
function getAsChannelTree(baseChannel: BaseChannelType, children: Set<ChildChannelType>): ChannelTreeType {
  // Clone the base object while omitting the computed fields.
  const { standardizedName: _ignoredName, recommendedChildren: _ignoreRecommended, ...cleanBase } = baseChannel;

  // Map the children set to create a new array of clean child objects.
  const cleanChildren = Array.from(children.values()).map((child) => {
    const { standardizedName: _snChild, parent: _pChild, ...cleanChild } = child;
    return cleanChild as ChildChannelType;
  });

  // Return the new object composed of the cleaned parts.
  return {
    base: cleanBase as BaseChannelType,
    children: cleanChildren,
  };
}

async function performMigration(migrationRequest: MigrationScheduleRequest): Promise<MessageType[]> {
  const response = await Network.post("/rhn/manager/api/systems/migration/schedule", migrationRequest);
  return MessagesUtils.info(
    migrationRequest.actionChain ? (
      <span>
        {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
          name: migrationRequest.actionChain,
          link: (str) => <ActionChainLink id={response.data}>{str}</ActionChainLink>,
        })}
      </span>
    ) : (
      <span>
        {t("The action has been <link>scheduled</link>.", {
          link: (str) => <ActionLink id={response.data}>{str}</ActionLink>,
        })}
      </span>
    )
  );
}

export const MigrationUtils = { processChannelData, getAsChannelTree, performMigration };
