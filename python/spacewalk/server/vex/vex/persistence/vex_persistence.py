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

    def insert_cve(self, cve_name):
        """Insert a CVE and return its ID
        Returns:
            int: Existing or new CVE ID, -1 on failure
        """
            
        try:
            with self.conn.cursor() as cursor:
                logging.info(f"Inserting new CVE: {cve_name}")
                cursor.execute(
                    """
                        INSERT INTO rhnCve (id, name)
                        VALUES (nextval('rhn_cve_id_seq'), %s)
                        ON CONFLICT (name) DO NOTHING
                        RETURNING id
                    """, [cve_name]
                )
                result = cursor.fetchone()
                if result:
                    self.conn.commit()
                    return result[0]

                # If already existed, get its ID
                cursor.execute("SELECT id FROM rhnCve WHERE name = %s", [cve_name])
                existing_id = cursor.fetchone()
                if existing_id:
                    self.conn.commit()
                    return existing_id[0]
                
        except Exception as e:
            self.conn.rollback()  # Rollback on error
            logging.error(f"Failed to insert CVE {cve_name}: {e}")
            return -1

    def insert_oval_platform(self, cpe):
        """Insert a platform and return its ID
        Returns:
            int: Existing or new platform ID, -1 on failure
        """
        
            
        try:
            with self.conn.cursor() as cursor:
                # Search existing
                cursor.execute("SELECT id FROM suseOVALPlatform WHERE cpe = %s", [cpe])
                result = cursor.fetchone()
                if result:
                    logging.debug(f"Platform {cpe} already exists with ID {result[0]}")
                    return result[0]

                # Insert if not in table
                logging.debug(f"Inserting new platform: {cpe}")
                cursor.execute(
                    """
                    INSERT INTO suseOVALPlatform (id, cpe)
                    VALUES (nextval('suse_oval_platform_id_seq'), %s)
                    RETURNING id;
                    """, [cpe]
                )
                inserted_id = cursor.fetchone()[0]
                self.conn.commit()
                return inserted_id

        except Exception as e:
            self.conn.rollback()  # Rollback on error
            logging.error(f"Failed to insert platform {cpe}: {e}")
            return -1

    def insert_vulnerable_package(self, name):
        """Insert a vulnerable package and return its ID (with existence check)"""

        try:
            with self.conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO rhnPackageName (id, name)
                    VALUES (nextval('rhn_pkg_name_seq'), %s)
                    ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                    RETURNING id;
                    """, [name]
                )
                inserted_id = cursor.fetchone()[0]
                self.conn.commit()
                logging.debug(f"Inserted or updated package {name} with ID {inserted_id}")
                return inserted_id

        except Exception as e:
            self.conn.rollback()
            logging.error(f"Failed to insert package {name}: {e}")
            return -1

    def insert_vex_annotation(self, platform_id, cve_id, package_name, status, fix_version=None):
        """Insert or update a VEX annotation with fix version.

        Returns:
            str | None: Final vex_status, or None on failure.
        """
        try:
            with self.conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO suseVEXAnnotations 
                        (platform_id, cve_id, package_name, vex_status, fix_version)
                    VALUES (%s, %s, %s, %s, %s)
                    ON CONFLICT (platform_id, cve_id, package_name)
                    DO UPDATE SET 
                        vex_status = EXCLUDED.vex_status,
                        fix_version = EXCLUDED.fix_version
                    RETURNING vex_status;
                    """,
                    [platform_id, cve_id, package_name, status, fix_version]
                )
                result = cursor.fetchone()
                self.conn.commit()
                logging.debug(f"Annotation saved with status {result[0]}")
                return result[0]

        except Exception as e:
            self.conn.rollback()
            logging.error(f"Failed to insert/update annotation for CVE {cve_id}, platform {platform_id}, pkg {package_name}: {e}")
            return None


    def store_vex_object(self, platform_cpe, cve_name, package_name, status="status", fix_version=-1):
        """Main method to store all VEX components"""
        try:
            # Insert platform
            platform_id = self.insert_oval_platform(platform_cpe)
            
            # Insert vulnerable package
            pkg_id = self.insert_vulnerable_package(package_name, fix_version)
            
            # Get CVE ID
            cve_id = self.insert_cve(cve_name)
            
            # Create VEX annotation
            self.insert_vex_annotation(platform_id, cve_id, pkg_id, status, fix_version)
            
            self.conn.commit()
            print("VEX object stored successfully!")
            return True
            
        except Exception as e:
            self.conn.rollback()
            print(f"Error storing VEX object: {e}")
            return False

    def get_cve_hash(self, cve_id):
        """Get both hash and hash_status from a CVE.

        Args:
            cve_id (str): CVE identifier (e.g., 'CVE-2023-1234')
            hash_type (str): Hash type ('MD5', 'SHA1', 'SHA256', 'SHA512', 'OTHER')

        Returns:
            tuple(str, str)|None: Tuple (hash, cve_status) if found, None otherwise.
        """

        cve_id = cve_id[:3].upper() + cve_id[3:]
        logging.info(f"Retrieving hash for {cve_id}")

        try:
            with self.conn.cursor() as cursor:
                cursor.execute(
                    "SELECT hash, cve_status FROM suseVEXHash WHERE cve = %s",
                    (cve_id,)
                )
                result = cursor.fetchone()

                if result:
                    logging.info(f"Found hash and status for CVE={cve_id}: {result}")
                    return result  # result is a tuple (hash, cve_status)
                else:
                    logging.warning(f"{cve_id} not found in suseVEXHash")
                    return None, None
        except Exception as e:
            logging.error(f"Error retrieving hash and status for CVE {cve_id}: {str(e)}")
            return None, None


    def insert_vex_hash(self, cve_id, hash, hash_status=0, hash_type="SHA256"):
        """
        Insert or update a VEX hash
        
        Args:
            cve_id (str): CVE name (e.g., 'CVE-2023-1234')
            hash (str): Hash value to store
            hash_type ('MD5', 'SHA1', 'SHA256', 'SHA512', 'OTHER'): Hash type (default:'SHA256')
            
        Returns:
            int: 1 if inserted, 0 if already existed, -1 on error
        """

        cve_id = cve_id[:3].upper() + cve_id[3:]
        logging.info(f"Inserting hash for {cve_id}")

        try:
            with self.conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO suseVEXHash 
                    (id, cve, hash_type, hash, created, last_updated, cve_status)
                    VALUES (
                        nextval('suse_cve_hash_id_seq'),
                        %s, %s, %s,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP,
                        %s
                    )
                    ON CONFLICT (cve)
                    DO UPDATE SET
                        hash = EXCLUDED.hash,
                        hash_type = EXCLUDED.hash_type,
                        last_updated = CURRENT_TIMESTAMP,
                        cve_status = EXCLUDED.cve_status
                        WHERE suseVEXHash.hash IS DISTINCT FROM EXCLUDED.hash
                        OR suseVEXHash.hash_type IS DISTINCT FROM EXCLUDED.hash_type
                        OR suseVEXHash.cve_status IS DISTINCT FROM EXCLUDED.cve_status
                    RETURNING (xmax = 0) AS inserted;
                    """,
                    (cve_id, hash_type, hash, hash_status,)
                )

                inserted = cursor.fetchone()[0]
                self.conn.commit()
                return 1 if inserted else 0

        except Exception as e:
            self.conn.rollback()
            logging.error(f"Failed to store hash {hash} for {cve_id}")
            logging.error(e)
            return -1

# Example usage
# Using context manager (recommended)
# with VEXDatabaseManager() as db_manager:
#     db_manager.store_vex_object(
#         platform_cpe="cpe:/o:suse:sles:15:sp4",
#         cve_name="CVE-2023-1234",
#         package_name="openssl",
#         fix_version="1.1.1k-94.1",
#         status="fixed"
#     )

# Alternative usage without context manager
# db_manager = VEXDatabaseManager()
# try:
#     db_manager.connect()
#     db_manager.store_vex_object(...)
# finally:
#     db_manager.close()
