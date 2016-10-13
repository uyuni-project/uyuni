/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.utils;

import com.redhat.rhn.domain.server.MinionServer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities for working with machine passwords
 */
public class MachinePasswordUtils {

   private MachinePasswordUtils() {
   }

   /**
    * generate the machine password for a given minion
    *
    * @param minionServer minion
    * @return machine password as hash bytes
    */
   //TODO: move this to a more appropriate place and finalize how the password is generated
   //TODO: evaluate if plain SHA-256 is appropriate for password hashing
   public static byte[] machinePasswordBytes(MinionServer minionServer) {
      try {
         MessageDigest instance = MessageDigest.getInstance("SHA-256");
         instance.update(minionServer.getSecret().getBytes());
         byte[] digest = instance.digest(minionServer.getMachineId().getBytes());
         return digest;
      }
      catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * generate the machine password for a given minion
    *
    * @param minionServer minion
    * @return machine password as hex string
    */
   public static String machinePassword(MinionServer minionServer) {
       return Hex.encodeHexString(machinePasswordBytes(minionServer));
   }

   /**
    * Test the given machine password against the minions machine password
    * This uses a constant time comparison to guard against timeing attacks.
    *
    * @param minionServer minion
    * @param machinePassword machine password as hash byte array
    *
    * @return if the machine password matches that of the given minion
    */
   public static boolean match(MinionServer minionServer, byte[] machinePassword) {
      // Importent to use this constant time equals check for passwords
      return MessageDigest.isEqual(machinePasswordBytes(minionServer), machinePassword);
   }

   /**
    * Test the given machine password against the minions machine password
    * This uses a constant time comparison to guard against timeing attacks.
    *
    * @param minionServer minion
    * @param machinePassword machine password as hex string of bytes
    *
    * @return if the machine password matches that of the given minion
    */
   public static boolean match(MinionServer minionServer, String machinePassword) {
      try {
         byte[] bytes = Hex.decodeHex(machinePassword.toCharArray());
         return match(minionServer, bytes);
      }
      catch (DecoderException e) {
         return false;
      }
   }

}
