# Copyright (c) 2023 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import base64
import os
from binascii import hexlify

try:
    from Crypto.Hash import SHA256, HMAC
    HAS_HASH = True
except ImportError:
    HAS_HASH = False

try:
    from Crypto.Util import Counter
    HAS_COUNTER = True
except ImportError:
    HAS_COUNTER = False

try:
    from Crypto.Protocol.KDF import PBKDF2
    HAS_PBKDF2 = True
except ImportError:
    HAS_PBKDF2 = False

try:
    from Crypto.Cipher import AES as AES
    HAS_AES = True
except ImportError:
    HAS_AES = False


def to_bytes(obj, encoding='utf-8', errors='strict'):
    """Make sure that a string is a byte string"""
    if isinstance(obj, bytes):
        return obj

    if isinstance(obj, str):
        return obj.encode(encoding, errors)

    raise TypeError('obj must be a string type')

class CryptHelper:

    """
    Openssl compatible implementation of AES 256 CTR PKCS5 padding with PBKDF2
    """

    magic = b'Salted__'

    def __init__(self):
        if not HAS_AES or not HAS_COUNTER or not HAS_PBKDF2 or not HAS_HASH:
            raise ImportError('Missing algorithms')


    @classmethod
    def _generate_key(cls, password, salt):
        # 16 for AES 128, 32 for AES256
        keylength = 32
        ivlength = 16
        hash_function = SHA256

        b_derivedkey = PBKDF2(password, salt, dkLen=keylength + ivlength, count=10000, hmac_hash_module=SHA256)

        b_key = b_derivedkey[:keylength]
        b_iv = b_derivedkey[keylength:(keylength + ivlength)]
        #print("KEY: {}".format(hexlify(b_key1)))
        #print("IV : {}".format(hexlify(b_iv)))

        return b_key, hexlify(b_iv)

    def aes256Encrypt(self, plaintext, password):
        """Encrypt a plain text with a password using AES 256 CTR PKCS5 padding with PBKDF2

        This is compatible with openssl enc command:
            $>  echo "this is a secret text" | openssl enc -aes-256-ctr -pbkdf2 -e -a
        """

        salt = os.urandom(8)
        b_key, b_iv = self._generate_key(password, salt)

        ctr = Counter.new(128, initial_value=int(b_iv, 16))
        cipher = AES.new(b_key, AES.MODE_CTR, counter=ctr)

        b_chiphertext = self.magic + salt + cipher.encrypt(to_bytes(plaintext))

        b_b64chiphertext = base64.b64encode(b_chiphertext)
        return b_b64chiphertext.decode('ascii')

    def aes256Decrypt(self, chipertext, password):
        """Decrypt a cipher text with a password using AES 256 CTR PKCS5 padding with PBKDF2

        This is compatible with openssl enc command:
            $>  echo "this is a secret text" | openssl enc -aes-256-ctr -pbkdf2 -d -a
        """
        b_b64chiphertext = chipertext.encode('ascii')
        b_b64decoded = base64.b64decode(b_b64chiphertext)

        b_magic = b_b64decoded[:8]
        if self.magic != b_magic:
            raise ValueError("Invalid magic")

        b_salt = b_b64decoded[8:16]
        b_ciphertext = b_b64decoded[16:]
        #print("SALT: {}".format(hexlify(b_salt)))

        b_key, b_iv = self._generate_key(password, b_salt)

        ctr = Counter.new(128, initial_value=int(b_iv, 16))
        cipher = AES.new(b_key, AES.MODE_CTR, counter=ctr)

        b_plaintext = cipher.decrypt(b_ciphertext)
        return b_plaintext.decode()

    def pwFromFile(self, path):
        if not os.path.isfile(path):
            raise IOError("File does not exist: {}".format(path))
        password = ""
        with open(path, "r") as fd:
            password = fd.readline()
        if not password:
            raise ValueError("Password could not be read")
        return password


if __name__ == '__main__':
    tmpmpw = "/tmp/testmasterpassword"
    crypt = CryptHelper()
    with open(tmpmpw, "w") as fd:
        fd.write(">Nq[4>.yy7CLIWT(Ye:T4,Q@0b*xPG]R;=B [4+BLjoT4h}O=f35VE]")
    pw = crypt.pwFromFile(tmpmpw)
    encrypted = crypt.aes256Encrypt("this is a clear text", pw)
    print(encrypted)
    print(crypt.aes256Decrypt(encrypted, pw))
    print(crypt.aes256Decrypt("U2FsdGVkX1+61AfD8tW7xn26221yTgfHpheohxC5wk0O9JoFYQ==", "password"))
    os.unlink(tmpmpw)

