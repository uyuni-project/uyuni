#
# Copyright (c) 2008--2013 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
# Modified from Python 2.x to work with Python 1.5.x

"""Configuration file parser.

A setup file consists of sections, lead by a "[section]" header,
and followed by "name: value" entries, with continuations and such in
the style of RFC 822.

The option values can contain format strings which refer to other values in
the same section, or values in a special [DEFAULT] section.

For example:

    something: %(dir)s/whatever

would resolve the "%(dir)s" to the value of dir.  All reference
expansions are done late, on demand.

Intrinsic defaults can be specified by passing them into the
ConfigParser constructor as a dictionary.

class:

ConfigParser -- responsible for for parsing a list of
                configuration files, and managing the parsed database.

    methods:

    __init__(defaults=None)
        create the parser and specify a dictionary of intrinsic defaults.  The
        keys must be strings, the values must be appropriate for %()s string
        interpolation.  Note that `__name__' is always an intrinsic default;
        it's value is the section's name.

    sections()
        return all the configuration section names, sans DEFAULT

    has_section(section)
        return whether the given section exists

    has_option(section, option)
        return whether the given option exists in the given section

    options(section)
        return list of configuration options for the named section

    read(filenames)
        read and parse the list of named configuration files, given by
        name.  A single filename is also allowed.  Non-existing files
        are ignored.

    readfp(fp, filename=None)
        read and parse one configuration file, given as a file object.
        The filename defaults to fp.name; it is only used in error
        messages (if fp has no `name' attribute, the string `<???>' is used).

    get(section, option, raw=0, vars=None)
        return a string value for the named option.  All % interpolations are
        expanded in the return values, based on the defaults passed into the
        constructor and the DEFAULT section.  Additional substitutions may be
        provided using the `vars' argument, which must be a dictionary whose
        contents override any pre-existing defaults.

    getint(section, options)
        like get(), but convert value to an integer

    getfloat(section, options)
        like get(), but convert value to a float

    getboolean(section, options)
        like get(), but convert value to a boolean (currently case
        insensitively defined as 0, false, no, off for 0, and 1, true,
        yes, on for 1).  Returns 0 or 1.

    remove_section(section)
        remove the given file section and all its options

    remove_option(section, option)
        remove the given option from the given section

    set(section, option, value)
        set the given option

    write(fp)
        write the configuration state in .ini format
"""

import re
import types
import string
import sys

__all__ = ["NoSectionError","DuplicateSectionError","NoOptionError",
           "InterpolationError","InterpolationDepthError","ParsingError",
           "MissingSectionHeaderError","ConfigParser",
           "DEFAULTSECT", "MAX_INTERPOLATION_DEPTH"]

DEFAULTSECT = "DEFAULT"

MAX_INTERPOLATION_DEPTH = 10



# exception classes
class Error(Exception):
    def __init__(self, msg=''):
        self._msg = msg
        Exception.__init__(self, msg)
    def __repr__(self):
        return self._msg
    __str__ = __repr__

class NoSectionError(Error):
    def __init__(self, section):
        Error.__init__(self, 'No section: %s' % section)
        self.section = section

class DuplicateSectionError(Error):
    def __init__(self, section):
        Error.__init__(self, "Section %s already exists" % section)
        self.section = section

class NoOptionError(Error):
    def __init__(self, option, section):
        Error.__init__(self, "No option `%s' in section: %s" %
                       (option, section))
        self.option = option
        self.section = section

class InterpolationError(Error):
    def __init__(self, reference, option, section, rawval):
        Error.__init__(self,
                       "Bad value substitution:\n"
                       "\tsection: [%s]\n"
                       "\toption : %s\n"
                       "\tkey    : %s\n"
                       "\trawval : %s\n"
                       % (section, option, reference, rawval))
        self.reference = reference
        self.option = option
        self.section = section

class InterpolationDepthError(Error):
    def __init__(self, option, section, rawval):
        Error.__init__(self,
                       "Value interpolation too deeply recursive:\n"
                       "\tsection: [%s]\n"
                       "\toption : %s\n"
                       "\trawval : %s\n"
                       % (section, option, rawval))
        self.option = option
        self.section = section

class ParsingError(Error):
    def __init__(self, filename):
        Error.__init__(self, 'File contains parsing errors: %s' % filename)
        self.filename = filename
        self.errors = []

    def append(self, lineno, line):
        self.errors.append((lineno, line))
        self._msg = self._msg + '\n\t[line %2d]: %s' % (lineno, line)

class MissingSectionHeaderError(ParsingError):
    def __init__(self, filename, lineno, line):
        Error.__init__(
            self,
            'File contains no section headers.\nfile: %s, line: %d\n%s' %
            (filename, lineno, line))
        self.filename = filename
        self.lineno = lineno
        self.line = line



class ConfigParser:
    def __init__(self, defaults=None):
        self.__sections = {}
        if defaults is None:
            self.__defaults = {}
        else:
            self.__defaults = defaults

    def defaults(self):
        return self.__defaults

    def sections(self):
        """Return a list of section names, excluding [DEFAULT]"""
        # self.__sections will never have [DEFAULT] in it
        return self.__sections.keys()

    def add_section(self, section):
        """Create a new section in the configuration.

        Raise DuplicateSectionError if a section by the specified name
        already exists.
        """
        if self.__sections.has_key(section):
            raise DuplicateSectionError(section)
        self.__sections[section] = {}

    def has_section(self, section):
        """Indicate whether the named section is present in the configuration.

        The DEFAULT section is not acknowledged.
        """
        return self.__sections.has_key(section)

    def options(self, section):
        """Return a list of option names for the given section name."""
        try:
            opts = self.__sections[section].copy()
        except KeyError:
            raise NoSectionError(section), None, sys.exc_info()[2]
        opts.update(self.__defaults)
        if opts.has_key('__name__'):
            del opts['__name__']
        return opts.keys()

    def read(self, filenames):
        """Read and parse a filename or a list of filenames.

        Files that cannot be opened are silently ignored; this is
        designed so that you can specify a list of potential
        configuration file locations (e.g. current directory, user's
        home directory, systemwide directory), and all existing
        configuration files in the list will be read.  A single
        filename may also be given.
        """
        if isinstance(filenames, types.StringType):
            filenames = [filenames]
        for filename in filenames:
            try:
                fp = open(filename)
            except IOError:
                continue
            self.__read(fp, filename)
            fp.close()

    def readfp(self, fp, filename=None):
        """Like read() but the argument must be a file-like object.

        The `fp' argument must have a `readline' method.  Optional
        second argument is the `filename', which if not given, is
        taken from fp.name.  If fp has no `name' attribute, `<???>' is
        used.

        """
        if filename is None:
            try:
                filename = fp.name
            except AttributeError:
                filename = '<???>'
        self.__read(fp, filename)

    def get(self, section, option, raw=0, vars=None):
        """Get an option value for a given section.

        All % interpolations are expanded in the return values, based on the
        defaults passed into the constructor, unless the optional argument
        `raw' is true.  Additional substitutions may be provided using the
        `vars' argument, which must be a dictionary whose contents overrides
        any pre-existing defaults.

        The section DEFAULT is special.
        """
        d = self.__defaults.copy()
        try:
            d.update(self.__sections[section])
        except KeyError:
            if section != DEFAULTSECT:
                raise NoSectionError(section), None, sys.exc_info()[2]
        # Update with the entry specific variables
        if vars is not None:
            d.update(vars)
        option = self.optionxform(option)
        try:
            value = d[option]
        except KeyError:
            raise NoOptionError(option, section), None, sys.exc_info()[2]

        if raw:
            return value
        return self._interpolate(section, option, value, d)

    def _interpolate(self, section, option, rawval, vars):
        # do the string interpolation
        value = rawval
        depth = MAX_INTERPOLATION_DEPTH
        while depth:                    # Loop through this until it's done
            depth = depth - 1
            if string.find(value, "%(") != -1:
                try:
                    value = value % vars
                except KeyError, key:
                    raise InterpolationError(key, option, section, rawval), None, sys.exc_info()[2]
            else:
                break
        if string.find(value, "%(") != -1:
            raise InterpolationDepthError(option, section, rawval)
        return value

    def __get(self, section, conv, option):
        return conv(self.get(section, option))

    def getint(self, section, option):
        return self.__get(section, int, option)

    def getfloat(self, section, option):
        return self.__get(section, float, option)

    _boolean_states = {'1': True, 'yes': True, 'true': True, 'on': True,
                       '0': False, 'no': False, 'false': False, 'off': False}

    def getboolean(self, section, option):
        v = self.get(section, option)
        lv = string.lower(v)
        if not self._boolean_states.has_key(lv):
            raise ValueError, 'Not a boolean: %s' % v
        return self._boolean_states[lv]

    def optionxform(self, optionstr):
        return string.lower(optionstr)

    def has_option(self, section, option):
        """Check for the existence of a given option in a given section."""
        if not section or section == DEFAULTSECT:
            option = self.optionxform(option)
            return self.__defaults.has_key(option)
        elif not self.__sections.has_key(section):
            return 0
        else:
            option = self.optionxform(option)
            return (self.__sections[section].has_key(option)
                    or self.__defaults.has_key(option))

    def set(self, section, option, value):
        """Set an option."""
        if not section or section == DEFAULTSECT:
            sectdict = self.__defaults
        else:
            try:
                sectdict = self.__sections[section]
            except KeyError:
                raise NoSectionError(section), None, sys.exc_info()[2]
        sectdict[self.optionxform(option)] = value

    def write(self, fp):
        """Write an .ini-format representation of the configuration state."""
        if self.__defaults:
            fp.write("[%s]\n" % DEFAULTSECT)
            for (key, value) in self.__defaults.items():
                fp.write("%s = %s\n" % (key, string.replace(str(value), '\n', '\n\t')))
            fp.write("\n")
        for section in self.__sections.keys():
            fp.write("[%s]\n" % section)
            for (key, value) in self.__sections[section].items():
                if key != "__name__":
                    fp.write("%s = %s\n" %
                             (key, string.replace(str(value), '\n', '\n\t')))
            fp.write("\n")

    def remove_option(self, section, option):
        """Remove an option."""
        if not section or section == DEFAULTSECT:
            sectdict = self.__defaults
        else:
            try:
                sectdict = self.__sections[section]
            except KeyError:
                raise NoSectionError(section), None, sys.exc_info()[2]
        option = self.optionxform(option)
        existed = sectdict.has_key(option)
        if existed:
            del sectdict[option]
        return existed

    def remove_section(self, section):
        """Remove a file section."""
        existed = self.__sections.has_key(section)
        if existed:
            del self.__sections[section]
        return existed

    #
    # Regular expressions for parsing section headers and options.
    #
    SECTCRE = re.compile(
        r'\['                                 # [
        r'(?P<header>[^]]+)'                  # very permissive!
        r'\]'                                 # ]
        )
    OPTCRE = re.compile(
        r'(?P<option>[^:=\s][^:=]*)'          # very permissive!
        r'\s*(?P<vi>[:=])\s*'                 # any number of space/tab,
                                              # followed by separator
                                              # (either : or =), followed
                                              # by any # space/tab
        r'(?P<value>.*)$'                     # everything up to eol
        )

    def __read(self, fp, fpname):
        """Parse a sectioned setup file.

        The sections in setup file contains a title line at the top,
        indicated by a name in square brackets (`[]'), plus key/value
        options lines, indicated by `name: value' format lines.
        Continuation are represented by an embedded newline then
        leading whitespace.  Blank lines, lines beginning with a '#',
        and just about everything else is ignored.
        """
        cursect = None                            # None, or a dictionary
        optname = None
        lineno = 0
        e = None                                  # None, or an exception
        while 1:
            line = fp.readline()
            if not line:
                break
            lineno = lineno + 1
            # comment or blank line?
            if string.strip(line) == '' or line[0] in '#;':
                continue
            if string.lower(string.split(line, None, 1)[0]) == 'rem' and line[0] in "rR":
                # no leading whitespace
                continue
            # continuation line?
            if isspace(line[0]) and cursect is not None and optname:
                value = string.strip(line)
                if value:
                    cursect[optname] = "%s\n%s" % (cursect[optname], value)
            # a section header or option header?
            else:
                # is it a section header?
                mo = self.SECTCRE.match(line)
                if mo:
                    sectname = mo.group('header')
                    if self.__sections.has_key(sectname):
                        cursect = self.__sections[sectname]
                    elif sectname == DEFAULTSECT:
                        cursect = self.__defaults
                    else:
                        cursect = {'__name__': sectname}
                        self.__sections[sectname] = cursect
                    # So sections can't start with a continuation line
                    optname = None
                # no section header in the file?
                elif cursect is None:
                    raise MissingSectionHeaderError(fpname, lineno, `line`)
                # an option line?
                else:
                    mo = self.OPTCRE.match(line)
                    if mo:
                        optname, vi, optval = mo.group('option', 'vi', 'value')
                        if vi in ('=', ':') and ';' in optval:
                            # ';' is a comment delimiter only if it follows
                            # a spacing character
                            pos = string.find(optval, ';')
                            if pos != -1 and isspace(optval[pos-1]):
                                optval = optval[:pos]
                        optval = string.strip(optval)
                        # allow empty values
                        if optval == '""':
                            optval = ''
                        optname = self.optionxform(string.rstrip(optname))
                        cursect[optname] = optval
                    else:
                        # a non-fatal parsing error occurred.  set up the
                        # exception but keep going. the exception will be
                        # raised at the end of the file and will contain a
                        # list of all bogus lines
                        if not e:
                            e = ParsingError(fpname)
                        e.append(lineno, `line`)
        # if any parsing errors occurred, raise an exception
        if e:
            raise e

def isspace(s):
    return (s in string.whitespace)
