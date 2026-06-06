# Uyuni Bench

Initial real-world storage benchmark helper for Uyuni on RKE2.

This first version focuses only on the **RPM repodata generation workload**. It does not run `fio`, `kubestr` or `pgbench`.

## Workload: repodata generation

The workload builds RPM repository metadata with `createrepo_c` against a repository directory stored on the target storage backend, for example under the Uyuni server PVC:

```text
/var/spacewalk/uyuni-bench/repos/<dataset>/
```

The timed part is only:

```bash
createrepo_c <repo-dir>
```

Dataset download/preparation is separate and should not be included in performance results.

## Requirements

On the machine/container where the workload runs:

```text
python3
createrepo_c
/usr/bin/time       # optional, used for extra metrics
zstd                # only needed for repositories whose metadata is .zst, e.g. Tumbleweed
network access      # only needed during dataset preparation
```

## Quick smoke run

From this directory:

```bash
./uyuni-bench repodata inspect \
  --dataset manifests/datasets/uyuni-test-packages-smoke.json

./uyuni-bench repodata prepare \
  --dataset manifests/datasets/uyuni-test-packages-smoke.json \
  --repo-dir /tmp/uyuni-bench/repos/smoke \
  --workers 4

./uyuni-bench repodata run \
  --repo-dir /tmp/uyuni-bench/repos/smoke \
  --storage-backend local-smoke \
  --mode full \
  --iterations 1
```

Result summary is written under:

```text
results/repodata/<storage-backend>-<timestamp>/summary.json
```

## Large dataset example

For a larger real benchmark:

```bash
./uyuni-bench repodata prepare \
  --dataset manifests/datasets/opensuse-tumbleweed-oss-current-x86_64-noarch.json \
  --repo-dir /var/spacewalk/uyuni-bench/repos/tumbleweed-oss-current \
  --workers 16

./uyuni-bench repodata run \
  --repo-dir /var/spacewalk/uyuni-bench/repos/tumbleweed-oss-current \
  --storage-backend longhorn-3replica \
  --mode full \
  --iterations 3
```

The Tumbleweed dataset is large, around 50k+ packages and 100+ GiB. Use `--limit` for development runs:

```bash
./uyuni-bench repodata prepare \
  --dataset manifests/datasets/opensuse-tumbleweed-oss-current-x86_64-noarch.json \
  --repo-dir /var/spacewalk/uyuni-bench/repos/tw-1000 \
  --limit 1000
```

## Running against Uyuni on RKE2

The benchmark should run where the Uyuni storage is mounted. For the server Helm chart, `/var/spacewalk` is backed by the `var-spacewalk` PVC.

Typical flow:

```bash
POD=$(kubectl -n uyuni get pod -l app.kubernetes.io/component=server -o jsonpath='{.items[0].metadata.name}')

# Copy this tool into the server pod, or make it available via your automation.
kubectl -n uyuni cp . "$POD:/tmp/uyuni-bench"

kubectl -n uyuni exec -it "$POD" -- bash
cd /tmp/uyuni-bench
./uyuni-bench repodata prepare --dataset manifests/datasets/uyuni-test-packages-smoke.json --repo-dir /var/spacewalk/uyuni-bench/repos/smoke
./uyuni-bench repodata run --repo-dir /var/spacewalk/uyuni-bench/repos/smoke --storage-backend local-path --iterations 1
```

Before running, confirm the path is on the expected PVC:

```bash
df -h /var/spacewalk
```

## Commands

### Inspect metadata only

```bash
./uyuni-bench repodata inspect --dataset manifests/datasets/opensuse-tumbleweed-oss-current-x86_64-noarch.json
```

### Prepare dataset

Downloads RPMs into a repository directory and stores a manifest at:

```text
<repo-dir>/.uyuni-bench/dataset.json
```

```bash
./uyuni-bench repodata prepare --dataset <dataset.json> --repo-dir <repo-dir>
```

### Run benchmark

```bash
./uyuni-bench repodata run --repo-dir <repo-dir> --storage-backend <label> --iterations 3
```

Modes:

```text
full    remove repodata/ before each run
update  run createrepo_c --update
```

### Clean generated metadata

```bash
./uyuni-bench repodata clean --repo-dir <repo-dir>
```

## Optional Cucumber integration

This tool is wired into an optional Cucumber run set:

```text
testsuite/features/kubernetes/srv_rke2_storage_benchmark.feature
testsuite/run_sets/kubernetes_storage_benchmark.yml
```

Run it explicitly from the controller:

```bash
cd /root/spacewalk/testsuite
rake cucumber:kubernetes_storage_benchmark
```

Useful environment variables:

```text
UYUNI_BENCH_STORAGE_BACKEND   label stored in results, e.g. local-path or nfs
UYUNI_BENCH_DATASET           dataset JSON path inside the copied tool directory
UYUNI_BENCH_REPO_DIR          repo path inside the server pod; default is /var/spacewalk/uyuni-bench/repos/repodata
UYUNI_BENCH_ITERATIONS        timed repetitions; default 1
UYUNI_BENCH_LIMIT             package limit for development runs
UYUNI_BENCH_PREPARE_WORKERS   parallel package downloads; default 4
UYUNI_BENCH_REPODATA_MODE     full or update; default full
```

Example:

```bash
UYUNI_BENCH_STORAGE_BACKEND=local-path \
UYUNI_BENCH_LIMIT=1000 \
rake cucumber:kubernetes_storage_benchmark
```

## Result fields

`summary.json` includes:

```text
storage_backend
mode
iterations
createrepo_c version
RPM count
repository size
repodata size
wall-clock duration
/usr/bin/time -v metrics when available
stdout/stderr log paths
```

## Notes

- Do not commit downloaded RPM datasets.
- Do not commit benchmark result directories.
- Use a fixed dataset manifest for publication-quality comparison.
- The `opensuse-tumbleweed-oss-current` dataset URL is moving; it is useful for experiments but should be pinned before final reporting.
