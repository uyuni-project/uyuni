"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""
import abc
import os


class Repo(metaclass=abc.ABCMeta):

    def __init__(self, name, cache_path, repository, handler):
        self.name = name
        self.cache_dir = os.path.join(cache_path, str(name))
        self.repository = repository
        self.handler = handler  # The sax handler/parser
        self.metadata_files = None  # Eg: 'primary.xml', 'filelists.xml', etc..

    def get_repo_path(self, path):
        return "{}/{}".format(self.repository, path)

    def get_metadata_files(self):
        """
        Return a dict containing the metadata files' information in the following format
        {
            "type [eg: primary]" : {
                                    "location": "...",
                                    "checksum": "...",
                                    }
        }
        """
        repomd_url = self.get_repo_path("repomd.xml")
        repomd_path = urllib.request.urlopen(repomd_url)
        doc = pulldom.parse(repomd_path)
        files = {}
        for event, node in doc:
            if event == pulldom.START_ELEMENT and node.tagName == "data":
                doc.expandNode(node)
                files[node.getAttribute("type")] = {
                    "location": node.getElementsByTagName("location")[0].getAttribute("href"),
                    "checksum": get_text(node.getElementsByTagName("checksum")[0].childNodes)
                }
        return files

    def find_metadata_file_url(self, file_name) -> (str, str):
        """
        Return the corresponding metadata file url given its name.
        An example of these files can be 'primary', 'filelists', 'other', etc...
        """
        if not self.metadata_files:
            self.metadata_files = self.get_metadata_files()
        md_file_url = urljoin(
            self.repository,
            self.metadata_files[file_name]['location'].lstrip("/repodata")
        )
        return md_file_url

    def find_metadata_file_checksum(self, file_name):
        """
        Return the corresponding metadata file url given its name.
        An example of these files can be 'primary', 'filelists', 'other', etc...
        """
        if not self.metadata_files:
            self.metadata_files = self.get_metadata_files()
        return self.metadata_files[file_name]["checksum"]


    # @profile
    def download_and_parse_metadata(self):
        if not self.repository:
            print("Error: target url not defined!")
            raise ValueError("Repository URL missing")

        hash_file = os.path.join(self.cache_dir, self.name) + ".hash"

        primary_url = self.find_metadata_file_url("primary")
        primary_hash = self.find_metadata_file_checksum("primary")

        for cnt in range(1, 4):
            try:
                logging.debug("Parsing primary %s, try %s", primary_url, cnt)

                # Download the primary.xml.gz to a file first to avoid
                # connection resets
                with tempfile.TemporaryFile() as tmp_file:
                    with urllib.request.urlopen(primary_url) as primary_fd:
                        # Avoid loading large documents into memory at once
                        chunk_size = 1024 * 1024
                        written = True
                        while written:
                            written = tmp_file.write(primary_fd.read(chunk_size))

                    # Work on temporary file without loading it into memory at once
                    tmp_file.seek(0)
                    with gzip.GzipFile(fileobj=tmp_file, mode="rb") as gzip_fd:
                        parser = xml.sax.make_parser()
                        parser.setContentHandler(self.handler)
                        parser.setFeature(xml.sax.handler.feature_namespaces, True)
                        input_source = InputSource()
                        input_source.setByteStream(gzip_fd)
                        parser.parse(input_source)
                        packages_count = len(self.handler.packages)
                        logging.debug("Parsed packages: %s", packages_count)
                break
            except urllib.error.HTTPError as e:
                # We likely hit the repo while it changed:
                # At the time we read repomd.xml refered to an primary.xml.gz
                # that does not exist anymore.
                if cnt < 3 and e.code == 404:
                    primary_url = self.find_metadata_file_url("primary")
                    time.sleep(2)
                else:
                    raise
            except OSError:
                if cnt < 3:
                    time.sleep(2)
                else:
                    raise

        try:
            # Prepare cache directory
            if not os.path.exists(self.cache_dir):
                logging.debug("Creating cache directory: %s", self.cache_dir)
                os.makedirs(self.cache_dir)
            else:
                # Delete old cache files from directory
                for f in os.listdir(self.cache_dir):
                    os.remove(os.path.join(self.cache_dir, f))

            # Cache the hash of the file
            with open(hash_file, 'w') as fw:
                logging.debug("Caching file hash in file: %s", hash_file)
                fw.write(primary_hash)
        except OSError as error:
            logging.warning("Error caching the primary XML data: %s", error)
