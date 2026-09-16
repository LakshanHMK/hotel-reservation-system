import urllib.request
import urllib.parse
import http.cookiejar
import json
import time
import subprocess
import sys

BASE_URL = "http://localhost:8080"
ts = int(time.time() * 1000)
EMAIL_A = f"qa_cust_a_{ts}@lankastay.test"
EMAIL_B = f"qa_cust_b_{ts}@lankastay.test"
PASS_A = "InitialPass#2026A!"
NEW_PASS_A = "UpdatedPass#2026A!"
PASS_B = "CustomerB#2026Pass!"

class TestClient:
    def __init__(self):
        self.cookie_jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.cookie_jar))
        self.csrf_token = None

    def get_csrf(self):
        req = urllib.request.Request(f"{BASE_URL}/api/v1/csrf")
        with self.opener.open(req) as resp:
            data = json.loads(resp.read().decode())
            self.csrf_token = data.get("token")
            return self.csrf_token

    def request(self, method, path, body=None, expect_status=200, include_csrf=True):
        url = f"{BASE_URL}{path}"
        headers = {"Content-Type": "application/json"}
        if include_csrf and self.csrf_token:
            headers["X-XSRF-TOKEN"] = self.csrf_token

        data_bytes = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(url, data=data_bytes, headers=headers, method=method)

        try:
            with self.opener.open(req) as resp:
                status = resp.getcode()
                raw_body = resp.read().decode()
                parsed = json.loads(raw_body) if raw_body else None
                if status != expect_status:
                    raise AssertionError(f"Expected status {expect_status} for {method} {path}, got {status}. Body: {raw_body}")
                return status, parsed
        except urllib.error.HTTPError as e:
            status = e.code
            raw_body = e.read().decode()
            try:
                parsed = json.loads(raw_body)
            except Exception:
                parsed = raw_body
            if status != expect_status:
                raise AssertionError(f"Expected status {expect_status} for {method} {path}, got {status}. Body: {raw_body}")
            return status, parsed

def run_mysql_query(query):
    cmd = [
        "mysql.exe",
        "-u", "lankastay_app",
        '-pLankaStay#App2026!',
        "lankastay_db",
        "-e", query
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, check=True)
    return res.stdout

def run_tests():
    print("=== STARTING COMPREHENSIVE CUSTOMER AUTH & PROFILE E2E TESTS ===")

    # 1. CSRF Token Fetch
    print("\n[STEP 1] Fetching CSRF Token...")
    client_a = TestClient()
    token = client_a.get_csrf()
    assert token, "CSRF token must not be empty"
    print(f"PASS: Got CSRF token ({token[:12]}...)")

    # 2. Registration QA Customer A
    print(f"\n[STEP 2] Registering Customer A: {EMAIL_A}...")
    reg_payload_a = {
        "firstName": "Kasun",
        "lastName": "Perera",
        "email": EMAIL_A,
        "phone": "+94711234567",
        "password": PASS_A
    }
    status, res_a = client_a.request("POST", "/api/v1/customer/auth/register", reg_payload_a, expect_status=201)
    cust_a_id = res_a["id"]
    assert cust_a_id, "Customer ID must be present"
    assert res_a["role"] == "CUSTOMER", f"Expected role CUSTOMER, got {res_a.get('role')}"
    assert res_a["status"] == "ACTIVE", f"Expected status ACTIVE, got {res_a.get('status')}"
    assert "password" not in res_a and "passwordHash" not in res_a, "Sensitive password fields must not leak"
    print(f"PASS: Customer A registered with ID: {cust_a_id}")

    # 3. Weak Password Rejection
    print("\n[STEP 3] Testing weak password rejection during registration...")
    weak_payload = {
        "firstName": "Weak",
        "lastName": "User",
        "email": f"weak_{ts}@lankastay.test",
        "phone": "+94711111111",
        "password": "weak"
    }
    status, res_weak = client_a.request("POST", "/api/v1/customer/auth/register", weak_payload, expect_status=400)
    print(f"PASS: Weak password rejected with 400 Bad Request: {res_weak}")

    # 4. Duplicate Registration Conflict
    print("\n[STEP 4] Testing duplicate registration conflict (same email)...")
    status, res_dup = client_a.request("POST", "/api/v1/customer/auth/register", reg_payload_a, expect_status=409)
    print(f"PASS: Duplicate email rejected with 409 Conflict: {res_dup}")

    # 5. Login Invalid Credentials
    print("\n[STEP 5] Testing login with invalid password...")
    wrong_login = {"email": EMAIL_A, "password": "WrongPassword#123"}
    status, res_inv = client_a.request("POST", "/api/v1/customer/auth/login", wrong_login, expect_status=401)
    print(f"PASS: Wrong password rejected with 401 Unauthorized: {res_inv}")

    # 6. Login Customer A
    print("\n[STEP 6] Logging in Customer A with valid credentials...")
    login_a = {"email": EMAIL_A, "password": PASS_A}
    status, res_login_a = client_a.request("POST", "/api/v1/customer/auth/login", login_a, expect_status=200)
    assert res_login_a["email"] == EMAIL_A
    print(f"PASS: Customer A logged in successfully: {res_login_a['email']}")

    # 7. Customer Session Hydration (/me)
    print("\n[STEP 7] Verifying Customer session hydration via /api/v1/customer/auth/me...")
    status, res_me = client_a.request("GET", "/api/v1/customer/auth/me", expect_status=200)
    assert res_me["id"] == cust_a_id
    assert res_me["email"] == EMAIL_A
    assert res_me["firstName"] == "Kasun"
    print(f"PASS: /api/v1/customer/auth/me returned correct authenticated customer: {res_me['firstName']} {res_me['lastName']}")

    # 8. Customer Profile Retrieval (/profile)
    print("\n[STEP 8] Retrieving profile via /api/v1/customer/profile...")
    status, res_prof = client_a.request("GET", "/api/v1/customer/profile", expect_status=200)
    assert res_prof["id"] == cust_a_id
    assert res_prof["firstName"] == "Kasun"
    print(f"PASS: /api/v1/customer/profile verified: {res_prof}")

    # 9. Update Profile & Direct MySQL Persistence Verification
    print("\n[STEP 9] Updating profile fields (firstName, lastName, phone)...")
    update_payload = {
        "firstName": "KasunUpdated",
        "lastName": "SilvaUpdated",
        "phone": "+94777888999",
        "email": EMAIL_A
    }
    status, res_updated = client_a.request("PUT", "/api/v1/customer/profile", update_payload, expect_status=200)
    assert res_updated["firstName"] == "KasunUpdated"
    assert res_updated["lastName"] == "SilvaUpdated"
    assert res_updated["phone"] == "+94777888999"
    print(f"PASS: Profile PUT returned updated values.")

    # Direct MySQL DB Verification
    print("Verifying persistence directly in MySQL database...")
    db_out = run_mysql_query(f"SELECT first_name, last_name, phone, role, status FROM customer_users WHERE email = '{EMAIL_A}';")
    print("MySQL Output:\n" + db_out.strip())
    assert "KasunUpdated" in db_out, "MySQL must reflect updated first_name"
    assert "SilvaUpdated" in db_out, "MySQL must reflect updated last_name"
    assert "+94777888999" in db_out, "MySQL must reflect updated phone"
    assert "CUSTOMER" in db_out, "Role must remain CUSTOMER"
    print("PASS: Verified MySQL database persistence directly!")

    # 10. Session Navigation / F5 Simulation
    print("\n[STEP 10] Simulating page refresh (F5) / subsequent profile GET request...")
    status, res_refreshed = client_a.request("GET", "/api/v1/customer/profile", expect_status=200)
    assert res_refreshed["firstName"] == "KasunUpdated"
    assert res_refreshed["lastName"] == "SilvaUpdated"
    assert res_refreshed["phone"] == "+94777888999"
    print("PASS: Profile remains updated across navigation/refresh.")

    # 11. Mass Assignment Escalation Defense
    print("\n[STEP 11] Testing Mass Assignment Attack (trying to escalate role to MANAGER, status to DISABLED)...")
    malicious_payload = {
        "firstName": "KasunHacker",
        "lastName": "SilvaHacker",
        "phone": "+94777888999",
        "email": EMAIL_A,
        "role": "MANAGER",
        "status": "DISABLED",
        "passwordHash": "$2a$10$maliciousfakehash"
    }
    status, res_mass = client_a.request("PUT", "/api/v1/customer/profile", malicious_payload, expect_status=200)
    assert res_mass["role"] == "CUSTOMER", "Role must NOT be escalated"
    assert res_mass["status"] == "ACTIVE", "Status must NOT be changed"

    db_check_role = run_mysql_query(f"SELECT role, status FROM customer_users WHERE email = '{EMAIL_A}';")
    assert "CUSTOMER" in db_check_role, "MySQL role must still be CUSTOMER"
    assert "ACTIVE" in db_check_role, "MySQL status must still be ACTIVE"
    print(f"PASS: Mass assignment attack thwarted. Role & status preserved in MySQL: {db_check_role.strip()}")

    # 12. Register Customer B & Test Profile Collision
    print(f"\n[STEP 12] Registering Customer B: {EMAIL_B}...")
    client_b = TestClient()
    client_b.get_csrf()
    reg_b = {
        "firstName": "Nimal",
        "lastName": "Fernando",
        "email": EMAIL_B,
        "phone": "+94722334455",
        "password": PASS_B
    }
    status, res_b = client_b.request("POST", "/api/v1/customer/auth/register", reg_b, expect_status=201)
    cust_b_id = res_b["id"]
    print(f"PASS: Customer B registered with ID: {cust_b_id}")

    print("Customer A attempts changing email to Customer B's email (collision test)...")
    dup_email_payload = {
        "firstName": "Kasun",
        "lastName": "Silva",
        "phone": "+94777888999",
        "email": EMAIL_B
    }
    status, res_coll = client_a.request("PUT", "/api/v1/customer/profile", dup_email_payload, expect_status=409)
    print(f"PASS: Email collision rejected with 409 Conflict: {res_coll}")

    # 13. Customer Isolation (Customer B profile isolation)
    print("\n[STEP 13] Logging in Customer B and checking profile isolation...")
    status, res_login_b = client_b.request("POST", "/api/v1/customer/auth/login", {"email": EMAIL_B, "password": PASS_B}, expect_status=200)
    status, prof_b = client_b.request("GET", "/api/v1/customer/profile", expect_status=200)
    assert prof_b["id"] == cust_b_id
    assert prof_b["email"] == EMAIL_B
    assert prof_b["firstName"] == "Nimal"
    assert prof_b["id"] != cust_a_id, "Customer B ID must differ from Customer A ID"
    print(f"PASS: Customer B profile is completely isolated from Customer A data.")

    # 14. Customer Password Change Lifecycle
    print("\n[STEP 14] Testing Customer Password Change lifecycle...")
    # 14a. Wrong current password
    wrong_pw_req = {
        "currentPassword": "WrongCurrentPassword#123",
        "newPassword": NEW_PASS_A,
        "confirmNewPassword": NEW_PASS_A
    }
    status, res_pw_wrong = client_a.request("POST", "/api/v1/customer/profile/change-password", wrong_pw_req, expect_status=400)
    print(f"PASS: Wrong current password rejected with 400: {res_pw_wrong}")

    # 14b. Weak new password
    weak_pw_req = {
        "currentPassword": PASS_A,
        "newPassword": "weak",
        "confirmNewPassword": "weak"
    }
    status, res_pw_weak = client_a.request("POST", "/api/v1/customer/profile/change-password", weak_pw_req, expect_status=400)
    print(f"PASS: Weak new password rejected with 400: {res_pw_weak}")

    # 14c. Mismatched confirm password
    mismatch_pw_req = {
        "currentPassword": PASS_A,
        "newPassword": NEW_PASS_A,
        "confirmNewPassword": "DifferentPassword#999!"
    }
    status, res_pw_mis = client_a.request("POST", "/api/v1/customer/profile/change-password", mismatch_pw_req, expect_status=400)
    print(f"PASS: Mismatched new password rejected with 400: {res_pw_mis}")

    # 14d. Successful password change
    valid_pw_req = {
        "currentPassword": PASS_A,
        "newPassword": NEW_PASS_A,
        "confirmNewPassword": NEW_PASS_A
    }
    status, res_pw_ok = client_a.request("POST", "/api/v1/customer/profile/change-password", valid_pw_req, expect_status=200)
    print(f"PASS: Password changed successfully: {res_pw_ok}")

    # 14e. Verify old password rejected
    client_a_new = TestClient()
    client_a_new.get_csrf()
    status, res_old_log = client_a_new.request("POST", "/api/v1/customer/auth/login", {"email": EMAIL_A, "password": PASS_A}, expect_status=401)
    print(f"PASS: Login with OLD password rejected with 401.")

    # 14f. Verify new password accepted
    status, res_new_log = client_a_new.request("POST", "/api/v1/customer/auth/login", {"email": EMAIL_A, "password": NEW_PASS_A}, expect_status=200)
    assert res_new_log["email"] == EMAIL_A
    print(f"PASS: Login with NEW password succeeded.")

    # 15. Logout & Session Invalidation
    print("\n[STEP 15] Testing Logout and session invalidation...")
    status, res_logout = client_a_new.request("POST", "/api/v1/customer/auth/logout", expect_status=200)
    print(f"PASS: Logout succeeded.")
    status, res_post_logout = client_a_new.request("GET", "/api/v1/customer/profile", expect_status=401)
    print(f"PASS: Subsequent GET /profile with old session rejected with 401 Unauthorized: {res_post_logout}")

    # 16. Customer Cannot Access Staff Management Endpoints
    print("\n[STEP 16] Verifying Customer cannot access Staff Management endpoints...")
    # Log in customer A again to have active session
    client_a_staff_check = TestClient()
    client_a_staff_check.get_csrf()
    client_a_staff_check.request("POST", "/api/v1/customer/auth/login", {"email": EMAIL_A, "password": NEW_PASS_A}, expect_status=200)

    for mgmt_endpoint in ["/api/v1/hotels", "/api/v1/admin/staff", "/api/v1/management/reviews"]:
        status, res_mgmt = client_a_staff_check.request("GET", mgmt_endpoint, expect_status=401)
        print(f"PASS: Customer access to {mgmt_endpoint} blocked with {status} Unauthorized.")

    # 17. Unauthenticated Access Blocked
    print("\n[STEP 17] Verifying unauthenticated request to /api/v1/customer/profile is blocked...")
    client_anon = TestClient()
    status, res_anon = client_anon.request("GET", "/api/v1/customer/profile", expect_status=401)
    print(f"PASS: Unauthenticated profile request blocked with 401: {res_anon}")

    print("\n=======================================================")
    print("ALL 17 LIVE CUSTOMER AUTH & PROFILE E2E TESTS PASSED 100%!")
    print("=======================================================")

if __name__ == "__main__":
    run_tests()
