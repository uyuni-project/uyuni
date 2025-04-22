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
        else:
            raise Exception(f"No se pudo descargar el índice. Código de estado: {response.status_code}")

    def parse_index_lines(self, lines):
        """Parsea las líneas del índice para obtener nombres de archivos y sus hashes"""
        files = []
        for line in lines:
            print(line)
            if line.strip():  # Ignorar líneas vacías
                parts = line.split()
                if len(parts) >= 2:  # Al menos hash y nombre de archivo
                    file_hash = parts[0]
                    filename = parts[-1]  # El último elemento es el nombre del archivo
                    files.append((filename, file_hash))
        return files

    def retrieve_hash(self, filename):
        """
        Gets the hash of a given file from the external repo.
        """
        url = urljoin(self.BASE_URL, filename+".sha256") 
        response = requests.get(url, stream=True)
        return response.content.decode().split()[0]

    def check_hash_db(self, filename):

        cve = filename.replace(".json", "")

        try:
            self.db_manager.connect()
            result = self.db_manager.get_cve_hash(cve, 'SHA256')
        finally:
            self.db_manager.close()
        
        return result

    def download_file(self, filename):
        """
        Download a file.
        """

        # Debug info
        logging.debug(f"\nFile: {filename}")
        file_hash = self.retrieve_hash(filename)
        logging.debug(f"Hash SHA-256: {file_hash}")

        db_hash = self.check_hash_db(filename)

        if db_hash == -1 or db_hash != file_hash:

            logging.debug(f"File needs to be updated")

            url = urljoin(self.BASE_URL, filename)
            local_path = os.path.join(self.DOWNLOAD_DIR, filename)
            
            logging.debug(f"Descargando desde {url}...")
            response = requests.get(url, stream=True)
            
            if response.status_code == 200:
                with open(local_path, 'wb') as f:
                    for chunk in response.iter_content(chunk_size=8192):
                        f.write(chunk)
                logging.debug(f"Descarga completada: {local_path}")
                return file_hash
            else:
                logging.error(f"Error al descargar {filename}. Código de estado: {response.status_code}")
                return None
        
        else:
            logging.debug(f"File already exists, nothing to do here :)")

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

            logging.debug(f"\nEncontrados {total_files} archivos en el índice.")
            
            for filename in index_lines:
                hash = self.download_file(filename)
                if hash != None:

                    try:
                        downloaded_count += 1

                        self.parser.parse(self.DOWNLOAD_DIR+filename)
                        logging.info(f"Parsing info with hash {hash}")
                        self.parser.persist_data(hash, 'SHA256')
                        self.remove_file(self.DOWNLOAD_DIR+filename)

                        # Cálculos de progreso
                        elapsed_time = time.time() - start_time
                        progress = (downloaded_count / total_files) * 100
                        if downloaded_count > 0:
                            remaining_time = (elapsed_time / downloaded_count) * (total_files - downloaded_count)/60
                        else:
                            remaining_time = 0
                        
                        logging.info(f"\n▶ Progress: {downloaded_count}/{total_files} ({progress:.2f}%)")
                        logging.info(f"▶ Time spent: {elapsed_time:.2f}s")
                        logging.info(f"▶ Remaining: {remaining_time:.2f}min")
                    
                    except Exception as e:
                        logging.error(f"Error while parsing {filename}: {str(e)}")
                
            total_time = time.time() - start_time
            logging.info(f"\n[*] Process completed in {total_time:.2f} seg.")
            logging.info(f"[*] Downloaded files: {downloaded_count}/{total_files}")
                
            logging.info("\nProceso completado.")
            
        except Exception as e:
            logging.error(f"\nError: {str(e)}")

