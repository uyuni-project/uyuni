export function getRequestParams(model: any, index: number): any {
  if (!["file", "volume"].includes(model[`disk${index}_type`])) {
    return null;
  }

  let source = Object.assign(
    {},
    { pool: model[`disk${index}_source_pool`] },
    model[`disk${index}_source_size`] !== "" ? { size: model[`disk${index}_source_size`] } : {},
    model[`disk${index}_source_template`] !== "" ? { template: model[`disk${index}_source_template`] } : {},
    model[`disk${index}_format`] !== "" ? { format: model[`disk${index}_format`] } : {},
    model[`disk${index}_source_file`] !== "" ? { source_file: model[`disk${index}_source_file`] } : {}
  );

  return Object.assign(
    {},
    {
      device: model[`disk${index}_device`],
      bus: model[`disk${index}_bus`],
    },
    source
  );
}

export function getModelFromDefinition(definition: any) {
  return definition.disks != null
    ? definition.disks.reduce((result, disk, index) => {
        if (disk != null) {
          const sourceToModel = {
            file: () => ({ [`disk${index}_source_file`]: disk.source ? disk.source.file : "" }),
            dir: () => ({ [`disk${index}_source_dir`]: disk.source ? disk.source.dir : "" }),
            block: () => ({ [`disk${index}_source_dev`]: disk.source ? disk.source.dev : "" }),
            network: () =>
              Object.assign(
                {
                  [`disk${index}_source_protocol`]: disk.source ? disk.source.protocol : "",
                  [`disk${index}_source_name`]: disk.source ? disk.source.name : "",
                },
                disk.source && disk.source.host
                  ? disk.source.host.reduce(
                      (resultHosts, host, hostIndex) =>
                        Object.assign(resultHosts, {
                          [`disk${index}_source_host${hostIndex}_name`]: host.name,
                          [`disk${index}_source_host${hostIndex}_port`]: host.port,
                        }),
                      {}
                    )
                  : {}
              ),
            volume: () => ({
              [`disk${index}_source_pool`]: disk.source ? disk.source.pool : "",
              [`disk${index}_source_file`]: disk.source ? disk.source.volume : "",
            }),
          };

          const editable = disk.device === "cdrom" ? { [`disk${index}_editable`]: true } : {};

          return Object.assign(
            result,
            {
              [`disk${index}_type`]: disk.type,
              [`disk${index}_device`]: disk.device,
              [`disk${index}_target`]: disk.target,
              [`disk${index}_bus`]: disk.bus,
              [`disk${index}_format`]: disk.format,
            },
            sourceToModel[disk.type](),
            editable
          );
        }
        return result;
      }, {})
    : {};
}
