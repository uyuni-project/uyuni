# Integrated Uyuni Dev Container Environment

This repository contains a fully containerized development environment for Uyuni. It utilizes **VS Code DevContainers** and **Docker Compose** to provide a seamless, reproducible workflow that integrates Java development, unit testing, and full system deployment in a single workspace.

## 🌍 Supported Environments

This definition is designed to work identically in two contexts:

### 1. ☁️ GitHub Codespaces (Cloud)
* **Zero Setup:** Runs entirely in the cloud on Azure/GitHub infrastructure.
* **Access:**
    * **Browser:** Edit code and access Web UIs (Uyuni, Reports) via auto-generated `app.github.dev` URLs.
    * **Desktop:** Connect your local VS Code to the cloud instance for a native experience.

### 2. 💻 Local Dev Container (Localhost)
* **Offline Capable:** Runs on your local Docker/Podman engine.

---

## 🏗 Architecture

The environment consists of containers orchestrated to work together:

| Service | Container Name | Description | Status |
| :--- | :--- | :--- | :--- |
| **Development** | `uyuni-dev` | **Main Container.** Runs openSUSE Tumbleweed with Systemd (PID 1). Contains Java, Ant, Maven, Python, and the Source Code. | **Auto-Starts** |
| **Unit Test DB** | `db` | A PostgreSQL instance pre-configured for running Unit Tests (`make test` / `ant test`). | **Auto-Starts** |

---

## 🛠 Feature Breakdown
<img width="884" height="416" alt="motd" src="https://github.com/user-attachments/assets/81223279-e989-45d8-b3b0-585ed9044f1c" />

### 1. Systemd & Podman Support
The `uyuni-dev` container boots **Systemd as PID 1** and includes **Podman**. This allows you to manage containers and run system services natively inside the development environment.

### 2. Java Development & Unit Tests
Pre-configured with `ant`, `maven`, and `java-17-openjdk`.

**Common Commands:**
* **Download Dependencies:** `ant -f java/manager-build.xml ivy`
* **Compile:** `ant -f java/manager-build.xml compile`
* **Run Unit Tests:** `ant -f java/manager-build.xml test-report`

### 3. Visual Test Reports 📊
The environment automatically starts a web server to view HTML test results.
* **Access:** Go to the **PORTS** tab and click the "Open in Browser" (Globe) icon next to **Port 9000**.
* **Note:** In Codespaces, this opens a secure `...app.github.dev` URL. Locally, it opens `localhost:9000`.

### 4. Ephemeral Uyuni Server
<img width="1908" height="999" alt="home" src="https://github.com/user-attachments/assets/7abfe813-478c-4bb3-86c7-d4e2d7375679" />

You can spin up a full ephemeral Uyuni Server instance directly from the terminal.

**Setup:**
```bash
sudo mgradm install podman --config /etc/uyuni-tools/uyuni-tools.yaml
```

**Accessing the UI:**

Go to the **PORTS** tab and click the link for **Port 443**.
* **Credentials:** `admin` / `admin`

---

## 📡 Networking & Ports

### Service Ports

| Service | Port | Description |
| :--- | :--- | :--- |
| **Web UI** | `443`, `80` | Uyuni Web Interface |
| **Reports** | `9000` | Test Results |
| **Database** | `5432` | PostgreSQL |
| **Unit Test Database** | `54320` | PostgreSQL Unit Test |
| **Taskomatic** | `8001` | Remote Debug |
| **Search** | `8002` | Remote Debug |
| **Tomcat** | `8003` | Remote Debug |

---
