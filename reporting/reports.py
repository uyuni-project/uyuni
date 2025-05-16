# pylint: disable=missing-module-docstring
import os
import re

# pylint: disable-next=unused-import
import sys

from spacewalk.common.rhnConfig import RHNOptions

BASE_REPORT_DEFINITIONS = "/usr/share/spacewalk/reports"


# pylint: disable-next=redefined-builtin
def available_reports(type):
    return os.listdir(os.path.join(BASE_REPORT_DEFINITIONS, type))


# pylint: disable-next=missing-class-docstring,invalid-name
class report:
    # pylint: disable-next=redefined-builtin
    def __init__(self, name, type, params):
        full_path = os.path.join(BASE_REPORT_DEFINITIONS, type, name)
        self.sql = None
        self.description = None
        self.synopsis = None
        self.columns = None
        self.column_indexes = None
        self.column_types = None
        self.column_descriptions = None
        self.multival_column_names = {}
        self.multival_columns_reverted = {}
        self.multival_columns_stop = []
        self.params = {}
        self.params_values = params
        self._load(full_path)

    def _load(self, full_path):
        try:
            # pylint: disable-next=unspecified-encoding
            fd = open(full_path, "r")
        except IOError as e:
            raise spacewalk_unknown_report from e
        tag = None
        value = ""
        # pylint: disable-next=anomalous-backslash-in-string
        re_comment = re.compile("^\s*#")
        # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string
        re_tag_name = re.compile("^(\S+):\s*$")

        for line in fd:
            result = re_comment.match(line)
            # pylint: disable-next=singleton-comparison
            if result != None:
                continue

            result = re_tag_name.match(line)
            # pylint: disable-next=singleton-comparison
            if result != None:
                # pylint: disable-next=singleton-comparison
                if tag != None:
                    self._set(tag, value)
                    tag = None
                    value = ""
                tag = result.group(1)
            else:
                value += line

        # pylint: disable-next=singleton-comparison
        if tag != None:
            self._set(tag, value)

        # pylint: disable-next=singleton-comparison
        if self.multival_column_names != None:
            unknown_columns = []

            # pylint: disable-next=consider-using-dict-items
            for c in self.multival_column_names:
                if c in self.column_indexes:
                    c_id = self.column_indexes[c]
                    v = self.multival_column_names[c]
                    # pylint: disable-next=singleton-comparison
                    if v == None:
                        self.multival_columns_stop.append(c_id)
                    elif v in self.column_indexes:
                        v_id = self.column_indexes[v]
                        if v_id in self.multival_columns_reverted:
                            self.multival_columns_reverted[v_id].append(c_id)
                        else:
                            self.multival_columns_reverted[v_id] = [c_id]
                else:
                    unknown_columns.append(c)
            if len(unknown_columns) > 0:
                raise spacewalk_report_unknown_multival_column_exception(
                    unknown_columns
                )

    def _set(self, tag, value):
        if tag == "columns":
            self.columns = []
            self.column_indexes = {}
            self.column_types = {}
            self.column_descriptions = {}
            # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string
            lines = filter(None, re.split("\s*\n\s*", value))
            i = 0
            for l in lines:
                description = None
                try:
                    # pylint: disable-next=anomalous-backslash-in-string
                    (c, description) = re.split("\s+", l, 1)
                # pylint: disable-next=bare-except
                except:
                    c = l
                try:
                    (c, t) = re.split(":", c, 1)
                # pylint: disable-next=bare-except
                except:
                    t = "s"
                self.columns.append(c)
                self.column_indexes[c] = i
                self.column_types[c] = t
                # pylint: disable-next=singleton-comparison
                if description != None:
                    self.column_descriptions[c] = description
                i = i + 1
        elif tag == "params":
            # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string
            lines = filter(None, re.split("\s*\n\s*", value))
            for l in lines:
                # pylint: disable-next=anomalous-backslash-in-string
                (p, v) = re.split("\s+", l, 1)
                value = v
                if p in self.params_values:
                    value = self.params_values[p]
                else:
                    try:
                        # pylint: disable-next=anomalous-backslash-in-string
                        (component, option) = re.split("\.", v, 1)
                        cfg = RHNOptions(component)
                        cfg.parse()
                        value = str(cfg.get(option))
                    except ValueError:
                        # This wasn't a configuration option, assume the value is the default
                        pass
                self.params[p] = value
        elif tag == "multival_columns":
            # the multival_columns specifies either
            # a "stop" column, usually the first one,
            # or a pair of column names separated by colon,
            # where the first on is column which should be
            # joined together and the second one is column
            # whose value should be used to distinguish if
            # we still have the same entity or not.
            for l in filter(None, re.split("\n", value)):
                # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string
                m = re.match("^\s*(\S+?)(\s*:\s*(\S*)\s*)?$", l)
                # pylint: disable-next=singleton-comparison
                if m == None:
                    continue
                (col, id_col) = (m.group(1), m.group(3))
                # pylint: disable-next=singleton-comparison
                if col != None:
                    self.multival_column_names[col] = id_col
        elif tag == "sql":
            self.sql = value
        elif tag == "synopsis":
            # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string
            self.synopsis = re.sub("^(\s*\n)+\s*|(\s*\n)+$", "", value)
        elif tag == "description":
            self.description = re.sub(
                # pylint: disable-next=anomalous-backslash-in-string
                "(?m)^\s*",
                "    ",
                # pylint: disable-next=anomalous-backslash-in-string,anomalous-backslash-in-string,anomalous-backslash-in-string
                re.sub("^(\s*\n)+\s*|(\s*\n)+$", "", value),
            )
        else:
            raise spacewalk_report_unknown_tag_exception(tag)


# pylint: disable-next=invalid-name
class spacewalk_unknown_report(Exception):
    pass


# pylint: disable-next=invalid-name
class spacewalk_report_unknown_tag_exception(Exception):
    pass


# pylint: disable-next=invalid-name
class spacewalk_report_unknown_multival_column_exception(Exception):
    pass
