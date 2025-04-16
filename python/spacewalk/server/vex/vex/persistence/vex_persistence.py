import psycopg2
from psycopg2 import sql
import logging

class VEXDatabaseManager:
    def __init__(self, dbname="susemanager", user="spacewalk", 
                 password="OaPqj3cJ7E6fNjIK0iK6WHg/+akS6TCmTRaqfml/", 
                 host="localhost"):
        
        """Initialize the database connection parameters"""

        self.load_db_config()

        logging.info(self.config.get("db_name"))
        logging.info(self.config.get("db_user"))
        logging.info(self.config.get("db_password"))

        self.dbname=self.config.get("db_name")
        self.user=self.config.get("db_user")
        self.password=self.config.get("db_password")
        self.host=self.config.get("db_host")
        self.conn = None

    def __enter__(self):
        """Connect to database when entering context manager"""
        self.connect()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Close connection when exiting context manager"""
        self.close()

    def load_db_config(self, path="/etc/rhn/rhn.conf"):
        self.config = {}
        with open(path, "r") as file:
            for linea in file:
                if "=" in linea and not linea.strip().startswith("#"):
                    clave, valor = linea.strip().split("=", 1)
                    self.config[clave.strip()] = valor.strip()
        return self.config

    def connect(self):
        """Establish database connection"""
        try:
            self.conn = psycopg2.connect(
                dbname=self.dbname,
                user=self.user,
                password=self.password,
                host=self.host
            )
            return self.conn
        except psycopg2.Error as e:
            raise ConnectionError(f"Failed to connect to database: {e}")

    def close(self):
        """Close database connection if it exists"""
        if self.conn and not self.conn.closed:
            self.conn.close()

    def get_cve_id(self, cve_name):
        """Get ID of an existing CVE from rhnCve
        Returns:
            int: CVE ID if found, -1 if not found
        """
        with self.conn.cursor() as cursor:
            cursor.execute(
                sql.SQL("SELECT id FROM rhnCve WHERE name = %s"), [cve_name]
            )
            result = cursor.fetchone()
            if result:
                logging.info(f"Found CVE {cve_name}: ID {result[0]}")  # DEBUG
                return result[0]
            logging.warning(f"CVE {cve_name} not found in rhnCve")  # DEBUG
            return -1

    def get_platform_id(self, cpe):
        """Get ID of an existing platform from suseOVALPlatform
        Returns:
            int: Platform ID if found, -1 if not found
        """
        with self.conn.cursor() as cursor:
            cursor.execute(
                sql.SQL("SELECT id FROM suseOVALPlatform WHERE cpe = %s"), [cpe]
            )
            result = cursor.fetchone()
            if result:
                logging.info(f"Found platform {cpe}: ID {result[0]}")  # DEBUG
                return result[0]
            logging.warning(f"Platform {cpe} not found in suseOVALPlatform")  # DEBUG
            return -1

    def get_package_id(self, name):
        """Get ID of an existing vulnerable package
        Args:
            name: Package name (e.g., "openssl")
            fix_version: Optional fix version (e.g., "1.1.1k-94.1")
        Returns:
            int: Package ID if found, -1 if not found
        """
        with self.conn.cursor() as cursor:
            cursor.execute(
                sql.SQL("SELECT id FROM suseOVALVulnerablePackage WHERE name = %s"), [name]
            )
            result = cursor.fetchone()
            if result:
                logging.info(f"Found package {name}: ID {result[0]}")  # DEBUG
                return result[0]
            logging.warning(f"Package {name} not found in suseOVALVulnerablePackage")  # DEBUG
            return -1

    def insert_cve(self, cve_name):
        """Insert a CVE and return its ID
        Returns:
            int: Existing or new CVE ID, -1 on failure
        """
        existing_id = self.get_cve_id(cve_name)
        if existing_id != -1:
            return existing_id
            
        try:
            with self.conn.cursor() as cursor:
                logging.info(f"Inserting new CVE: {cve_name}")
                cursor.execute(
                    sql.SQL("""
                        INSERT INTO rhnCve (id, name)
                        VALUES (nextval('rhn_cve_id_seq'), %s)
                        RETURNING id
                    """), [cve_name]
                )
                inserted_id = cursor.fetchone()[0]
                self.conn.commit()  # Explicit commit
                return inserted_id
        except Exception as e:
            self.conn.rollback()  # Rollback on error
            logging.error(f"Failed to insert CVE {cve_name}: {e}")
            return -1

    def insert_oval_platform(self, cpe):
        """Insert a platform and return its ID
        Returns:
            int: Existing or new platform ID, -1 on failure
        """
        existing_id = self.get_platform_id(cpe)
        if existing_id != -1:
            return existing_id
            
        try:
            with self.conn.cursor() as cursor:
                logging.info(f"Inserting new platform: {cpe}")
                cursor.execute(
                    sql.SQL("""
                        INSERT INTO suseOVALPlatform (id, cpe)
                        VALUES (nextval('suse_oval_platform_id_seq'), %s)
                        RETURNING id
                    """), [cpe]
                )
                inserted_id = cursor.fetchone()[0]
                self.conn.commit()  # Commit operation
                return inserted_id
        except Exception as e:
            self.conn.rollback()  # Rollback on error
            logging.error(f"Failed to insert platform {cpe}: {e}")
            return -1

    def insert_vulnerable_package(self, name, fix_version=None):
        """Insert a vulnerable package and return its ID (with existence check)"""
        # First check if package already exists
        existing_id = self.get_package_id(name)
        if existing_id != -1:
            logging.info(f"Package {name} already exists with ID {existing_id}")
            return existing_id

        try:
            with self.conn.cursor() as cursor:
                cursor.execute(
                    sql.SQL("""
                        INSERT INTO suseOVALVulnerablePackage (id, name, fix_version)
                        VALUES (nextval('suse_oval_vulnerable_pkg_id_seq'), %s, %s)
                        RETURNING id
                    """), [name, fix_version]
                )
                inserted_id = cursor.fetchone()[0]
                self.conn.commit()  # Explicit commit
                logging.info(f"Inserted new package {name} (fix: {fix_version}) with ID {inserted_id}")
                return inserted_id
        except Exception as e:
            self.conn.rollback()
            logging.error(f"Failed to insert package {name}: {e}")
            return -1

    def insert_vex_annotation(self, platform_id, cve_id, vulnerable_pkg_id, status):
        """Insert a VEX annotation"""
        with self.conn.cursor() as cursor:
            cursor.execute(
                sql.SQL("""
                    INSERT INTO suseVEXAnnotations 
                    (platform_id, cve_id, vulnerable_pkg_id, status)
                    VALUES (%s, %s, %s, %s)
                """), [platform_id, cve_id, vulnerable_pkg_id, status]
            )



    def store_vex_object(self, platform_cpe, cve_name, package_name, fix_version=None, status="status"):
        """Main method to store all VEX components"""
        try:
            # Insert platform
            platform_id = self.insert_oval_platform(platform_cpe)
            
            # Insert vulnerable package
            pkg_id = self.insert_vulnerable_package(package_name, fix_version)
            
            # Get CVE ID
            cve_id = self.get_cve_id(cve_name)
            
            # Create VEX annotation
            self.insert_vex_annotation(platform_id, cve_id, pkg_id, status)
            
            self.conn.commit()
            print("VEX object stored successfully!")
            return True
            
        except Exception as e:
            self.conn.rollback()
            print(f"Error storing VEX object: {e}")
            return False


# Example usage
if __name__ == "__main__":
    # Using context manager (recommended)
    with VEXDatabaseManager() as db_manager:
        db_manager.store_vex_object(
            platform_cpe="cpe:/o:suse:sles:15:sp4",
            cve_name="CVE-2023-1234",
            package_name="openssl",
            fix_version="1.1.1k-94.1",
            status="fixed"
        )

    # Alternative usage without context manager
    # db_manager = VEXDatabaseManager()
    # try:
    #     db_manager.connect()
    #     db_manager.store_vex_object(...)
    # finally:
    #     db_manager.close()