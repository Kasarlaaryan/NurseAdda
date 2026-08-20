#!/bin/bash
set +e

BASE="http://localhost:8080/api"
G='\033[0;32m'
R='\033[0;31m'
Y='\033[1;33m'
C='\033[0m'
PASS=0
FAIL=0

log_pass() { echo -e "  ${G}✅ PASS${NC} $1"; PASS=$((PASS+1)); }
log_fail() { echo -e "  ${R}❌ FAIL${NC} $1"; echo "     $2"; FAIL=$((FAIL+1)); }

# Helper: make request, save response to /tmp/res.json and http code to /tmp/code.txt
call() {
    local method="$1" url="$2" auth="$3" data="$4"
    local args="-s -w '\n%{http_code}' -X $method"
    if [ -n "$data" ]; then
        args="$args -H 'Content-Type: application/json' -d '$data'"
    fi
    if [ -n "$auth" ]; then
        args="$args -H 'Authorization: Bearer $auth'"
    fi
    eval curl $args "'$url'" 2>/dev/null > /tmp/call_out.txt
    local code=$(tail -1 /tmp/call_out.txt | tr -d "'")
    local body=$(sed '$d' /tmp/call_out.txt)
    echo "$body" > /tmp/res.json
    echo "$code" > /tmp/code.txt
}

check() {
    local label="$1" expected="$2"
    local code=$(cat /tmp/code.txt | tr -d '[:space:]')
    local body=$(cat /tmp/res.json)
    if [ "$code" = "$expected" ]; then
        log_pass "[$code] $label"
    else
        log_fail "[$code != $expected] $label" "$(echo $body | head -c 300)"
    fi
}

# JSON extract using grep+sed (no python needed)
jval() {
    grep -o "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" /tmp/res.json 2>/dev/null | head -1 | sed "s/\"$1\"[[:space:]]*:[[:space:]]*\"//" | sed 's/"$//'
}
jnum() {
    grep -o "\"$1\"[[:space:]]*:[[:space:]]*[0-9]*" /tmp/res.json 2>/dev/null | head -1 | sed "s/\"$1\"[[:space:]]*:[[:space:]]*//"
}

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  🏥 NurseAdda Full E2E Test (curl + bash)"
echo "═══════════════════════════════════════════════════════════"
echo ""

# ─── Clean Redis ───
echo -e "${Y}Cleaning Redis...${NC}"
docker exec nurseadda-redis redis-cli FLUSHALL > /dev/null 2>&1

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 1. REGISTER ADMIN ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/auth/register-admin" "" \
    '{"firstName":"Super","lastName":"Admin","email":"admin_e2e@nurseadda.com","phone":"9999999999","password":"Admin@123","confirmPassword":"Admin@123","adminType":"SUPER_ADMIN"}'
check "Register Admin" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 2. REGISTER CLIENT ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/auth/register-client" "" \
    '{"firstName":"Rahul","lastName":"Mehta","email":"rahul_e2e@hospital.com","mobileNumber":"9876543210","password":"Rahul@123","confirmPassword":"Rahul@123"}'
check "Register Client" "200"

sleep 1

# Get OTP from Redis
CLIENT_OTP=$(docker exec nurseadda-redis redis-cli GET "pending:registration:rahul_e2e@hospital.com" 2>/dev/null | grep -o '"code":"[^"]*"' | sed 's/"code":"//' | sed 's/"$//')
echo "   📧 Client OTP: $CLIENT_OTP"

call POST "$BASE/auth/verify-otp" "" \
    "{\"email\":\"rahul_e2e@hospital.com\",\"otp\":\"$CLIENT_OTP\"}"
check "Verify Client OTP" "200"

CLIENT_TOKEN=$(jval "accessToken")
echo "   🔑 Client Token: ${CLIENT_TOKEN:0:50}..."

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 3. REGISTER STAFF ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/auth/register-staff" "" \
    '{"fullName":"Priya Sharma","email":"priya_e2e@nurse.com","phone":"9876543211","staffCategory":"ICU Nurse","password":"Priya@123"}'
check "Register Staff" "200"

sleep 1

STAFF_OTP=$(docker exec nurseadda-redis redis-cli GET "pending:registration:priya_e2e@nurse.com" 2>/dev/null | grep -o '"code":"[^"]*"' | sed 's/"code":"//' | sed 's/"$//')
echo "   📧 Staff OTP: $STAFF_OTP"

call POST "$BASE/auth/verify-otp" "" \
    "{\"email\":\"priya_e2e@nurse.com\",\"otp\":\"$STAFF_OTP\"}"
check "Verify Staff OTP" "200"

STAFF_TOKEN=$(jval "accessToken")
echo "   🔑 Staff Token: ${STAFF_TOKEN:0:50}..."

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 4. ADMIN LOGIN ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/auth/login" "" \
    '{"email":"admin_e2e@nurseadda.com","password":"Admin@123"}'
check "Admin Login" "200"

ADMIN_TOKEN=$(jval "accessToken")
echo "   🔑 Admin Token: ${ADMIN_TOKEN:0:50}..."

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 5. GET /me ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/auth/me" "$CLIENT_TOKEN"
check "Client /me" "200"
echo "   → $(cat /tmp/res.json | head -c 100)"

call GET "$BASE/auth/me" "$STAFF_TOKEN"
check "Staff /me" "200"
echo "   → $(cat /tmp/res.json | head -c 100)"

call GET "$BASE/auth/me" "$ADMIN_TOKEN"
check "Admin /me" "200"
echo "   → $(cat /tmp/res.json | head -c 100)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 6. UPDATE PROFILES ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call PUT "$BASE/auth/client-profile" "$CLIENT_TOKEN" \
    '{"firstName":"Rahul Kumar","lastName":"Mehta","mobileNumber":"9876543211"}'
check "Update Client Profile" "200"
echo "   → $(cat /tmp/res.json | head -c 150)"

call GET "$BASE/auth/client-profile" "$CLIENT_TOKEN"
check "Get Client Profile" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 7. ADMIN VERIFIES STAFF ═══${NC}"
# ═══════════════════════════════════════════════════════════════

# Get staff user ID from /me
call GET "$BASE/auth/me" "$STAFF_TOKEN"
STAFF_USER_ID=$(jnum "id")
echo "   👤 Staff User ID: $STAFF_USER_ID"

call PATCH "$BASE/auth/staff/$STAFF_USER_ID/verification?verified=true" "$ADMIN_TOKEN"
check "Admin Verifies Staff" "200"
echo "   → verified=$(grep -o '"verified":[a-z]*' /tmp/res.json)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 8. CLIENT CREATES STAFFING REQUEST ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/staffing-requests" "$CLIENT_TOKEN" \
    '{"designation":"ICU Nurse","location":"Mumbai","shift":"Morning Shift","startDate":"2026-09-01","endDate":"2026-09-30","numberOfStaff":1,"requiredSkills":"Ventilator Management"}'
check "Create Staffing Request" "201"

REQUEST_ID=$(jnum "id")
echo "   📋 Request ID: $REQUEST_ID"
echo "   → advance=$(grep -o '"advanceAmount":[0-9.]*' /tmp/res.json)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 8b. CLIENT PAYS 40% ADVANCE ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/staffing-requests/$REQUEST_ID/pay-advance?razorpayOrderId=order_test_001&razorpayPaymentId=pay_test_001&razorpaySignature=test_sig_001" "$CLIENT_TOKEN"
check "Client Pays Advance" "200"
echo "   → advancePaid=$(grep -o '"advancePaid":[a-z]*' /tmp/res.json)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 8c. LISTS ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/staffing-requests" "$CLIENT_TOKEN"
check "Client Lists Requests" "200"

call GET "$BASE/staffing-requests" "$ADMIN_TOKEN"
check "Admin Lists All Requests" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 9. STAFF SEES & ACCEPTS REQUEST ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/staffing-requests/pending" "$STAFF_TOKEN"
check "Staff Sees Pending Requests" "200"
echo "   → pending count: $(grep -co '"id"' /tmp/res.json)"

call POST "$BASE/staffing-requests/$REQUEST_ID/accept" "$STAFF_TOKEN"
check "Staff Accepts Request" "201"

ASSIGNMENT_ID=$(jnum "id")
echo "   📋 Assignment ID: $ASSIGNMENT_ID"
echo "   → status=$(grep -o '"status":"[^"]*"' /tmp/res.json | head -1)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 10. VIEW ASSIGNMENTS ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/assignments" "$ADMIN_TOKEN"
check "Admin Lists Assignments" "200"

call GET "$BASE/assignments" "$STAFF_TOKEN"
check "Staff Lists Assignments" "200"

call GET "$BASE/assignments" "$CLIENT_TOKEN"
check "Client Lists Assignments" "200"

call GET "$BASE/assignments/$ASSIGNMENT_ID" "$ADMIN_TOKEN"
check "Get Assignment By ID" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 11. STAFF DETAILS ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/assignments/$ASSIGNMENT_ID/staff-details" "$ADMIN_TOKEN"
check "Admin Views Staff Details" "200"
echo "   → $(cat /tmp/res.json | head -c 200)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 12. ADMIN APPROVES TO CLIENT ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call PATCH "$BASE/assignments/$ASSIGNMENT_ID/approve-to-client" "$ADMIN_TOKEN"
check "Admin Approves to Client" "200"
echo "   → sentToClient=$(grep -o '"sentToClient":[a-z]*' /tmp/res.json)"

call GET "$BASE/assignments/$ASSIGNMENT_ID/staff-details" "$CLIENT_TOKEN"
check "Client Views Staff Details" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 13. STAFF CHECK-IN ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/attendance/checkin" "$STAFF_TOKEN" \
    "{\"assignmentId\":$ASSIGNMENT_ID,\"notes\":\"Starting ICU duty\"}"
check "Staff Check-In" "201"

ATTENDANCE_ID=$(jnum "id")
echo "   📋 Attendance ID: $ATTENDANCE_ID"

call GET "$BASE/attendance/today" "$STAFF_TOKEN"
check "Staff Today Attendance" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 14. STAFF CHECK-OUT ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/attendance/checkout/$ATTENDANCE_ID" "$STAFF_TOKEN" \
    '{"notes":"Completed shift"}'
check "Staff Check-Out" "200"
echo "   → hours=$(grep -o '"workingHours":[0-9.]*' /tmp/res.json)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 15. INVOICES & PAYMENTS ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call GET "$BASE/invoices" "$ADMIN_TOKEN"
check "Admin Lists Invoices" "200"
echo "   → invoices: $(grep -co '"id"' /tmp/res.json)"

call GET "$BASE/payments" "$STAFF_TOKEN"
check "Staff Lists Payments" "200"
echo "   → payments: $(grep -co '"id"' /tmp/res.json)"

call GET "$BASE/billing/summary" "$ADMIN_TOKEN"
check "Billing Summary" "200"
echo "   → $(cat /tmp/res.json | head -c 200)"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 16. INVOICE PDF DOWNLOAD ═══${NC}"
# ═══════════════════════════════════════════════════════════════

# Get first invoice ID
call GET "$BASE/invoices" "$ADMIN_TOKEN"
INVOICE_ID=$(grep -o '"id":[0-9]*' /tmp/res.json | head -1 | sed 's/"id"://')
echo "   📄 Invoice ID: $INVOICE_ID"

if [ -n "$INVOICE_ID" ]; then
    HTTP_CODE=$(curl -s -o /tmp/invoice.pdf -w '%{http_code}' "$BASE/invoices/$INVOICE_ID/pdf" -H "Authorization: Bearer $ADMIN_TOKEN")
    if [ "$HTTP_CODE" = "200" ] && [ -f /tmp/invoice.pdf ]; then
        PDF_SIZE=$(wc -c < /tmp/invoice.pdf)
        if [ "$PDF_SIZE" -gt 1000 ]; then
            log_pass "[$HTTP_CODE] Download Invoice PDF ($PDF_SIZE bytes)"
        else
            log_fail "[$HTTP_CODE] Download Invoice PDF" "PDF too small ($PDF_SIZE bytes)"
        fi
    else
        log_fail "[$HTTP_CODE] Download Invoice PDF" "Expected 200"
    fi
else
    echo -e "  ⏭️  SKIP No invoice to download"
fi

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 17. ASSIGNMENT STATUS: COMPLETED ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call PUT "$BASE/assignments/$ASSIGNMENT_ID/status" "$STAFF_TOKEN" '{"status":"COMPLETED"}'
check "Staff Sets Assignment COMPLETED" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 18. RATE CONFIG ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call POST "$BASE/rates" "$ADMIN_TOKEN" \
    '{"shiftType":"Morning Shift","staffHourlyRate":200,"clientHourlyRate":500,"overtimeMultiplier":1.5}'
check "Create Rate Config" "201"

call GET "$BASE/rates" "$ADMIN_TOKEN"
check "Get All Rates" "200"
echo "   → rates: $(grep -co '"shiftType"' /tmp/res.json)"

call GET "$BASE/rates/Morning%20Shift" "$ADMIN_TOKEN"
check "Get Rate By Shift" "200"

# ═══════════════════════════════════════════════════════════════
echo -e "${Y}═══ 19. SECURITY TESTS ═══${NC}"
# ═══════════════════════════════════════════════════════════════

call PATCH "$BASE/assignments/$ASSIGNMENT_ID/approve-to-client" "$STAFF_TOKEN"
check "Staff Cannot Approve to Client (403)" "403"

call POST "$BASE/assignments" "$CLIENT_TOKEN" '{"staffProfileId":1,"staffingRequestId":1}'
check "Client Cannot Create Assignment (403)" "403"

call GET "$BASE/staffing-requests/status/PENDING" "$CLIENT_TOKEN"
check "Client Cannot Filter Status (403)" "403"

call GET "$BASE/staffing-requests/pending" "$ADMIN_TOKEN"
check "Admin Cannot See Pending Staff Requests (403)" "403"

# ═══════════════════════════════════════════════════════════════
echo ""
echo "═══════════════════════════════════════════════════════════"
echo -e "  🏁 RESULTS: ${G}$PASS passed${NC}, ${R}$FAIL failed${NC} out of $((PASS+FAIL))"
echo "═══════════════════════════════════════════════════════════"

if [ $FAIL -eq 0 ]; then
    echo -e "  ${G}🎉 ALL TESTS PASSED!${NC}"
else
    echo -e "  ${R}⚠️  $FAIL TEST(S) FAILED${NC}"
fi
