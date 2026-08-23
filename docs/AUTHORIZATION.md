# Dashboard authentication authorization matrix

`ASSIGNED_SCOPE_ONLY` records the intended property boundary. The authentication foundation persists that boundary; future hotel-domain services must enforce it on each resource.

| Endpoint | Anonymous | MANAGER | HOTEL_STAFF | RECEPTIONIST |
|---|---|---|---|---|
| `GET /api/v1/auth/csrf` | ALLOW | ALLOW | ALLOW | ALLOW |
| `POST /api/v1/auth/login` | ALLOW | ALLOW | ALLOW | ALLOW |
| `GET /api/v1/auth/me` | DENY | ALLOW | ALLOW | ALLOW |
| `POST /api/v1/auth/change-initial-password` | DENY | SELF_ONLY | SELF_ONLY | SELF_ONLY |
| `POST /api/v1/auth/change-password` | DENY | SELF_ONLY | SELF_ONLY | SELF_ONLY |
| `POST /api/v1/auth/logout` | DENY | SELF_ONLY | SELF_ONLY | SELF_ONLY |
| `GET /api/v1/admin/staff` | DENY | ALLOW | DENY | DENY |
| `GET /api/v1/admin/staff/{id}` | DENY | ALLOW | DENY | DENY |
| `POST /api/v1/admin/staff` | DENY | ALLOW | DENY | DENY |
| `PATCH /api/v1/admin/staff/{id}` | DENY | ALLOW | DENY | DENY |
| `PATCH /api/v1/admin/staff/{id}/status` | DENY | ALLOW | DENY | DENY |
| `POST /api/v1/admin/staff/{id}/reset-password` | DENY | ALLOW | DENY | DENY |
| Existing `/api/management/destinations/**` | DENY | ALLOW | ASSIGNED_SCOPE_ONLY | DENY |
| Existing `POST /api/media/upload` | DENY | ALLOW | ASSIGNED_SCOPE_ONLY | DENY |

Management operations cannot target the acting manager or another `MANAGER`. Public staff registration does not exist.
