#  pylint: disable=missing-module-docstring


#  pylint: disable-next=missing-class-docstring
class RepoDTO:
    def __init__(
        self,
        channel_label,
        repo_id,
        channel_arch,
        repo_label,
        repo_type,
        source_url,
        metadata_signed="N",
    ):
        self.repo_id = repo_id
        self.channel_arch = channel_arch
        self.repo_label = repo_label
        self.repo_type = repo_type
        self.source_url = source_url
        self.metadata_singed = metadata_signed
        self.channel_label = channel_label

    def __str__(self):
        return (
            "Repo("
            + str(self.repo_id)
            + ", "
            + self.repo_label
            + ", "
            + self.repo_type
            + ", "
            + self.source_url
            + ")"
        )
