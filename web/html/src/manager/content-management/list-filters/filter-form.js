//@flow
import React from 'react';
import {Text} from "components/input/Text";
import {Select} from "components/input/Select";
import {Form} from "components/input/Form";
import {Check} from "components/input/Check";
import type {FilterFormType} from "../shared/type/filter.type";
import filtersEnum from "../shared/business/filters.enum";
import type {FilterOptionType} from "../shared/business/filters.enum";

type Props = {
  filter: FilterFormType,
  onChange: Function,
  onClientValidate: Function,
  editing?: boolean
}

const FilterForm = (props: Props) => {

  return (
    <Form
      model={{...props.filter}}
      onValidate={props.onClientValidate}
      onChange={model => {
        props.onChange(model);
      }}
    >
      <React.Fragment>
        <div className="row">
          <Text
            name="name"
            label={t("Filter Name")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            disabled={props.editing}
          />
        </div>
        <div className="row">
          <Select
            name="type"
            label={t("Filter Type")}
            labelClass="col-md-3"
            divClass="col-md-6"
            defaultValue={""}
            required
            disabled={props.editing}
          >
            <option disabled selected value=""> -- select a filter type -- </option>
            {
              filtersEnum.getFiltersOptions().map((filter: FilterOptionType) =>
                <option
                  key={filter.key}
                  value={filter.key}
                >
                  {t(filter.text)}
                </option>
              )
            }
          </Select>
        </div>

        {
          filtersEnum.enum.PACKAGE.key === props.filter.type &&
          <div className="row">
            <Text
              name="criteria"
              label={t("Name contains")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
            />
          </div>
        }

        {
          filtersEnum.enum.PACKAGE_NEVRA.key === props.filter.type &&
          <>
            <Text
              name="packageName"
              label={t("Package Name")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
            />
            <Text
              name="epoch"
              label={t("Epoch")}
              labelClass="col-md-3"
              divClass="col-md-6" />
            <Text
              name="version"
              label={t("Version")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
            />
            <Text
              name="release"
              label={t("Release")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
            />
            <Text
              name="architecture"
              label={t("Architecture")}
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        }

        {
          filtersEnum.enum.ERRATUM.key === props.filter.type &&
          <div className="row">
            <Text
              name="advisoryName"
              label={t("Advisory name")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
            />
          </div>
        }

        <Check
          name="deny"
          label={t("deny")}
          disabled
          divClass="col-md-8 col-md-offset-3"/>


      </React.Fragment>
    </Form>
  )}

export default FilterForm;
