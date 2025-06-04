import os
import time
import requests
import logging
from urllib.parse import urljoin
from vex.parser.csaf_parser import CSAFParser
from vex.persistence.vex_persistence import VEXDatabaseManager



class Downloader():
    def __init__(self, parser=CSAFParser()):
        self.parser = parser
        self.db_manager = VEXDatabaseManager()

        self.BASE_URL = "https://ftp.suse.com/pub/projects/security/csaf-vex/"
        self.INDEX_FILE = "index.txt"
        self.DOWNLOAD_DIR = "/tmp/csaf_vex_files/"  # Directorio donde se guardarán los archivos

        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.DEBUG)

    def create_download_directory(self):
        """
        Creates temporal directory where files will be downloaded.
        """
        if not os.path.exists(self.DOWNLOAD_DIR):
            os.makedirs(self.DOWNLOAD_DIR)
            print(f"Directorio {self.DOWNLOAD_DIR} creado")

    def remove_file(self, file):
        """
        Creates temporal directory where files will be downloaded.
        """
        if os.path.exists(file):
            os.remove(file)
            print(f"Fichero {file} eliminado")

    def download_index_file(self):
        """
        Downloads index file with all filenames.

        Returns:
            lines: Array with all filenames,
        """
        url = urljoin(self.BASE_URL, self.INDEX_FILE)
        response = requests.get(url)
        if response.status_code == 200:
            return response.text.splitlines()

        raise Exception(f"No se pudo descargar el índice. Código de estado: {response.status_code}")

    def parse_index_lines(self, lines):
        """
        Parsea las líneas del índice para obtener nombres de archivos y sus hashes
        """
        
        files = []
        for line in lines:
            
            parts = line.split()
            if len(parts) >= 2:
                files.append((parts[-1], parts[0]))  # (filename, hash)
        return files


    def retrieve_hash(self, filename):
        """
        Gets the hash of a given file from the external repo.
        """
        url = urljoin(self.BASE_URL, filename+".sha256") 
        response = requests.get(url, stream=True)
        return response.content.decode().split()[0]

    def check_hash_db(self, cve_id):

        try:
            logging.critical(f"Checking hash for {cve_id}")
            self.db_manager.connect()
            result, status = self.db_manager.get_cve_hash(cve_id)
        
        except Exception as e:
            logging.critical(f"{str(e)}")
            
        finally:
            self.db_manager.close()

        logging.critical(f"DB hash {result} , {status}")

        return result, status
    
    def insert_hash_db(self, cve_id, file_hash, hash_status, hash_type='SHA256'):

        

        try:
            self.db_manager.connect()
            logging.critical(self.db_manager.insert_cve(cve_id))
            self.db_manager.insert_vex_hash(cve_id, file_hash, hash_status, hash_type)
            
        except Exception as e:
            logging.error(f"{str(e)}")

        finally:
            self.db_manager.close()


    # TODO: Change returns by raise
    def download_file(self, filename):
        """
        Download a file.
        """

        file_hash = self.retrieve_hash(filename)

        cve_id = filename.rstrip(".json")
        db_hash, db_hash_status = self.check_hash_db(cve_id)

        logging.info(f"File hash: {file_hash}")
        logging.info(f"DB hash: {db_hash}")
        logging.info(f"DB hash status: {db_hash_status}")

        if str(db_hash) != str(file_hash):
            logging.info(f"File needs to be updated")

            url = urljoin(self.BASE_URL, filename)
            local_path = os.path.join(self.DOWNLOAD_DIR, filename)
            
            logging.info(f"Downloading from {url}...")
            response = requests.get(url, stream=True)
            
            if response.status_code == 200:
                with open(local_path, 'wb') as f:
                    for chunk in response.iter_content(chunk_size=8192):
                        f.write(chunk)
                logging.info(f"Downloading completed: {local_path}")

                return file_hash
            
            else:
                logging.error(f"Error al descargar {filename}. Código de estado: {response.status_code}")
                return None

        elif db_hash_status != 0:
            logging.warning(f"File not valid, nothing to do here :)")
            return None
            

    def download(self):
        try:
            logging.debug("Iniciando descarga de archivos VEX/CSAF de SUSE...")
            self.create_download_directory()

            logging.debug(f"\nDownloading index from: {self.BASE_URL}...")
            index_lines = self.download_index_file()

            # Progress control
            start_time = time.time()
            downloaded_count = 0
            total_files = len(index_lines)

            logging.debug(f"\nFound {total_files} files in index.")
            
            for filename in index_lines:
                hash = self.download_file(filename)

                # Formatting CVE 
                cve_id = filename.rstrip(".json")
                

                if hash != None:
                    try:
                        downloaded_count += 1

                        self.parser.parse(self.DOWNLOAD_DIR+filename)
                        logging.info(f"Parsing info with hash {hash}")
                        self.parser.persist_data()

                        logging.info(f"Inserting hash: {hash} for {cve_id}")
                        self.insert_hash_db(cve_id, hash, 0, 'SHA256')

                        self.remove_file(self.DOWNLOAD_DIR+filename)
                    
                    except Exception as e:
                        logging.error(f"Error while parsing {filename}: {str(e)}")
                        if "product_tree" in str(e):
                            self.insert_hash_db(cve_id, hash, -1, 'SHA256')
                            self.remove_file(self.DOWNLOAD_DIR+filename)
                
                # Cálculos de progreso
                elapsed_time = time.time() - start_time
                progress = (downloaded_count / total_files) * 100
                if downloaded_count > 0:
                    remaining_time = (elapsed_time / downloaded_count) * (total_files - downloaded_count)/60
                else:
                    remaining_time = 0
                
                logging.info(f"""\n
                                ▶ Progress: {downloaded_count}/{total_files} ({progress:.2f}%)
                                ▶ Time spent: {elapsed_time:.2f}s
                                ▶ Remaining: {remaining_time:.2f}min
                            """)

            total_time = time.time() - start_time
            logging.info(f"\n[*] Process completed in {total_time:.2f} seg.")
            logging.info(f"[*] Downloaded files: {downloaded_count}/{total_files}")
                
            logging.info("\nProceso completado.")
            
        except Exception as e:
            logging.error(f"\nError: {str(e)}")

