/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.salt.custom.coco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CoCoAttestationResponseDataParserTest extends JMockBaseTestCaseWithUser {

    //suppress checkstyle warnings in order to keep it as it appears in the file
    @SuppressWarnings("checkstyle:lineLength")
    private static final String AMD_SALT_STATE_JSON_INPUT_STRING = """
                {
                   "saltutil_|-sync_states_|-sync_states_|-sync_states":{
                      "name":"sync_states",
                      "changes":{
                      },
                      "result":true,
                      "comment":"No updates to sync",
                      "__sls__":"util.syncstates",
                      "__run_num__":0,
                      "start_time":"09:51:03.373782",
                      "duration":104.521,
                      "__id__":"sync_states"
                   },
                   "pkg_|-mgr_absent_ca_package_|-rhn-org-trusted-ssl-cert_|-removed":{
                      "name":"rhn-org-trusted-ssl-cert",
                      "changes":{
                      },
                      "result":true,
                      "comment":"All specified packages are already absent",
                      "__sls__":"certs",
                      "__run_num__":1,
                      "start_time":"09:51:03.992424",
                      "duration":3.079,
                      "__id__":"mgr_absent_ca_package"
                   },
                   "file_|-mgr_ca_cert_|-/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT_|-managed":{
                      "changes":{
                      },
                      "comment":"File /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT is in the correct state",
                      "name":"/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT",
                      "result":true,
                      "__sls__":"certs",
                      "__run_num__":2,
                      "start_time":"09:51:03.996701",
                      "duration":14.323,
                      "__id__":"mgr_ca_cert"
                   },
                   "cmd_|-update-ca-certificates_|-/usr/sbin/update-ca-certificates_|-run":{
                      "changes":{
                      },
                      "result":true,
                      "duration":0.002,
                      "start_time":"09:51:04.011737",
                      "comment":"State was not run because none of the onchanges reqs changed",
                      "__state_ran__":false,
                      "__run_num__":3,
                      "__sls__":"certs",
                      "__id__":"update-ca-certificates",
                      "name":"/usr/sbin/update-ca-certificates"
                   },
                   "file_|-mgr_proxy_ca_cert_symlink_|-/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT_|-symlink":{
                      "result":true,
                      "name":"/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT",
                      "changes":{
                      },
                      "comment":"onlyif condition is false",
                      "__sls__":"certs",
                      "__id__":"mgr_proxy_ca_cert_symlink",
                      "skip_watch":true,
                      "__run_num__":4,
                      "start_time":"09:51:04.011780",
                      "duration":240.799
                   },
                   "file_|-mgr_deploy_tools_uyuni_key_|-/etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d12345w.key_|-managed":{
                      "changes":{
                      },
                      "comment":"File /etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d12345w.key is in the correct state",
                      "name":"/etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d12345w.key",
                      "result":true,
                      "__sls__":"channels.gpg-keys",
                      "__run_num__":5,
                      "start_time":"09:51:04.252657",
                      "duration":14.5,
                      "__id__":"mgr_deploy_tools_uyuni_key"
                   },
                   "file_|-mgr_deploy_suse_addon_key_|-/etc/pki/rpm-gpg/suse-addon-12a345bc6def7ghi.key_|-managed":{
                      "changes":{
                      },
                      "comment":"File /etc/pki/rpm-gpg/suse-addon-12a345bc6def7ghi.key is in the correct state",
                      "name":"/etc/pki/rpm-gpg/suse-addon-12a345bc6def7ghi.key",
                      "result":true,
                      "__sls__":"channels.gpg-keys",
                      "__run_num__":6,
                      "start_time":"09:51:04.267225",
                      "duration":13.196,
                      "__id__":"mgr_deploy_suse_addon_key"
                   },
                   "file_|-mgr_deploy_suse16_gpg_key_|-/etc/pki/rpm-gpg/suse16-gpg-pubkey-98a7bc65.key_|-managed":{
                      "changes":{
                      },
                      "comment":"File /etc/pki/rpm-gpg/suse16-gpg-pubkey-98a7bc65.key is in the correct state",
                      "name":"/etc/pki/rpm-gpg/suse16-gpg-pubkey-98a7bc65.key",
                      "result":true,
                      "__sls__":"channels.gpg-keys",
                      "__run_num__":7,
                      "start_time":"09:51:04.280505",
                      "duration":13.293,
                      "__id__":"mgr_deploy_suse16_gpg_key"
                   },
                   "file_|-mgrchannels_repo_|-/etc/zypp/repos.d/susemanager:channels.repo_|-managed":{
                      "changes":{
                      },
                      "comment":"File /etc/zypp/repos.d/susemanager:channels.repo is in the correct state",
                      "name":"/etc/zypp/repos.d/susemanager:channels.repo",
                      "result":true,
                      "__sls__":"channels",
                      "__run_num__":8,
                      "start_time":"09:51:04.293915",
                      "duration":43.894,
                      "__id__":"mgrchannels_repo"
                   },
                   "product_|-mgrchannels_install_products_|-mgrchannels_install_products_|-all_installed":{
                      "name":"mgrchannels_install_products",
                      "changes":{
                      },
                      "result":true,
                      "comment":"All subscribed products are already installed",
                      "__sls__":"channels",
                      "__run_num__":9,
                      "start_time":"09:51:04.338216",
                      "duration":431.093,
                      "__id__":"mgrchannels_install_products"
                   },
                   "pkg_|-mgrchannels_inst_suse_build_key_|-suse-build-key_|-installed":{
                      "name":"suse-build-key",
                      "changes":{
                      },
                      "result":true,
                      "comment":"All specified packages are already installed",
                      "__sls__":"channels",
                      "__run_num__":10,
                      "start_time":"09:51:04.769675",
                      "duration":1965.43,
                      "__id__":"mgrchannels_inst_suse_build_key"
                   },
                   "file_|-mgr_sevsnp_create_attestdir_|-/tmp/cocoattest_sevsnp_|-directory":{
                       "name":"/tmp/cocoattest_sevsnp",
                       "changes":{
                       },
                       "result":true,
                       "comment":"The directory /tmp/cocoattest_sevsnp is in the correct state",
                       "__sls__":"cocoattest.coco_sev_snp",
                       "__run_num__":11,
                       "start_time":"11:55:16.601550",
                       "duration":0.573,
                       "__id__":"mgr_sevsnp_create_attestdir"
                    },
                   "pkg_|-mgr_sevsnp_inst_snpguest_|-mgr_sevsnp_inst_snpguest_|-latest":{
                       "name":"mgr_sevsnp_inst_snpguest",
                       "changes":{
                       },
                       "result":true,
                       "comment":"Package snpguest is already up-to-date",
                       "__sls__":"cocoattest.coco_sev_snp",
                       "__run_num__":12,
                       "start_time":"11:55:16.602215",
                       "duration":479.856,
                       "__id__":"mgr_sevsnp_inst_snpguest"
                    },
                   "cmd_|-mgr_sevsnp_write_request_data_|-/usr/bin/echo \\"l/BVaYeYPhEhf75K9pxDspw2DYAFdg7Op81zn8a+ql6tlHAG/e8S3cu7oeSSbViaWJv6xUNSEPsr2xBYJSc43g==\\" | /usr/bin/base64 -d > /tmp/cocoattest_sevsnp/random_user_nonce.bin_|-run":{
                       "name":"/usr/bin/echo \\"l/BVaYeYPhEhf75K9pxDspw2DYAFdg7Op81zn8a+ql6tlHAG/e8S3cu7oeSSbViaWJv6xUNSEPsr2xBYJSc43g==\\" | /usr/bin/base64 -d > /tmp/cocoattest_sevsnp/random_user_nonce.bin",
                       "changes":{
                          "pid":10947,
                          "retcode":0,
                          "stdout":"",
                          "stderr":""
                       },
                       "result":true,
                       "comment":"Command \\"/usr/bin/echo \\"l/BVaYeYPhEhf75K9pxDspw2DYAFdg7Op81zn8a+ql6tlHAG/e8S3cu7oeSSbViaWJv6xUNSEPsr2xBYJSc43g==\\" | /usr/bin/base64 -d > /tmp/cocoattest_sevsnp/random_user_nonce.bin\\" run",
                       "__sls__":"cocoattest.coco_sev_snp",
                       "__run_num__":13,
                       "start_time":"11:55:17.082297",
                       "duration":7.369,
                       "__id__":"mgr_sevsnp_write_request_data"
                    },
                   "cmd_|-mgr_sevsnp_create_snpguest_response_|-/usr/bin/snpguest report /tmp/cocoattest_sevsnp/response.bin /tmp/cocoattest_sevsnp/random_user_nonce.bin_|-run":{
                       "name":"/usr/bin/snpguest report /tmp/cocoattest_sevsnp/response.bin /tmp/cocoattest_sevsnp/random_user_nonce.bin",
                       "changes":{
                          "pid":10950,
                          "retcode":0,
                          "stdout":"",
                          "stderr":""
                       },
                       "result":true,
                       "comment":"Command \\"/usr/bin/snpguest report /tmp/cocoattest_sevsnp/response.bin /tmp/cocoattest_sevsnp/random_user_nonce.bin\\" run",
                       "__sls__":"cocoattest.coco_sev_snp",
                       "__run_num__":14,
                       "start_time":"11:55:17.089899",
                       "duration":3.37,
                       "__id__":"mgr_sevsnp_create_snpguest_response"
                    },
                   "cmd_|-mgr_sevsnp_snpguest_response_|-/usr/bin/cat /tmp/cocoattest_sevsnp/response.bin | /usr/bin/base64_|-run":{
                      "name":"/usr/bin/cat /tmp/cocoattest_sevsnp/response.bin | /usr/bin/base64",
                      "changes":{
                         "pid":2744,
                         "retcode":0,
                         "stdout":"BQAAAAAAAAAAAAMCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAEAAAAE\\nAAAAAAAd3icAAAAAAAAABAAAAAAAAACvUTwurA60JuLr4saoUcqnlRByTSye2RoG7cm7aV3GFDM8\\nhBVP1bRVv+xZ+xgMnSxeXtRHKWQWIJeOKWzdHrjKUH6C0n6luVHddlo+sxul9YJnOzAdaYPe1ILT\\n/rBmy2iXnx8R/t6XaHN006JQAqFfAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC0BzK9Zd/WojFnYDcZSyMuFyK38wdR\\nFG1y0mG8SdJ85///////////////////////////////////////////BAAAAAAAHd4ZAQEAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAHd4BOgEAAToBAAQAAAAAAB3eDwAAAAAAAAAP\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAl5RQN5FIiFMGs4Ud\\nFRQ/It0+g1ccaH0sbEgIVvRsxZMs5UwjpgqEwwqXKJ+vWUP3AAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAJFiRN+f4t0/lz6CxHmVe4DEl+YWRZ/U/jacsJCUt7XqpIHqeJUr0u4F4s3NIMTZGAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                         "stderr":""
                      },
                      "result":true,
                      "comment":"Command \\"/usr/bin/cat /tmp/cocoattest_sevsnp/response.bin | /usr/bin/base64\\" run",
                      "__sls__":"cocoattest.coco_sev_snp",
                      "__run_num__":15,
                      "start_time":"09:51:07.123989",
                      "duration":1.806,
                      "__id__":"mgr_sevsnp_snpguest_response"
                   },
                   "cmd_|-mgr_sevsnp_create_vlek_certificate_|-/usr/bin/snpguest certificates PEM /tmp/cocoattest_sevsnp_|-run":{
                      "name":"/usr/bin/snpguest certificates PEM /tmp/cocoattest_sevsnp",
                      "changes":{
                         "pid":2747,
                         "retcode":0,
                         "stdout":"total 4\\ndrwxr-xr-x  2 root root    6 Mar 15  2022 bin\\ndrwxr-xr-x 21 root root 4096 Feb 24 18:29 salt",
                         "stderr":""
                      },
                      "result":true,
                      "comment":"Command \\"/usr/bin/snpguest certificates PEM /tmp/cocoattest_sevsnp\\" run",
                      "__sls__":"cocoattest.coco_sev_snp",
                      "__run_num__":16,
                      "start_time":"09:51:07.125867",
                      "duration":1.497,
                      "__id__":"mgr_sevsnp_create_vlek_certificate"
                   },
                   "cmd_|-mgr_sevsnp_vlek_certificate_|-/usr/bin/cat /tmp/cocoattest_sevsnp/vlek.pem_|-run":{
                      "name":"/usr/bin/cat /tmp/cocoattest_sevsnp/vlek.pem",
                      "changes":{
                         "pid":2748,
                         "retcode":0,
                         "stdout":"-----BEGIN CERTIFICATE-----\\nMIIFIzCCAtegAwIBAgIBADBBBgkqhkiG9w0BAQowNKAPMA0GCWCGSAFlAwQCAgUA\\noRwwGgYJKoZIhvcNAQEIMA0GCWCGSAFlAwQCAgUAogMCATAwgYAxFDASBgNVBAsM\\nC0VuZ2luZWVyaW5nMQswCQYDVQQGEwJVUzEUMBIGA1UEBwwLU2FudGEgQ2xhcmEx\\nCzAJBgNVBAgMAkNBMR8wHQYDVQQKDBZBZHZhbmNlZCBNaWNybyBEZXZpY2VzMRcw\\nFQYDVQQDDA5TRVYtVkxFSy1NaWxhbjAeFw0yNjAzMDkxOTMwMDVaFw0yNzAzMDkx\\nOTMwMDVaMHoxFDASBgNVBAsMC0VuZ2luZWVyaW5nMQswCQYDVQQGEwJVUzEUMBIG\\nA1UEBwwLU2FudGEgQ2xhcmExCzAJBgNVBAgMAkNBMR8wHQYDVQQKDBZBZHZhbmNl\\nZCBNaWNybyBEZXZpY2VzMREwDwYDVQQDDAhTRVYtVkxFSzB2MBAGByqGSM49AgEG\\nBSuBBAAiA2IABE/rO0PxEkKVu5SAX9Fv+h1pF0r+wWNmNO+DLcMENz2IOaqYiS6s\\nwJjXThFjsjIx5mdy42ozh33DIC+b02LmGScVQrREUL0h5KR4Qd5+BTiO+UDBb8Xd\\n/p8rzpTQn+foEKOB8jCB7zAQBgkrBgEEAZx4AQEEAwIBADAUBgkrBgEEAZx4AQIE\\nBxYFTWlsYW4wEQYKKwYBBAGceAEDAQQDAgEEMBEGCisGAQQBnHgBAwIEAwIBADAR\\nBgorBgEEAZx4AQMEBAMCAQAwEQYKKwYBBAGceAEDBQQDAgEAMBEGCisGAQQBnHgB\\nAwYEAwIBADARBgorBgEEAZx4AQMHBAMCAQAwEQYKKwYBBAGceAEDAwQDAgEdMBIG\\nCisGAQQBnHgBAwgEBAICAN4wLAYJKwYBBAGceAEFBB8WHUNOPWNjLXVzLWVhc3Qt\\nMi5hbWF6b25hd3MuY29tMEEGCSqGSIb3DQEBCjA0oA8wDQYJYIZIAWUDBAICBQCh\\nHDAaBgkqhkiG9w0BAQgwDQYJYIZIAWUDBAICBQCiAwIBMAOCAgEAezLRKlKoxDhJ\\n9gGLDoIhruKSWJcwMHPn6E/aJzJvpDaYfb12ACKHyiMsm5htk6+lL9Kse544pmhh\\nUm/hxlpUNXiF+61obfKcaxp11Q2hzC/hTyLYpse6IXskLq6OH+DXzrcw0X30t3YI\\niUqzCGszbeTBy4uUOULxz3XbSUYFsKwsk9oMuo7isOUM3INiMiOTq91THlp6ZSVJ\\nDLIpW8v17DSL7yfJsvyLKsvtL7YjohFsOJe/qr/ttVSLC9WJmnxMxaxtgLSVGwCN\\nTHsx6646R7SAPRVXpdOd4f+Gfj4Dk1eiELs1L85Qm0tChDLrLLO31X9cwPxg6fjd\\nZ3sgqZrOv9Up2EiiX3uzH9pjuek9KZw8BvbNmkABURnj2QVKkANKWeVwJ2OI9xmb\\nKnLDQp+t3Q6gTcdPdcjSsAkT0JijpzamEmIPLdBobgVHAcxCxRhBILmJWQcU8V8R\\ng2+n/Zs8gpuGaCj0j8s8YDq7+sgy0/5CsDgAhU7+4HqzBTPbhOCNAI9uptU6v0xo\\nDLzldXmVJQaGWp6zQ+WB29ZnFrF4UE85+os3uIwc6uEPBjh3bjmhTwa3I7LWRWld\\nRyoJUuLnBIdTJYVIkgrYJ47LfI3akZxUM0D+FpqawemnHOTT1z8ee1wj7wnE6nS2\\nX0R7cJNthweU48At2ZfRIuPYvu9av2Y=\\n-----END CERTIFICATE-----",
                         "stderr":""
                      },
                      "result":true,
                      "comment":"Command \\"/usr/bin/cat /tmp/cocoattest_sevsnp/vlek.pem\\" run",
                      "__sls__":"cocoattest.coco_sev_snp",
                      "__run_num__":17,
                      "start_time":"09:51:07.127515",
                      "duration":1.291,
                      "__id__":"mgr_sevsnp_vlek_certificate"
                   },
                   "pkg_|-mgr_secureboot_inst_mokutil_|-mgr_secureboot_inst_mokutil_|-latest":{
                     "name":"mgr_secureboot_inst_mokutil",
                     "changes":{
                     },
                     "result":true,
                     "comment":"Package mokutil is already up-to-date",
                     "__sls__":"cocoattest.coco_secure_boot",
                     "__run_num__":18,
                     "start_time":"11:55:17.096480",
                     "duration":474.938,
                     "__id__":"mgr_secureboot_inst_mokutil"
                  },
                   "cmd_|-mgr_secureboot_enabled_|-/usr/bin/mokutil --sb-state_|-run":{
                      "name":"/usr/bin/mokutil --sb-state",
                      "changes":{
                         "pid":10978,
                         "retcode":1,
                         "stdout":"",
                         "stderr":"EFI variables are not supported on this system"
                      },
                      "result":false,
                      "comment":"Command \\"/usr/bin/mokutil --sb-state\\" run",
                      "__sls__":"cocoattest.coco_secure_boot",
                      "__run_num__":19,
                      "start_time":"11:55:17.571515",
                      "duration":3.706,
                      "__id__":"mgr_secureboot_enabled"
                   }
                }
                """;

    private static final String AMD_EXPECTED_BASE_64_REPORT_DATA = """
                BQAAAAAAAAAAAAMCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAEAAAAE
                AAAAAAAd3icAAAAAAAAABAAAAAAAAACvUTwurA60JuLr4saoUcqnlRByTSye2RoG7cm7aV3GFDM8
                hBVP1bRVv+xZ+xgMnSxeXtRHKWQWIJeOKWzdHrjKUH6C0n6luVHddlo+sxul9YJnOzAdaYPe1ILT
                /rBmy2iXnx8R/t6XaHN006JQAqFfAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC0BzK9Zd/WojFnYDcZSyMuFyK38wdR
                FG1y0mG8SdJ85///////////////////////////////////////////BAAAAAAAHd4ZAQEAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAHd4BOgEAAToBAAQAAAAAAB3eDwAAAAAAAAAP
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAl5RQN5FIiFMGs4Ud
                FRQ/It0+g1ccaH0sbEgIVvRsxZMs5UwjpgqEwwqXKJ+vWUP3AAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAJFiRN+f4t0/lz6CxHmVe4DEl+YWRZ/U/jacsJCUt7XqpIHqeJUr0u4F4s3NIMTZGAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=""";

    private static final String AMD_EXPECTED_VLEK_CERTIFICATE = """
                -----BEGIN CERTIFICATE-----
                MIIFIzCCAtegAwIBAgIBADBBBgkqhkiG9w0BAQowNKAPMA0GCWCGSAFlAwQCAgUA
                oRwwGgYJKoZIhvcNAQEIMA0GCWCGSAFlAwQCAgUAogMCATAwgYAxFDASBgNVBAsM
                C0VuZ2luZWVyaW5nMQswCQYDVQQGEwJVUzEUMBIGA1UEBwwLU2FudGEgQ2xhcmEx
                CzAJBgNVBAgMAkNBMR8wHQYDVQQKDBZBZHZhbmNlZCBNaWNybyBEZXZpY2VzMRcw
                FQYDVQQDDA5TRVYtVkxFSy1NaWxhbjAeFw0yNjAzMDkxOTMwMDVaFw0yNzAzMDkx
                OTMwMDVaMHoxFDASBgNVBAsMC0VuZ2luZWVyaW5nMQswCQYDVQQGEwJVUzEUMBIG
                A1UEBwwLU2FudGEgQ2xhcmExCzAJBgNVBAgMAkNBMR8wHQYDVQQKDBZBZHZhbmNl
                ZCBNaWNybyBEZXZpY2VzMREwDwYDVQQDDAhTRVYtVkxFSzB2MBAGByqGSM49AgEG
                BSuBBAAiA2IABE/rO0PxEkKVu5SAX9Fv+h1pF0r+wWNmNO+DLcMENz2IOaqYiS6s
                wJjXThFjsjIx5mdy42ozh33DIC+b02LmGScVQrREUL0h5KR4Qd5+BTiO+UDBb8Xd
                /p8rzpTQn+foEKOB8jCB7zAQBgkrBgEEAZx4AQEEAwIBADAUBgkrBgEEAZx4AQIE
                BxYFTWlsYW4wEQYKKwYBBAGceAEDAQQDAgEEMBEGCisGAQQBnHgBAwIEAwIBADAR
                BgorBgEEAZx4AQMEBAMCAQAwEQYKKwYBBAGceAEDBQQDAgEAMBEGCisGAQQBnHgB
                AwYEAwIBADARBgorBgEEAZx4AQMHBAMCAQAwEQYKKwYBBAGceAEDAwQDAgEdMBIG
                CisGAQQBnHgBAwgEBAICAN4wLAYJKwYBBAGceAEFBB8WHUNOPWNjLXVzLWVhc3Qt
                Mi5hbWF6b25hd3MuY29tMEEGCSqGSIb3DQEBCjA0oA8wDQYJYIZIAWUDBAICBQCh
                HDAaBgkqhkiG9w0BAQgwDQYJYIZIAWUDBAICBQCiAwIBMAOCAgEAezLRKlKoxDhJ
                9gGLDoIhruKSWJcwMHPn6E/aJzJvpDaYfb12ACKHyiMsm5htk6+lL9Kse544pmhh
                Um/hxlpUNXiF+61obfKcaxp11Q2hzC/hTyLYpse6IXskLq6OH+DXzrcw0X30t3YI
                iUqzCGszbeTBy4uUOULxz3XbSUYFsKwsk9oMuo7isOUM3INiMiOTq91THlp6ZSVJ
                DLIpW8v17DSL7yfJsvyLKsvtL7YjohFsOJe/qr/ttVSLC9WJmnxMxaxtgLSVGwCN
                THsx6646R7SAPRVXpdOd4f+Gfj4Dk1eiELs1L85Qm0tChDLrLLO31X9cwPxg6fjd
                Z3sgqZrOv9Up2EiiX3uzH9pjuek9KZw8BvbNmkABURnj2QVKkANKWeVwJ2OI9xmb
                KnLDQp+t3Q6gTcdPdcjSsAkT0JijpzamEmIPLdBobgVHAcxCxRhBILmJWQcU8V8R
                g2+n/Zs8gpuGaCj0j8s8YDq7+sgy0/5CsDgAhU7+4HqzBTPbhOCNAI9uptU6v0xo
                DLzldXmVJQaGWp6zQ+WB29ZnFrF4UE85+os3uIwc6uEPBjh3bjmhTwa3I7LWRWld
                RyoJUuLnBIdTJYVIkgrYJ47LfI3akZxUM0D+FpqawemnHOTT1z8ee1wj7wnE6nS2
                X0R7cJNthweU48At2ZfRIuPYvu9av2Y=
                -----END CERTIFICATE-----""";

    private static final String AMD_EXPECTED_SECURE_BOOT_RESULT = "EFI variables are not supported on this system";


    //suppress checkstyle warnings in order to keep it as it appears in the file
    @SuppressWarnings("checkstyle:lineLength")
    private static final String IBM_SALT_STATE_JSON_INPUT_STRING = """
            {
              "file_|-mgr_ibmpvattest_create_attestdir_|-/tmp/cocoattest_ibmpvattest_|-directory":{
                   "name":"/tmp/cocoattest_ibmpvattest",
                   "changes":{
                   },
                   "result":true,
                   "comment":"The directory /tmp/cocoattest_ibmpvattest is in the correct state",
                   "__sls__":"cocoattest.coco_ibm_pvattest",
                   "__run_num__":11,
                   "start_time":"11:55:16.368794",
                   "duration":0.561,
                   "__id__":"mgr_ibmpvattest_create_attestdir"
                },
                "pkg_|-mgr_ibmpvattest_inst_pvattest_|-mgr_ibmpvattest_inst_pvattest_|-latest":{
                   "name":"mgr_ibmpvattest_inst_pvattest",
                   "changes":{
                   },
                   "result":true,
                   "comment":"Package s390-tools is already up-to-date",
                   "__sls__":"cocoattest.coco_ibm_pvattest",
                   "__run_num__":12,
                   "start_time":"11:55:16.369441",
                   "duration":476.87,
                   "__id__":"mgr_ibmpvattest_inst_pvattest"
                },

               "cmd_|-mgr_ibmpvattest_write_attestation_request_|-/usr/bin/echo \\"cHZhdHRlc3QAAAEAAAAB0AAAAAAAAAAAAAABkAAAAEAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAZCJFE9dEsHIrsyuUjIAAAAAAAAAAAAAAAEAAAAAAAAAUHAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAADBQUSlP2Iq5u6Qn18quEHeU3DuLvPqxP2iKouQ63R3DIPsRHgpaHFG29fcV16PEkpHYJwPTJN0BRI/zCoNdWKz+gAAAAAAAAAAAAAAAAAAAAktF96/6YqRQEIProQTLG2WpSub2f/eqOsigaYq2KYjMopmVNK3iJaXY2AnBk3viMngesYo6yIuzQRgQQvKp/SV0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDy7mpjygjWJEgBR/epItEP8w/QyEZaHGtWF3gmL6s3r3J6AvPyQB0ZywzZfYwnCXNJ2vUnOMUtMcPvHh69fuyZ1vpsJ7Be57u6vgZzpu1hyZn4xDbIx4eyS64bjh8Lk3Pqv1sbVduTd02xPavw+B+TviLiasIRZH/6oTMWvBVyM/1QwcFQkieV+OTgtjd7ePhI=\\" | /usr/bin/base64 -d > /tmp/cocoattest_ibmpvattest/attestation_request.bin_|-run":{
                  "name":"/usr/bin/echo \\"cHZhdHRlc3QAAAEAAAAB0AAAAAAAAAAAAAABkAAAAEAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAZCJFE9dEsHIrsyuUjIAAAAAAAAAAAAAAAEAAAAAAAAAUHAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAADBQUSlP2Iq5u6Qn18quEHeU3DuLvPqxP2iKouQ63R3DIPsRHgpaHFG29fcV16PEkpHYJwPTJN0BRI/zCoNdWKz+gAAAAAAAAAAAAAAAAAAAAktF96/6YqRQEIProQTLG2WpSub2f/eqOsigaYq2KYjMopmVNK3iJaXY2AnBk3viMngesYo6yIuzQRgQQvKp/SV0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDy7mpjygjWJEgBR/epItEP8w/QyEZaHGtWF3gmL6s3r3J6AvPyQB0ZywzZfYwnCXNJ2vUnOMUtMcPvHh69fuyZ1vpsJ7Be57u6vgZzpu1hyZn4xDbIx4eyS64bjh8Lk3Pqv1sbVduTd02xPavw+B+TviLiasIRZH/6oTMWvBVyM/1QwcFQkieV+OTgtjd7ePhI=\\" | /usr/bin/base64 -d > /tmp/cocoattest_ibmpvattest/attestation_request.bin",
                  "changes":{
                     "pid":30764,
                     "retcode":0,
                     "stdout":"",
                     "stderr":""
                  },
                  "result":true,
                  "comment":"Command \\"/usr/bin/echo \\"cHZhdHRlc3QAAAEAAAAB0AAAAAAAAAAAAAABkAAAAEAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAZCJFE9dEsHIrsyuUjIAAAAAAAAAAAAAAAEAAAAAAAAAUHAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAADBQUSlP2Iq5u6Qn18quEHeU3DuLvPqxP2iKouQ63R3DIPsRHgpaHFG29fcV16PEkpHYJwPTJN0BRI/zCoNdWKz+gAAAAAAAAAAAAAAAAAAAAktF96/6YqRQEIProQTLG2WpSub2f/eqOsigaYq2KYjMopmVNK3iJaXY2AnBk3viMngesYo6yIuzQRgQQvKp/SV0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDy7mpjygjWJEgBR/epItEP8w/QyEZaHGtWF3gmL6s3r3J6AvPyQB0ZywzZfYwnCXNJ2vUnOMUtMcPvHh69fuyZ1vpsJ7Be57u6vgZzpu1hyZn4xDbIx4eyS64bjh8Lk3Pqv1sbVduTd02xPavw+B+TviLiasIRZH/6oTMWvBVyM/1QwcFQkieV+OTgtjd7ePhI=\\"  | /usr/bin/base64 -d > /tmp/cocoattest_ibmpvattest/attestation_request.bin\\" run",
                  "__sls__":"cocoattest.coco_ibm_pvattest",
                  "__run_num__":13,
                  "start_time":"11:05:12.377776",
                  "duration":8.077,
                  "__id__":"mgr_ibmpvattest_write_attestation_request"
               },
               "cmd_|-mgr_ibmpvattest_create_pvattest_response_|-/usr/bin/pvattest perform -i /tmp/cocoattest_ibmpvattest/attestation_request.bin -o /tmp/cocoattest_ibmpvattest/attestation_response.bin_|-run":{
                  "name":"/usr/bin/pvattest perform -i /tmp/cocoattest_ibmpvattest/attestation_request.bin -o /tmp/cocoattest_ibmpvattest/attestation_response.bin",
                  "changes":{
                     "pid":30771,
                     "retcode":0,
                     "stdout":"Send the attestation request to the Ultravisor.\\n\\nRun a measurement of this system through ’/dev/uv’. This device must be accessible and the\\nattestation Ultravisor facility must be present. The input must be an attestation request created\\nwith ’pvattest create’. Output will contain the original request and the response from the\\nUltravisor.\\n\\nUsage: pvattest perform [OPTIONS]\\n\\nOptions:\\n  -u, --user-data <File>\\n          Provide up to 256 bytes of user input\\n          \\n          User-data is arbitrary user-defined data appended to the Attestation measurement. It is\\n          verified during the Attestation measurement verification. May be any arbitrary data, as\\n          long as it is less or equal to 256 bytes\\n\\n  -h, --help\\n          Print help (see a summary with '-h')\\n\\n  -q, --quiet...\\n          Provide less output\\n\\n  -v, --verbose...\\n          Provide more detailed output",
                     "stderr":""
                  },
                  "result":true,
                  "comment":"Command \\"/usr/bin/pvattest perform -i /tmp/cocoattest_ibmpvattest/attestation_request.bin -o /tmp/cocoattest_ibmpvattest/attestation_response.bin\\" run",
                  "__sls__":"cocoattest.coco_ibm_pvattest",
                  "__run_num__":14,
                  "start_time":"11:55:16.854371",
                  "duration":5.646,
                  "__id__":"mgr_ibmpvattest_create_pvattest_response"
               },
               "cmd_|-mgr_ibmpvattest_pvattest_response_|-/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64_|-run":{
                  "name":"/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64",
                  "changes":{
                     "pid":30772,
                     "retcode":0,
                     "stdout":"cHZhdHRlc3QAAAEAAAACjgAAAAAAAAAAAAABkAAAAEAAAABAAAAB0AAAAEAAAAIQAAAALgAAAlAA\\nAAAQAAACfgAAAAAAAAAAAAABAAAAAZCz8o95xyTM/kw2NpEAAAAAAAAAAAAAAAEAAAAAAAAAUHAA\\nAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAFvqBTUY6O9+2KbhNpR5fcQCQgucHom6tR9JYnm\\n+ZAH/Wf6+ikMpFZbkdFbLZ1ghGzbvvZsb4PMznaJJlEaI+KP8gAAAAAAAAAAAAAAAAAAAAaISFiz\\nbu+oqU8lxJuPX+4byOUxYM5nI+8D6UBZYncOqefx9bc9FQRbc8PFjs/BR4sADEmh29D1xSaTJhrE\\nUjHk0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDyulCLYGJHPxM7XnSDNDjR9mIqNKn4b\\n/9KtXp1yvZNtImaxOY/Y+cM2ECxVNK2c1/2/G7BpkNzfrnugTvIX/TTXHCHNOkwyl0iqpPI+r9dO\\nKVa1McWfnOuGTGsVj7DRVwFeWELRTTAtS3Cr1TPHkTpEcdltCrKqPpyFPrJ/kL0ok+t7WSb+CbyW\\nyQWTIZLcx3IIBMiN2p4+GOGFKHhjGmz5Nf3jEX2bmBeu8qmXt/8//x0avZGO0gc5UXx/+X7rwra3\\nZJkggHZl3pGRFpkGn8Bd0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDzR9yjv2Z4dplDn\\nWFEmIaZfndjXfY00sdu7VpBOs3O4PHJhbmRvbSB1c2VyIGRhdGEgZm9yIHN0YW5kYXJkX2F0dGVt\\ncHRfNV8wM18xNgo+uRJTljTNzmYKuEAgkZDi",
                     "stderr":""
                  },
                  "result":true,
                  "comment":"Command \\"/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64\\" run",
                  "__sls__":"cocoattest.coco_ibm_pvattest",
                  "__run_num__":16,
                  "start_time":"11:05:12.398814",
                  "duration":2.228,
                  "__id__":"mgr_ibmpvattest_pvattest_response"
               }
            }
            """;

    private static final String IBM_EXPECTED_BASE_64_REPORT_DATA = """
            cHZhdHRlc3QAAAEAAAACjgAAAAAAAAAAAAABkAAAAEAAAABAAAAB0AAAAEAAAAIQAAAALgAAAlAA
            AAAQAAACfgAAAAAAAAAAAAABAAAAAZCz8o95xyTM/kw2NpEAAAAAAAAAAAAAAAEAAAAAAAAAUHAA
            AAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAFvqBTUY6O9+2KbhNpR5fcQCQgucHom6tR9JYnm
            +ZAH/Wf6+ikMpFZbkdFbLZ1ghGzbvvZsb4PMznaJJlEaI+KP8gAAAAAAAAAAAAAAAAAAAAaISFiz
            bu+oqU8lxJuPX+4byOUxYM5nI+8D6UBZYncOqefx9bc9FQRbc8PFjs/BR4sADEmh29D1xSaTJhrE
            UjHk0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDyulCLYGJHPxM7XnSDNDjR9mIqNKn4b
            /9KtXp1yvZNtImaxOY/Y+cM2ECxVNK2c1/2/G7BpkNzfrnugTvIX/TTXHCHNOkwyl0iqpPI+r9dO
            KVa1McWfnOuGTGsVj7DRVwFeWELRTTAtS3Cr1TPHkTpEcdltCrKqPpyFPrJ/kL0ok+t7WSb+CbyW
            yQWTIZLcx3IIBMiN2p4+GOGFKHhjGmz5Nf3jEX2bmBeu8qmXt/8//x0avZGO0gc5UXx/+X7rwra3
            ZJkggHZl3pGRFpkGn8Bd0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDzR9yjv2Z4dplDn
            WFEmIaZfndjXfY00sdu7VpBOs3O4PHJhbmRvbSB1c2VyIGRhdGEgZm9yIHN0YW5kYXJkX2F0dGVt
            cHRfNV8wM18xNgo+uRJTljTNzmYKuEAgkZDi""";

    private CoCoAttestationResponseDataParser responseDataParser;
    private Optional<CoCoAbstractAttestationResponseData> optAmdChunk;
    private Optional<CoCoAbstractAttestationResponseData> optBootChunk;
    private Optional<CoCoAbstractAttestationResponseData> optIbmChunk;

    private void setUpAmdTest() {
        JsonElement jsonResult = JsonParser.parseString(AMD_SALT_STATE_JSON_INPUT_STRING);
        responseDataParser = new CoCoAttestationResponseDataParser();
        responseDataParser.parse(jsonResult);
        optAmdChunk = responseDataParser.getChunk(CoCoAmdEpycAttestationResponseData.class);
        optBootChunk = responseDataParser.getChunk(CoCoSecureBootAttestationResponseData.class);
        optIbmChunk = responseDataParser.getChunk(CoCoIbmZAttestationResponseData.class);
    }

    private void setUpIbmTest() {
        JsonElement jsonResult = JsonParser.parseString(IBM_SALT_STATE_JSON_INPUT_STRING);
        responseDataParser = new CoCoAttestationResponseDataParser();
        responseDataParser.parse(jsonResult);
        optAmdChunk = responseDataParser.getChunk(CoCoAmdEpycAttestationResponseData.class);
        optBootChunk = responseDataParser.getChunk(CoCoSecureBootAttestationResponseData.class);
        optIbmChunk = responseDataParser.getChunk(CoCoIbmZAttestationResponseData.class);
    }

    @Test
    @DisplayName("check that parse() method is giving the expected results with AMD json result")
    public void testAmdTestParsing() {
        setUpAmdTest();

        assertTrue(optAmdChunk.isPresent());
        CoCoAmdEpycAttestationResponseData amdChunk = (CoCoAmdEpycAttestationResponseData)optAmdChunk.get();
        assertTrue(optBootChunk.isPresent());
        CoCoSecureBootAttestationResponseData bootChunk = (CoCoSecureBootAttestationResponseData)optBootChunk.get();
        assertTrue(optIbmChunk.isPresent());
        CoCoIbmZAttestationResponseData ibmChunk = (CoCoIbmZAttestationResponseData)optIbmChunk.get();

        Optional<StateApplyResult<CmdResult>> optResult;

        optResult = responseDataParser.getResult(CoCoAmdEpycAttestationResponseData.SNP_GUEST_RESPONSE_TAG);
        assertTrue(optResult.isPresent());
        assertEquals(AMD_EXPECTED_BASE_64_REPORT_DATA, optResult.get().getChanges().getStdout());
        assertTrue(amdChunk.getSnpguestResponse().isPresent());
        assertEquals(AMD_EXPECTED_BASE_64_REPORT_DATA, amdChunk.getSnpguestResponse().get().getChanges().getStdout());

        optResult = responseDataParser.getResult(CoCoAmdEpycAttestationResponseData.VLEK_CERTIFICATE_TAG);
        assertTrue(optResult.isPresent());
        assertEquals(AMD_EXPECTED_VLEK_CERTIFICATE, optResult.get().getChanges().getStdout());
        assertTrue(amdChunk.getVlekCertificate().isPresent());
        assertEquals(AMD_EXPECTED_VLEK_CERTIFICATE, amdChunk.getVlekCertificate().get().getChanges().getStdout());

        optResult = responseDataParser.getResult(CoCoSecureBootAttestationResponseData.SECURE_BOOT_ENABLED_TAG);
        assertTrue(optResult.isPresent());
        assertEquals(AMD_EXPECTED_SECURE_BOOT_RESULT, optResult.get().getChanges().getStderr());
        assertTrue(bootChunk.getSecureBoot().isPresent());
        assertEquals(AMD_EXPECTED_SECURE_BOOT_RESULT, bootChunk.getSecureBoot().get().getChanges().getStderr());

        optResult = responseDataParser.getResult(CoCoIbmZAttestationResponseData.PVATTEST_RESPONSE_TAG);
        assertTrue(optResult.isEmpty());
    }

    @Test
    @DisplayName("check that asMap() method is giving the expected results with AMD json result")
    public void testAmdAsMap() {
        setUpAmdTest();

        assertTrue(optAmdChunk.isPresent());
        CoCoAmdEpycAttestationResponseData amdChunk = (CoCoAmdEpycAttestationResponseData)optAmdChunk.get();
        assertTrue(optBootChunk.isPresent());
        CoCoSecureBootAttestationResponseData bootChunk = (CoCoSecureBootAttestationResponseData)optBootChunk.get();
        assertTrue(optIbmChunk.isPresent());
        CoCoIbmZAttestationResponseData ibmChunk = (CoCoIbmZAttestationResponseData)optIbmChunk.get();

        Map<String, Object> requestDataMap = responseDataParser.asMap();
        assertEquals(3, requestDataMap.size());
        assertEquals(AMD_EXPECTED_BASE_64_REPORT_DATA.replace("\n", ""),
                requestDataMap.get(CoCoAmdEpycAttestationResponseData.SNP_GUEST_RESPONSE_TAG));
        assertEquals(AMD_EXPECTED_VLEK_CERTIFICATE,
                requestDataMap.get(CoCoAmdEpycAttestationResponseData.VLEK_CERTIFICATE_TAG));
        assertEquals(AMD_EXPECTED_SECURE_BOOT_RESULT,
                requestDataMap.get(CoCoSecureBootAttestationResponseData.SECURE_BOOT_ENABLED_TAG));

        assertEquals(2, amdChunk.asMap().size());
        assertEquals(AMD_EXPECTED_BASE_64_REPORT_DATA.replace("\n", ""),
                amdChunk.asMap().get(CoCoAmdEpycAttestationResponseData.SNP_GUEST_RESPONSE_TAG));
        assertEquals(AMD_EXPECTED_VLEK_CERTIFICATE,
                amdChunk.asMap().get(CoCoAmdEpycAttestationResponseData.VLEK_CERTIFICATE_TAG));

        assertEquals(1, bootChunk.asMap().size());
        assertEquals(AMD_EXPECTED_SECURE_BOOT_RESULT,
                bootChunk.asMap().get(CoCoSecureBootAttestationResponseData.SECURE_BOOT_ENABLED_TAG));

        assertEquals(0, ibmChunk.asMap().size());
    }

    @Test
    @DisplayName("check that parse() method is giving the expected results with IBM json result")
    public void testIbmParsing() {
        setUpIbmTest();

        assertTrue(optAmdChunk.isPresent());
        CoCoAmdEpycAttestationResponseData amdChunk = (CoCoAmdEpycAttestationResponseData)optAmdChunk.get();
        assertTrue(optBootChunk.isPresent());
        CoCoSecureBootAttestationResponseData bootChunk = (CoCoSecureBootAttestationResponseData)optBootChunk.get();
        assertTrue(optIbmChunk.isPresent());
        CoCoIbmZAttestationResponseData ibmChunk = (CoCoIbmZAttestationResponseData)optIbmChunk.get();

        Optional<StateApplyResult<CmdResult>> optResult;

        optResult = responseDataParser.getResult(CoCoAmdEpycAttestationResponseData.SNP_GUEST_RESPONSE_TAG);
        assertTrue(optResult.isEmpty());

        optResult = responseDataParser.getResult(CoCoAmdEpycAttestationResponseData.VLEK_CERTIFICATE_TAG);
        assertTrue(optResult.isEmpty());

        optResult = responseDataParser.getResult(CoCoSecureBootAttestationResponseData.SECURE_BOOT_ENABLED_TAG);
        assertTrue(optResult.isEmpty());

        optResult = responseDataParser.getResult(CoCoIbmZAttestationResponseData.PVATTEST_RESPONSE_TAG);
        assertTrue(optResult.isPresent());
        assertEquals(IBM_EXPECTED_BASE_64_REPORT_DATA, optResult.get().getChanges().getStdout());
        assertTrue(ibmChunk.getPvattestResponse().isPresent());
        assertEquals(IBM_EXPECTED_BASE_64_REPORT_DATA, ibmChunk.getPvattestResponse().get().getChanges().getStdout());
    }

    @Test
    @DisplayName("check that asMap() method is giving the expected results with IBM json result")
    public void testIbmAsMap() {
        setUpIbmTest();

        assertTrue(optAmdChunk.isPresent());
        CoCoAmdEpycAttestationResponseData amdChunk = (CoCoAmdEpycAttestationResponseData)optAmdChunk.get();
        assertTrue(optBootChunk.isPresent());
        CoCoSecureBootAttestationResponseData bootChunk = (CoCoSecureBootAttestationResponseData)optBootChunk.get();
        assertTrue(optIbmChunk.isPresent());
        CoCoIbmZAttestationResponseData ibmChunk = (CoCoIbmZAttestationResponseData)optIbmChunk.get();

        Map<String, Object> requestDataMap = responseDataParser.asMap();
        assertEquals(1, requestDataMap.size());
        assertEquals(IBM_EXPECTED_BASE_64_REPORT_DATA.replace("\n", ""),
                requestDataMap.get(CoCoIbmZAttestationResponseData.PVATTEST_RESPONSE_TAG));

        assertEquals(0, amdChunk.asMap().size());

        assertEquals(0, bootChunk.asMap().size());

        assertEquals(1, ibmChunk.asMap().size());
        assertEquals(IBM_EXPECTED_BASE_64_REPORT_DATA.replace("\n", ""),
                ibmChunk.asMap().get(CoCoIbmZAttestationResponseData.PVATTEST_RESPONSE_TAG));
    }


    private static class TestResponseData extends CoCoAbstractAttestationResponseData {

        @SerializedName("cmd_|-test_command_|-/usr/bin/cat binary_file.bin | /usr/bin/base64_|-run")
        private StateApplyResult<CmdResult> base64Binary;

        @Override
        public Map<String, Optional<StateApplyResult<CmdResult>>> getResults() {
            Map<String, Optional<StateApplyResult<CmdResult>>> out = new HashMap<>();
            out.put("base64BinaryTag", Optional.ofNullable(base64Binary));
            return out;
        }
    }


    private static class TestDataParser extends CoCoAttestationResponseDataParser {
        @Override
        public void parse(JsonElement jsonResult) {
            chunks.clear();
            chunks.add(Json.GSON.fromJson(jsonResult, TestResponseData.class));
        }
    }

    //suppress checkstyle warnings in order to keep it as it appears in the file
    //original binary file represented in stdout: 864 bytes
    //original base64 string in stdout: 15 rows x 76 chars + 12 chars = 1152 chars + 15 crlf = 1167
    @SuppressWarnings("checkstyle:lineLength")
    private static final String BASE64_BIN_FILE_EXAMPLE = """
            {
            "cmd_|-test_command_|-/usr/bin/cat binary_file.bin | /usr/bin/base64_|-run":{
                  "name":"/usr/bin/cat binary_file.bin | /usr/bin/base64",
                  "changes":{
                     "pid":20446,
                     "retcode":0,
                     "stdout":"cHZhdHRlc3QAAAEAAAADYAAAAAAAAAAAAAABkAAAAEAAAABAAAAB0AAAAEAAAAIQAAABAAAAAlAA\\nAAAQAAADUAAAAAAAAAAAAAABAAAAAZBBLTgZS4052sfgucEAAAAAAAAAAAAAAAEAAAAAAAAAUHAA\\nAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAACKt+uyUssDiTYXy+KNpqaUhsfNO0U2wrl82kDQ\\nHFhlf6SRoRiqfY4PTqmU1ufYx/HL6UI6nWDszrnV1Yr6fHhdAwAAAAAAAAAAAAAAAAAAAWuCp/Xk\\nqA/YiEtOaWsT3jD4BNJM6e+DWJfEKKUPEMpyW69KU9tvrXRqGcKUzpxCiK/VzHIWt4Vb5qjGCjl2\\nH0AT0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDzL84FaRMxHnRGKNBKKRofcMv+hXDIE\\nOdM5sXOEn7EkuH2TAQP+j0FnxBvPQCOjVNBbgPmqXa9AGYcsU/PNZROEbQhxPYGO+3LBg8iQEfev\\nE2tL2Rot4Ete/DsmUZed/0lGI/79Ke7BMNB26nhGNNJ2X1NHWsHpux2wuRT+Cn2TMRCDWVfLXdLv\\n/3YI59qYvihf/FYwkFbPucBeg/tfRxnPkh7OAZy7bK/vilksUrxWFmthKsyw5z4KSBGeWmoW2+wc\\n6Og6acBXWyO+rTPUVXOv0fco79meHaZQ51hRJiGmX53Y132NNLHbu1aQTrNzuDzR9yjv2Z4dplDn\\nWFEmIaZfndjXfY00sdu7VpBOs3O4PGF2570RMsTUC2HOjn7iX1pwO5LHBJfR4JmiR2NZbx+15bo4\\nUI/av+oKuznJEqY/zgRuxofzfdU/WlgpN/AFR75RPqeNWfrpV3Ep4mjEaCW3H4+rM1Y3cGlB7yxe\\nnYQT6M+qpWk6NH6rakoJN9gelv/JXCgF5gMR8t8Bq2ZiK6SKwkJVEuxv6m74RiYIONIKprrPz5SR\\nOBFGWdUfRCHGA5LvGcxJL3mrt0lwcRuNeatFIRYjdxn8OLg9lFehhvx+KMXmy4oarU5rhR4SVjSf\\nW7w+0c4Ihinkd8CwLdIDvmAjMBwRyRRtPe/0o+X2206E3JodtS6V5OJ5nXk+ApLpVXau7LVvIc1f\\n6BDXLViQLYAJ",
                     "stderr":""
                  },
                  "result":true,
                  "comment":"Command \\"/usr/bin/cat binary_file.bin | /usr/bin/base64\\" run",
                  "__sls__":"cocoattest.test_file",
                  "__run_num__":16,
                  "start_time":"11:48:13.072596",
                  "duration":2.811,
                  "__id__":"test_command"
               }
            }
            """;

    @Test
    @DisplayName("check that asMap() method correctly gets binary files in base64 format")
    public void testBase64Encoding() {
        JsonElement jsonResult = JsonParser.parseString(BASE64_BIN_FILE_EXAMPLE);
        responseDataParser = new TestDataParser();
        responseDataParser.parse(jsonResult);

        Optional<StateApplyResult<CmdResult>> optResult = responseDataParser.getResult("base64BinaryTag");
        assertTrue(optResult.isPresent());

        //original binary file represented in stdout: 864 bytes
        //original base64 string in stdout: 15 rows x 76 chars + 12 chars = 1152 chars + 15 crlf = 1167
        StateApplyResult<CmdResult> res = optResult.get();
        String parsedBase64BinaryFromStdout = res.getChanges().getStdout();

        assertEquals(1167, parsedBase64BinaryFromStdout.length());
        assertEquals(15, StringUtils.countMatches(parsedBase64BinaryFromStdout, "\n"));
        assertEquals(1152, parsedBase64BinaryFromStdout.replace("\n", "").length());
        byte[] decoded = Base64.getDecoder().decode(parsedBase64BinaryFromStdout.replace("\n", ""));
        assertEquals(864, decoded.length);

        //asMap() has no cr/lf
        Map<String, Object> requestDataMap = responseDataParser.asMap();
        assertTrue(requestDataMap.containsKey("base64BinaryTag"));
        String parsedBase64BinaryFromAsMap = (String)requestDataMap.get("base64BinaryTag");

        assertEquals(1152, parsedBase64BinaryFromAsMap.length());
        assertEquals(0, StringUtils.countMatches(parsedBase64BinaryFromAsMap, "\n"));
        byte[] decodedAsMap = Base64.getDecoder().decode(parsedBase64BinaryFromAsMap);
        assertEquals(864, decodedAsMap.length);
    }

    //suppress checkstyle warnings in order to keep it as it appears in the file
    @SuppressWarnings("checkstyle:lineLength")
    private static final String IBM_SALT_STATE_JSON_INPUT_ERROR = """
            {
            "cmd_|-mgr_ibmpvattest_pvattest_response_|-/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64_|-run":{
                  "name":"/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64",
                  "changes":{
                     "pid":16811,
                     "retcode":0,
                     "stdout":"",
                     "stderr":"/usr/bin/cat: /tmp/cocoattest_ibmpvattest/attestation_response.bin: No such file or directory"
                  },
                  "result":true,
                  "comment":"Command \\"/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64\\" run",
                  "__sls__":"cocoattest.coco_ibm_pvattest",
                  "__run_num__":15,
                  "start_time":"09:47:03.936832",
                  "duration":2.696,
                  "__id__":"mgr_ibmpvattest_pvattest_response"
               }
            }
            """;

    @Test
    @DisplayName("check that asMap() method correctly handles errors when using base64 format")
    public void testBase64ErrorsEncoding() {
        JsonElement jsonResult = JsonParser.parseString(IBM_SALT_STATE_JSON_INPUT_ERROR);
        responseDataParser = new CoCoAttestationResponseDataParser();
        responseDataParser.parse(jsonResult);

        Map<String, Object> requestDataMap = responseDataParser.asMap();
        assertEquals(1, requestDataMap.size());
        assertEquals("", requestDataMap.get(CoCoIbmZAttestationResponseData.PVATTEST_RESPONSE_TAG));
    }

}
