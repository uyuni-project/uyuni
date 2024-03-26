import pytest
from unittest.mock import patch
from ..modules import appstreams
from ..modules.appstreams import _parse_nsvca, _get_module_info, _get_enabled_module_names, get_enabled_modules
from collections import namedtuple

MockDNFCommandResult = namedtuple("MockObject", ["returncode", "stdout"])

@pytest.mark.parametrize("module_info_output, expected_result", [
    (["Name             : maven",
      "Stream           : 3.8 [e] [a]",
      "Version          : 9020020230511160017",
      "Context          : 4b0b4b45",
      "Architecture     : x86_64"], 
     {"name": "maven", "stream": "3.8", "version": "9020020230511160017", "context": "4b0b4b45", "architecture": "x86_64"}),
    
    (["Name             : ruby",
      "Stream           : 3.1 [e] [a]",
      "Version          : 9010020221119221509",
      "Context          : 8d1baf64",
      "Architecture     : x86_64"], 
     {"name": "ruby", "stream": "3.1", "version": "9010020221119221509", "context": "8d1baf64", "architecture": "x86_64"}),

    (["Context          : 8d1baf64",
      "Architecture     : x86_64"], 
     None),

    ([], None)
])
def test_parse_nsvca(module_info_output, expected_result):
    assert _parse_nsvca(module_info_output) == expected_result


sample_maven_ruby_module_info_result = """
Name             : maven
Stream           : 3.8 [e] [a]
Version          : 9020020230511160017
Context          : 4b0b4b45
Architecture     : x86_64
Profiles         : common [d]
Default profiles : common
Repo             : susemanager:rockylinux-9-appstream-x86_64
Summary          : Java project management and project comprehension tool
Description      : Maven is a software project management and comprehension tool. Based on the concept of a project object model (POM), Maven can manage a project's build, reporting and documentation from a central piece of information.
Requires         : platform:[el9]
Artifacts        : apache-commons-cli-0:1.5.0-4.module+el9.2.0+14755+4b0b4b45.noarch
                 : apache-commons-cli-0:1.5.0-4.module+el9.2.0+14755+4b0b4b45.src

Name             : ruby
Stream           : 3.1 [e] [a]
Version          : 9010020221119221509
Context          : 8d1baf64
Architecture     : x86_64
Profiles         : common [d]
Default profiles : common
Repo             : susemanager:rockylinux-9-appstream-x86_64
Summary          : An interpreter of object-oriented scripting language
Description      : Ruby is the interpreted scripting language for quick and easy object-oriented programming.  It has many features to process text files and to do system management tasks (as in Perl).  It is simple, straight-forward, and extensible.
Requires         : 
Artifacts        : ruby-0:3.1.2-141.module+el9.1.0+13172+8d1baf64.i686
                 : ruby-0:3.1.2-141.module+el9.1.0+13172+8d1baf64.src
"""

def test_get_module_info():
    module_names = ["maven", "ruby"]
    mocked_command_result = MockDNFCommandResult(
        returncode=0,
        stdout=sample_maven_ruby_module_info_result
    )
    expected_result = [
        {"name": "maven", "stream": "3.8", "version": "9020020230511160017", "context": "4b0b4b45", "architecture": "x86_64"},
        {"name": "ruby", "stream": "3.1", "version": "9010020221119221509", "context": "8d1baf64", "architecture": "x86_64"}
    ]
    with patch("subprocess.run", return_value=mocked_command_result):
        assert _get_module_info(module_names) == expected_result


sample_dnf_enabled_modules_result = """rockylinux-9-appstream for x86_64
Name                        Stream                        Profiles                                                  Summary                                                                   
maven                       3.8 [e]                       common [d]                                                Java project management and project comprehension tool                    
nginx                       1.22 [e]                      common [d]                                                nginx webserver                                                           
nodejs                      18 [e]                        common [d], development, minimal, s2i                     Javascript runtime                                                        
ruby                        3.1 [e]                       common [d]                                                An interpreter of object-oriented scripting language
"""

def test_get_enabled_module_names():
    mocked_command_result = MockDNFCommandResult(
        returncode=0,
        stdout=sample_dnf_enabled_modules_result
    )
    expected_result = ["maven", "nginx", "nodejs", "ruby"]
    with patch("subprocess.run", return_value=mocked_command_result):
        assert _get_enabled_module_names() == expected_result
