Here are **PowerShell-friendly `curl.exe` one-liners** (same pattern as yours). Base URL assumes gateway on **8080**.

### 1. List taxonomies (optional filters)

```powershell
# All taxonomies
curl.exe -s "http://localhost:8080/api/v1/taxonomies" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100

# With query params (examples)
curl.exe -s "http://localhost:8080/api/v1/taxonomies?type=TAG&active=true" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100

curl.exe -s "http://localhost:8080/api/v1/taxonomies?name=Action" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100

curl.exe -s "http://localhost:8080/api/v1/taxonomies?type=CATEGORY&name=Drama&active=true" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

### 2. List allowed type codes

```powershell
curl.exe -s "http://localhost:8080/api/v1/taxonomies/types" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

### 3. Get by slug

```powershell
$slug = "action"   # replace with your slug
curl.exe -s "http://localhost:8080/api/v1/taxonomies/by-slug/$slug" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

### 4. Get by UUID

```powershell
$id = "00000000-0000-0000-0000-000000000000"   # replace with taxonomy id
curl.exe -s "http://localhost:8080/api/v1/taxonomies/$id" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

---

PowerShell examples using **`curl.exe`** (gateway **`http://localhost:8080`**). Allowed **`type`** values: `CATEGORY`, `TAG`, `SPECIALTIES`, `MOOD_TAG`, `GENRE`.

---

### POST — create taxonomy

```powershell
$body = @{
    type        = "TAG"
    name        = "Example Tag"
    slug        = "example-tag"
    description = "Optional description"
    active      = $true
    actor       = "dev-user"
} | ConvertTo-Json -Compress

curl.exe -s -X POST "http://localhost:8080/api/v1/taxonomies" `
    -H "Content-Type: application/json" `
    -d $body |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

Save the returned id from `.data.id` for PUT/DELETE:

```powershell
$created = curl.exe -s -X POST "http://localhost:8080/api/v1/taxonomies" `
    -H "Content-Type: application/json" `
    -d $body | ConvertFrom-Json
$id = $created.data.id
$id
```

---

### PUT — update taxonomy (partial fields OK; **`actor` required**)

```powershell
$id = "00000000-0000-0000-0000-000000000000"   # replace with real taxonomy id

$body = @{
    name        = "Example Tag Renamed"
    description = "Updated notes"
    active      = $true
    actor       = "dev-user"
} | ConvertTo-Json -Compress

curl.exe -s -X PUT "http://localhost:8080/api/v1/taxonomies/$id" `
    -H "Content-Type: application/json" `
    -d $body |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

Change type/slug as needed:

```powershell
$body = @{
    type  = "MOOD_TAG"
    slug  = "example-mood"
    actor = "dev-user"
} | ConvertTo-Json -Compress

curl.exe -s -X PUT "http://localhost:8080/api/v1/taxonomies/$id" `
    -H "Content-Type: application/json" `
    -d $body |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

---

### DELETE — soft deactivate (`active=false`)

Default **`actor=system`** if omitted:

```powershell
$id = "00000000-0000-0000-0000-000000000000"

curl.exe -s -X DELETE "http://localhost:8080/api/v1/taxonomies/$id" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```

Explicit actor:

```powershell
curl.exe -s -X DELETE "http://localhost:8080/api/v1/taxonomies/$id?actor=dev-user" |
    ConvertFrom-Json |
    ConvertTo-Json -Depth 100
```
