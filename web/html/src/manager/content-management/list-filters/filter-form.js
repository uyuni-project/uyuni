//@flow
import React, {useEffect} from 'react';
import {Text} from "components/input/Text";
import {DateTime} from "components/input/DateTime";
import {Radio} from "components/input/Radio";
import {Select} from "components/input/Select";
import {Form} from "components/input/Form";
import AppStreamsForm from "./appstreams/appstreams";
import type {FilterFormType} from "../shared/type/filter.type";
import type {ClmFilterOptionType, FilterMatcherType} from "../shared/business/filters.enum";
import {clmFilterOptions, findClmFilterByKey, getClmFiltersOptions} from "../shared/business/filters.enum";
import useUserLocalization from "core/user-localization/use-user-localization";
import Functions from "utils/functions";
import produce from "immer";

type Props = {
  filter: FilterFormType,
  onChange: Function,
  onClientValidate: Function,
  editing?: boolean
}

const FilterForm = (props: Props) => {
  const {timezone, localTime} = useUserLocalization();

  // If the filter type changes, resets the matcher filter
  useEffect(() => {
    if(!props.editing) {
      props.onChange(
        produce(props.filter, draft => {
          const selectedFilter = findClmFilterByKey(props.filter.type);
          if (selectedFilter && selectedFilter.matchers.length === 1) {
            draft.matcher = selectedFilter.matchers[0].key
          } else {
            delete draft.matcher
          }
          if(clmFilterOptions.ADVISORY_TYPE.key === props.filter.type) {
            draft[clmFilterOptions.ADVISORY_TYPE.key] = "Security Advisory";
          }
          if(clmFilterOptions.ISSUE_DATE.key === props.filter.type) {
            draft[clmFilterOptions.ISSUE_DATE.key] = Functions.Utils.dateWithTimezone(localTime);
          }
        })
      )
    };
  }, [props.filter.type])

  const selectedFilter = findClmFilterByKey(props.filter.type);
  const selectedFilterMatchers = selectedFilter && selectedFilter.matchers;

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
            name="filter_name"
            label={t("Filter Name")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            disabled={props.editing}
          />
        </div>

        <Select
          name="type"
          label={t("Filter Type")}
          labelClass="col-md-3"
          divClass="col-md-6"
          required
          disabled={props.editing}
        >
          <option disabled selected value=""> -- select a filter type -- </option>
          {
            getClmFiltersOptions().map((filter: ClmFilterOptionType) =>
              <option
                key={filter.key}
                value={filter.key}
              >
                {`${filter.entityType.text} (${filter.text})`}
              </option>
            )
          }
        </Select>

        {
          selectedFilterMatchers &&
          <Select
            name="matcher"
            label={t("Matcher")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            disabled={props.editing}
          >
            <option disabled selected value=""> -- select a matcher -- </option>
            {
              selectedFilterMatchers.map((matcher: FilterMatcherType) =>
                <option
                  key={matcher.key}
                  value={matcher.key}
                >
                  {matcher.text}
                </option>
              )
            }
          </Select>
        }

        {
          clmFilterOptions.NAME.key === props.filter.type &&
          <Text
            name={clmFilterOptions.NAME.key}
            label={t("Package Name")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
          />
        }

        {
          clmFilterOptions.NEVRA.key === props.filter.type &&
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
          clmFilterOptions.PACKAGE_NEVR.key === props.filter.type &&
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
          </>
        }

        {
          clmFilterOptions.ADVISORY_NAME.key === props.filter.type &&
          <Text
            name={clmFilterOptions.ADVISORY_NAME.key}
            label={t("Advisory name")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
          />
        }

        {
          clmFilterOptions.ADVISORY_TYPE.key === props.filter.type &&
          <Radio
            name={clmFilterOptions.ADVISORY_TYPE.key}
            required
            items={[
              {"label": t("Security Advisory"), "value": "Security Advisory"},
              {"label": t("Bug Fix Advisory"), "value": "Bug Fix Advisory"},
              {"label": t("Product Enhancement Advisory"), "value": "Product Enhancement Advisory"}
            ]}
            label={t("Advisory Type")}
            labelClass="col-md-3"
            divClass="col-md-6" />
        }

        {
          clmFilterOptions.ISSUE_DATE.key === props.filter.type &&
          <DateTime
            name={clmFilterOptions.ISSUE_DATE.key}
            label={t("Issued")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
            timezone={timezone}
          />
        }

        {
          clmFilterOptions.SYNOPSIS.key === props.filter.type &&
          <Text
            name={clmFilterOptions.SYNOPSIS.key}
            label={t("Synopsis")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
          />
        }

        {
          clmFilterOptions.KEYWORD.key === props.filter.type &&
          <Radio
            name={clmFilterOptions.KEYWORD.key}
            required
            items={[
              {"label": t("Reboot Required"), "value": "reboot_suggested"},
              {"label": t("Package Manager Restart Required"), "value": "restart_suggested"},
            ]}
            openOption
            label={t("Advisory Keywords")}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
        }

        {
          clmFilterOptions.PACKAGE_NAME.key === props.filter.type &&
          <Text
            name={clmFilterOptions.PACKAGE_NAME.key}
            label={t("Package Name")}
            labelClass="col-md-3"
            divClass="col-md-6"
            required
          />
        }

        {
          clmFilterOptions.STREAM.key === props.filter.type &&
          <>
            <AppStreamsForm/>
            <input type="hidden" name="rule" value="allow"/>
          </>
        }

        {
          clmFilterOptions.STREAM.key !== props.filter.type &&
          <Radio
            inline
            name="rule"
            items={[
              {"label": t("Deny"), "value": "deny"},
              {"label": t("Allow"), "value": "allow"}
            ]}
            label={t("Rule")}
            labelClass="col-md-3"
            divClass="col-md-6" />
        }


      </React.Fragment>
    </Form>
  )}

export default FilterForm;
