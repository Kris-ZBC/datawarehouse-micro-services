Docker stack (Windows)

This document shows how to start the MariaDB + Login stack on Windows (Docker Desktop) and how to enable file sharing so the `infra/init-mariadb.sql` initialization file runs automatically.

Prerequisites
- Docker Desktop installed and running
- Your user has access to Docker Desktop and the drive containing the repository is shared with Docker Desktop (see steps below)

Enable file sharing (Docker Desktop)
1. Open Docker Desktop (click the whale icon in the system tray, or start from Start menu).
2. Click the gear icon to open Settings (or Settings/Preferences).
3. Go to Resources -> File Sharing (or "Resources" -> "File Sharing" / "File Sharing & WSL" depending on version).
4. Add the drive or folder that contains the repository. For example: `C:\Users\zbc23mhse\IdeaProjects\api-service` or share the whole `C:` drive.
   - Click the `+` or `Browse` button and select the folder.
   - On Windows, Docker Desktop may ask you to enter your Windows credentials to authorize the share. Enter your username and password when prompted.
5. Apply & Restart Docker Desktop when asked.

Note: On newer Docker Desktop versions using WSL2 backend, you may not need explicit drive sharing. Instead ensure the files are available inside WSL (e.g. using the WSL distro's filesystem or mounting the Windows path). If you use WSL2 and your project lives on the Windows filesystem, file sharing should be automatic; if it is not, share the drive as above.

Start the stack (recommended)
From PowerShell (run as a normal user):

```powershell
Set-Location C:\path\to\projectroot
# Build images and bring up the stack defined in infra/compose.yml
# This will build the login image (if necessary) and start mariadb + login
docker compose -f .\infra\compose.yml up -d

# Show running containers for the stack
docker ps --filter "name=mariadb-db" --filter "name=sop-login"

# Watch logs
docker logs -f mariadb-db
docker logs -f sop-login
```

If `docker compose` is not available on your PATH, use the hyphenated docker-compose binary instead:

```powershell
# legacy binary
docker-compose -f .\infra\compose.yml up -d
```

Troubleshooting
- If `init-mariadb.sql` did not run automatically (no `sopinfo_login_db` created):
  - Confirm Docker Desktop was running when the container was created and the drive was shared.
  - Check the `docker logs mariadb-db` output for errors related to running `/docker-entrypoint-initdb.d/init-mariadb.sql`.
  - If necessary, apply the SQL manually:

```powershell
Get-Content .\infra\init-mariadb.sql | docker exec -i mariadb-db mariadb -uroot -p<MARIADB_ROOT_PASSWORD>
```

- If `docker compose` complains about `context` paths when building, ensure the `context` paths in `infra/compose.yml` are correct. The `infra` compose file expects `context: ../bc-login/login-impl` (relative to the `infra/` folder).

Reverting the temporary docker-compose plugin shim
If you previously copied `docker-compose.exe` into `C:\Users\<you>\\.docker\\cli-plugins` to enable `docker compose`, you can remove it to revert to the prior state. On PowerShell:

```powershell
Remove-Item -LiteralPath "$env:USERPROFILE\.docker\cli-plugins\docker-compose.exe" -Force
```

If you want the official `docker compose` plugin instead, install Docker Desktop updates or follow the Docker docs to install the plugin.

If you want, I can also:
- Update other compose files in `infra/` to use correct relative build contexts (none were found except `compose.yml`),
- Add or adjust a Windows-specific startup script, or
- Walk you through enabling Docker Desktop drive sharing interactively.

