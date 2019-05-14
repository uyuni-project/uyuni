// @flow
import {useState} from 'react';
import Network from 'utils/network';

import type JsonResult from "../../../utils/network";

type ExportersStatusType = {
  [string]: boolean
};

type ExportersResultType = {
  exporters: ExportersStatusType
};

const useMonitoringApi = () => {
    const [loading, setLoading] = useState(false);
    const [exportersStatus, setExportersStatus] = useState(null);
    const [messages, setMessages] = useState<Array<Object>>([]);

    const monitoringEnabled = exportersStatus ? 
      Object.keys(exportersStatus).every(key => exportersStatus[key] === true) :
      null;

    const fetchStatus = (): Promise<ExportersStatusType> => {
      setLoading(true);
      return Network.get("/rhn/manager/api/admin/config/monitoring").promise
      .then((data: JsonResult<ExportersResultType>) => {
        setExportersStatus(data.data.exporters);
        return data.data.exporters;
      })
      .finally(() => {
        setLoading(false);
      });
    };

    const changeStatus = (toEnable: boolean) => {
      setLoading(true);
      return Network.post("/rhn/manager/api/admin/config/monitoring", JSON.stringify({"enable": toEnable}), "application/json").promise
      .then((data : JsonResult<ExportersResultType>) => {
          if (data.data.exporters) {
            setExportersStatus(data.data.exporters);
            if (monitoringEnabled && !toEnable) { // enabled -> disabled
              const allDisabled : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === false);
              const someEnabled : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === true);
              if (allDisabled) {
                return {success: true, message: "disabling_succeeded"};
              } else if (someEnabled) {
                return {success: false, message: "disabling_failed_partially"};
              } else {
                return {success: false, message: "disabling_failed"};
              }
            } else if (!monitoringEnabled && toEnable) { // disabled -> enabled
              const allEnabled : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === true);
              const someDisabled : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === false);
              if (allEnabled) {
                return {success: true, message: "enabling_succeeded"};
              } else if (someDisabled) {
                return {success: false, message: "enabling_failed_partially"};
              } else {
                return {success: false, message: "enabling_failed"};
              }
            } else { // disabled -> disabled, enabled -> enabled
              return {success: true, message: "no_change"};
            }
          } else {
              setExportersStatus(null);
              return {success: false, message: "unknown"};
          }
        })
        .finally(() => {
          setLoading(false);
        });
    };

    return {
      loading,
      monitoringEnabled,
      fetchStatus,
      changeStatus,
      exportersStatus,
      messages,
      setMessages
    }

}

export default useMonitoringApi;