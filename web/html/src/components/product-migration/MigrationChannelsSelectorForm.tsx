import { type FC, type ReactNode, useMemo, useState } from "react";

import BaseChannel from "manager/content-management/shared/components/panels/sources/channels/base-channel";
import { ChannelProcessor } from "manager/content-management/shared/components/panels/sources/channels/channel-processor";

import {
  BaseChannelType,
  ChannelTreeType,
  ChannelType,
  ChildChannelType,
  isChildChannel,
} from "core/channels/type/channels.type";

import { Button, SubmitButton } from "components/buttons";
import { Form, FormGroup, Label, Select } from "components/input";
import { Toggler } from "components/toggler";

import { MigrationProductList } from "./MigrationProductList";
import { MigrationProduct, MigrationTarget } from "./types";
import { MigrationUtils } from "./utils";

type Props = {
  migrationSource: MigrationProduct;
  migrationTarget: MigrationTarget;
  baseChannelTrees: ChannelTreeType[];
  mandatoryMap: Record<string, number[]>;
  baseChannel?: BaseChannelType;
  childChannels?: ChildChannelType[];
  allowVendorChange?: boolean;
  onChannelSelection: (channelTree: ChannelTreeType, allowVendorChange: boolean) => void;
  onBack: () => void;
};

export const MigrationChannelsSelectorForm: FC<Props> = ({
  migrationSource,
  migrationTarget,
  baseChannelTrees,
  mandatoryMap,
  baseChannel,
  childChannels,
  allowVendorChange,
  onChannelSelection,
  onBack,
}): JSX.Element => {
  // Compute and cache the data
  const { channelTrees, channelsMap, baseChannels, requiresMap, requiredByMap } = useMemo(() => {
    return MigrationUtils.processChannelData(baseChannelTrees, mandatoryMap);
  }, [baseChannelTrees, mandatoryMap]);

  // Cache the processor
  const channelProcessor = useMemo(() => {
    const initialBase = baseChannel ?? channelTrees[0].base;

    const processor = new ChannelProcessor();
    processor.setChannels(channelTrees, channelsMap, requiresMap, requiredByMap, initialBase.id);

    return processor;
  }, [baseChannel, channelTrees, channelsMap, requiresMap, requiredByMap]);

  function getRequiredChannelsForBase(
    base: BaseChannelType,
    additionalChildren?: ChildChannelType[]
  ): Set<ChildChannelType> {
    const selectionSet: Set<ChildChannelType> = new Set();

    channelProcessor.getRequires(base.id)?.forEach((item) => {
      if (isChildChannel(item)) {
        selectionSet.add(item);
      }
    });

    // Ensure we get the correct instance from the channel processor, otherwise the set won't behave as expected
    additionalChildren?.forEach((child) =>
      selectionSet.add(channelProcessor.getChannelById(child.id) as ChildChannelType)
    );

    return new Set(selectionSet.values());
  }

  const [selectedBaseChannel, setSelectedBaseChannel] = useState(baseChannel ?? baseChannels[0]);
  const [selectedChildChannels, setSelectedChildChannels] = useState(() =>
    getRequiredChannelsForBase(selectedBaseChannel, childChannels)
  );
  const [selectedAllowVendorChange, setSelectedAllowVendorChange] = useState(allowVendorChange ?? false);

  const selectedChannelTree = useMemo(
    () => channelTrees.find((ch) => ch.base.id === selectedBaseChannel.id),
    [channelTrees, selectedBaseChannel]
  );

  const selectedChildChannelsIds = useMemo(
    () => new Set(Array.from(selectedChildChannels).map((channel) => channel.id)),
    [selectedChildChannels]
  );

  function onToggleChannelSelect(channelId: number, toState?: boolean): void {
    const channel = channelProcessor.getChannelById(channelId);
    // Ignore any updates for base channels, the UI uses the dropdown to choose it
    if (!isChildChannel(channel)) {
      return;
    }

    const updatedSet = new Set(selectedChildChannels);
    const isAddition = toState ?? !updatedSet.has(channel);

    const updateAction = isAddition ? updatedSet.add.bind(updatedSet) : updatedSet.delete.bind(updatedSet);
    const relatedChannels = isAddition
      ? channelProcessor.getRequires(channel.id)
      : channelProcessor.getRequiredBy(channel.id);

    [channel, ...(relatedChannels ?? new Set<ChannelType>())]
      .filter((item) => isChildChannel(item))
      .forEach((item) => updateAction(item));

    setSelectedChildChannels(updatedSet);
  }

  function onChangeBase(value: string): void {
    if (value === selectedBaseChannel.id.toString()) {
      return;
    }

    const newBase = baseChannels.find((base) => base.id.toString() === value);
    if (newBase !== undefined) {
      channelProcessor.setSelectedBaseChannelId(newBase.id).then(() => {
        setSelectedBaseChannel(newBase);
        setSelectedChildChannels(getRequiredChannelsForBase(newBase));
      });
    }
  }

  function onSubmit(): void {
    const channelTree = MigrationUtils.getAsChannelTree(selectedBaseChannel, selectedChildChannels);
    onChannelSelection(channelTree, selectedAllowVendorChange);
  }

  function renderChildren(): ReactNode {
    if (selectedChannelTree === undefined) {
      return <></>;
    }

    return (
      <div className="col-md-9 form-control-static">
        <BaseChannel
          channelTree={selectedChannelTree}
          recommendedToggle={false}
          showBase={false}
          channelProcessor={channelProcessor}
          selectedChannelIds={selectedChildChannelsIds}
          onToggleChannelSelect={onToggleChannelSelect}
        />
      </div>
    );
  }

  return (
    <Form className="form-horizontal" onSubmit={onSubmit}>
      <FormGroup>
        <Label className="col-md-3" name={t("Source Product")} />
        <div className="form-control-static col-md-6">
          <MigrationProductList product={migrationSource} />
        </div>
      </FormGroup>

      <FormGroup>
        <Label className="col-md-3" name={t("Target Product")} />
        <div className="form-control-static col-md-6">
          <MigrationProductList product={migrationTarget.targetProduct} />
        </div>
      </FormGroup>

      <FormGroup>
        <Label className="col-md-3" name={t("Target base channel")} />
        <div className="col-md-6">
          <Select
            name="selectedChannelId"
            value={selectedBaseChannel.id.toString()}
            options={baseChannels}
            getOptionValue={(channel: BaseChannelType) => channel.id.toString()}
            getOptionLabel={(channel: BaseChannelType) => channel.name}
            onChange={onChangeBase}
          />
        </div>
      </FormGroup>

      <FormGroup>
        <Label className="col-md-3" name={t("Target child channels")} />
        {renderChildren()}
      </FormGroup>

      <FormGroup>
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <Toggler
            text={t("Allow vendor change")}
            value={selectedAllowVendorChange}
            className="btn"
            handler={setSelectedAllowVendorChange}
          />
        </div>
      </FormGroup>

      <div className="col-md-offset-3 offset-md-3 btn-group">
        <Button
          id="back-btn"
          icon="fa-chevron-left"
          className="btn-default"
          text={t("Back to target selection")}
          handler={onBack}
        />
        <SubmitButton id="submit-btn" className="btn-primary" text={t("Schedule Migration")} />
      </div>
    </Form>
  );
};
