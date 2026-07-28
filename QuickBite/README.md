# QuickBite backend

Spring Boot API using a local SQLite database.

## Start

From this directory in PowerShell:

```powershell
$env:JWT_SECRET="quickbite-local-development-secret-key-123456"
$env:PAYSTACK_SECRET_KEY="replace_with_your_paystack_test_key"
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:9909`. Swagger is available at
`http://localhost:9909/swagger-ui.html`.

SQLite creates `quickbite.db` automatically in this directory. There is no
separate database server to install or start.

To store the database elsewhere:

```powershell
$env:DB_URL="jdbc:sqlite:C:/path/to/quickbite.db"
```

## Tests

```powershell
.\mvnw.cmd test
```
