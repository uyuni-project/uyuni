import * as React from "react";
import { useState, useEffect } from "react";
import Network, { JsonResult } from "utils/network";
import { usePrevious } from "utils/hooks";
import { Select, FormContext } from "components/input";
import { Props as FilterFormProps } from "../filter-form";

import { Template } from "./index";

type Product = {
  id: number;
  label: string;
};

type System = {
  id: number;
  name: string;
  kernel: string;
};

type Kernel = {
  id: number;
  version: string;
  latest?: boolean;
};

function getProducts(): Promise<Product[]> {
  return Network.get<JsonResult<Product[]>>("/rhn/manager/api/contentmanagement/livepatching/products").then(
    Network.unwrap
  );
}

function getSystems(query: string): Promise<System[]> {
  return Network.get<JsonResult<System[]>>(`/rhn/manager/api/contentmanagement/livepatching/systems?q=${query}`).then(
    Network.unwrap
  );
}

function getKernels(id: number, type: string): Promise<Kernel[]> {
  return Network.get<JsonResult<Kernel[]>>(`/rhn/manager/api/contentmanagement/livepatching/kernels/${type}/${id}`)
    .then(Network.unwrap)
    .then((res) => {
      if (res.length > 0) res[0].latest = true;
      return res;
    });
}

export default (props: FilterFormProps & { template: Template }) => {
  const template = props.template;
  const prevTemplate = usePrevious(template);

  const formContext = React.useContext(FormContext);
  const setModelValue = formContext.setModelValue;
  const { productId, systemId, systemName, kernelName } = formContext.model;
  const [products, setProducts] = useState<Product[]>([]);
  const [kernels, setKernels] = useState<Kernel[]>([]);

  useEffect(() => {
    getProducts().then(setProducts).catch(Network.showResponseErrorToastr);
  }, []);

  useEffect(() => {
    // If the template changes, reset what we previously had
    if (prevTemplate) {
      setModelValue?.("systemId", null);
      setModelValue?.("productId", null);
      setModelValue?.("kernelId", null);
      setKernels([]);
    }
  }, [template]);

  useEffect(() => {
    if (systemId || productId) {
      getKernels(systemId ?? productId, systemId ? "system" : "product")
        .then((result) => {
          setKernels(result);

          const latestKernel = result.find((item) => Boolean(item.latest));
          const kernelId = latestKernel?.id ?? result[0]?.id ?? null;
          setModelValue?.("kernelId", kernelId);
        })
        .catch(Network.showResponseErrorToastr);
    } else {
      setKernels([]);
      setModelValue?.("kernelId", null);
    }
  }, [systemId, productId]);

  // Are we using predefined values from the URL params?
  const hasInitialValues = Boolean(systemId && systemName && kernelName);
  const defaultValueOption = hasInitialValues
    ? {
        id: systemId,
        name: systemName,
        kernel: kernelName,
      }
    : undefined;

  return (
    <>
      {template === Template.LivePatchingProduct && (
        <>
          <Select
            name="productId"
            label={t("Product")}
            labelClass="col-md-3"
            divClass="col-md-8"
            options={products}
            getOptionValue={(product) => product.id}
            getOptionLabel={(product) => product.label}
          />
        </>
      )}
      {template === Template.LivePatchingSystem && (
        <>
          <Select
            loadOptions={getSystems}
            name="systemId"
            label={t("System")}
            labelClass="col-md-3"
            divClass="col-md-8"
            getOptionValue={(system) => system.id}
            getOptionLabel={(system) => `${system.name} (${system.kernel})`}
            defaultValueOption={defaultValueOption}
          />
        </>
      )}
      <Select
        name="kernelId"
        label={t("Kernel")}
        labelClass="col-md-3"
        divClass="col-md-8"
        required={!!(systemId || productId)}
        disabled={!systemId && !productId}
        options={kernels}
        getOptionValue={(kernel) => kernel.id}
        getOptionLabel={(kernel) => `${kernel.version}${kernel.latest ? ` (${t("latest")})` : ""}`}
      />
    </>
  );
};
