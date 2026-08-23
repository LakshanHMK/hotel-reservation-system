# LankaStay Hotel Reservation System

The authentication backend is currently included under `backend/`.

## Run the backend

Create a MySQL database and application user, then set the credentials in PowerShell:

```powershell
cd backend
$env:DB_USERNAME="lankastay_app"
$env:DB_PASSWORD="your_password"
.\mvnw.cmd spring-boot:run
```

The backend runs at `http://localhost:8080`. Flyway applies the committed database migrations when the application starts.

Do not commit passwords, `.env` files, `target/`, or `uploads/`.