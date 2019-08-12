//@flow
import React from 'react';
import {Text} from "components/input/Text";
import {DateTime} from "components/input/DateTime";
import {Radio} from "components/input/Radio";
import {Select} from "components/input/Select";
import {Form} from "components/input/Form";
import type {FilterFormType} from "../shared/type/filter.type";
import filtersEnum from "../shared/business/filters.enum";
import type {FilterOptionType} from "../shared/business/filters.enum";
import useUserLocalization from "core/user-localization/use-user-localization";
import Functions from "utils/functions";

type Props = {
  filter: FilterFormType,
  onChange: Function,
  onClientValidate: Function,
  editing?: boolean
}

const FilterForm = (props: Props) => {

  const {timezone, localTime} = useUserLocalization();

  return (
    <Form
      model={{...props.filter}}
      onValidate={props.onClientValidate}
      onChange={model => {
        props.onChange(model);
      }}
    >
      <React.Fragment>
        {
          props.editing &&
          <div className="alert alert-info" style={{marginTop: "0px"}}>
            {t("Bear in mind that all the associated projects need to be rebuilt after a filter update")}
          </div>

        }
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

        {
          filtersEnum.enum.ERRATUM_BYDATE.key === props.filter.type &&
          <div className="row">
            <DateTime
              name="issueDate"
              label={t("Issued After")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
              timezone={timezone}
              defaultValue={Functions.Utils.dateWithTimezone(localTime)}
            />

          </div>
        }

        <Radio
          horizontal
          name="rule"
          defaultValue="deny"
          items={[
            {"label": t("Deny"), "value": "deny"},
            {"label": t("Allow"), "value": "allow"}
          ]}
          label={t("Rule")}
          labelClass="col-md-3"
          divClass="col-md-6">
        </Radio>

      </React.Fragment>
    </Form>
  )}

export default FilterForm;
