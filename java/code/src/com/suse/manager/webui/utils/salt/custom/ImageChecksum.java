package com.suse.manager.webui.utils.salt.custom;

public class ImageChecksum {
    /**
     * Checksum
     */
    public interface Checksum {
        /**
         * @return the checksum
         */
        String getChecksum();
    }

    /**
     * SHA1Checksum
     */
    public static class SHA1Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA1Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha1:" + checksum;
        }
    }

    /**
     * SHA256Checksum
     */
    public static class SHA256Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA256Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha256:" + checksum;
        }
    }

    /**
     * SHA384Checksum
     */
    public static class SHA384Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA384Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha384:" + checksum;
        }
    }

    /**
     * SHA512Checksum
     */
    public static class SHA512Checksum implements Checksum {

        private String checksum;

        /**
         * Constructor
         * @param checksumIn checksum
         */
        public SHA512Checksum(String checksumIn) {
            checksum = checksumIn;
        }

        @Override
        public String getChecksum() {
            return checksum;
        }

        /**
         * @return checksum with type
         */
        @Override
        public String toString() {
            return "sha512:" + checksum;
        }
    }
}
