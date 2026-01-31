#!/bin/bash

BOOKING_URL="${BOOKING_URL:-http://localhost:8081}"
HOTEL_URL="${HOTEL_URL:-http://localhost:8082}"

USER_USERNAME="${USER_USERNAME:-user1}"
USER_PASSWORD="${USER_PASSWORD:-password123}"

ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"

START_DATE="${START_DATE:-2026-02-10}"
END_DATE="${END_DATE:-2026-02-12}"

json() {
    jq -c .
}

http() {
    local method="$1"
    local url="$2"
    local token="${3:-}"
    local body="${4:-}"

    if [[ -n "$token" && -n "$body" ]]; then
        curl -sS -X "$method" "$url" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$body"
    elif [[ -n "$token" ]]; then
        curl -sS -X "$method" "$url" \
            -H "Authorization: Bearer $token"
    elif [[ -n "$body" ]]; then
        curl -sS -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$body"
    else
        curl -sS -X "$method" "$url"
    fi
}

echo "== 1) USER register (Booking Service): POST /api/user/register =="
USER_REGISTER_RES=$(http POST "$BOOKING_URL/api/user/register" "" \
    "{\"username\":\"$USER_USERNAME\",\"password\":\"$USER_PASSWORD\"}")
echo "$USER_REGISTER_RES" | json
USER_TOKEN=$(echo "$USER_REGISTER_RES" | jq -r .token)
echo "USER_TOKEN acquired: $USER_TOKEN"
echo

echo "== 2) USER auth (Booking Service): POST /user/auth =="
USER_AUTH_RES=$(http POST "$BOOKING_URL/api/user/auth" "" \
    "{\"username\":\"$USER_USERNAME\",\"password\":\"$USER_PASSWORD\"}")
echo "$USER_AUTH_RES" | json
USER_TOKEN2=$(echo "$USER_AUTH_RES" | jq -r .token)
echo "USER_TOKEN2 acquired (optional)"
echo

echo "== 3) Create ADMIN user via ADMIN endpoint (requires ADMIN token) =="
ADMIN_AUTH_RES=$(http POST "$BOOKING_URL/api/user/auth" "" \
    "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}")

ADMIN_TOKEN=$(echo "$ADMIN_AUTH_RES" | jq -r '.token // empty')

echo "ADMIN_TOKEN acquired"
echo

if [[ -n "$ADMIN_TOKEN" ]]; then
    echo "== 4) HOTEL: POST /api/hotels (ADMIN) =="
    HOTEL_CREATE_RES=$(http POST "$HOTEL_URL/api/hotels" "$ADMIN_TOKEN" \
        "{\"name\":\"Hotel One\",\"address\":\"Berlin\"}")
    echo "$HOTEL_CREATE_RES" | json
    HOTEL_ID=$(echo "$HOTEL_CREATE_RES" | jq -r .id)
    echo "HOTEL_ID=$HOTEL_ID"
    echo

    echo "== 5) HOTEL: POST /api/rooms (ADMIN) create 2 rooms =="
    ROOM1=$(http POST "$HOTEL_URL/api/rooms" "$ADMIN_TOKEN" \
        "{\"hotelId\":$HOTEL_ID,\"number\":101,\"available\":true}")
    echo "$ROOM1" | json
    ROOM1_ID=$(echo "$ROOM1" | jq -r .id)

    ROOM2=$(http POST "$HOTEL_URL/api/rooms" "$ADMIN_TOKEN" \
        "{\"hotelId\":$HOTEL_ID,\"number\":102,\"available\":true}")
    echo "$ROOM2" | json
    ROOM2_ID=$(echo "$ROOM2" | jq -r .id)

    echo "ROOM1_ID=$ROOM1_ID, ROOM2_ID=$ROOM2_ID"
    echo
else
    echo "Skipping ADMIN Related tests"
fi

echo "== 6) HOTEL: GET /api/hotels (USER or public if permitted) =="
HOTELS=$(http GET "$HOTEL_URL/api/hotels" "$USER_TOKEN")
echo "$HOTELS" | json
echo

echo "== 7) HOTEL: GET /api/rooms (free rooms) =="
ROOMS_FREE=$(http GET "$HOTEL_URL/api/rooms" "$USER_TOKEN")
echo "$ROOMS_FREE" | json
echo

echo "== 8) HOTEL: GET /api/rooms/recommended (sorted) =="
ROOMS_REC=$(http GET "$HOTEL_URL/api/rooms/recommended" "$USER_TOKEN")
echo "$ROOMS_REC" | json
echo

echo "== 9) BOOKING: POST /api/booking (USER) autoSelect=true, roomId ignored =="

REQ_ID="req-$(date +%s)-$(openssl rand -hex 4 2>/dev/null || echo $RANDOM)"

BOOKING_RAW=$(curl -sS -w "\nHTTP_STATUS:%{http_code}\n" -X POST "$BOOKING_URL/api/booking" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"requestId\":\"$REQ_ID\",\"autoSelect\":true,\"roomId\":999999,\"startDate\":\"2026-02-10\",\"endDate\":\"2026-02-12\"}")

BOOKING_BODY=$(echo "$BOOKING_RAW" | sed '/^HTTP_STATUS:/d')

BOOKING1_ID=$(echo "$BOOKING_BODY" | jq -r '.id // empty')

echo "BOOKING1_ID=$BOOKING1_ID"
echo

#
# echo "== 10) BOOKING: Idempotency повтор того же requestId -> должен вернуть тот же booking =="
# BOOKING1_REPEAT=$(http POST "$BOOKING_URL/api/booking" "$USER_TOKEN" \
#     "{\"requestId\":\"$REQ_ID_1\",\"autoSelect\":true,\"roomId\":999,\"startDate\":\"$START_DATE\",\"endDate\":\"$END_DATE\"}")
# echo "$BOOKING1_REPEAT" | json
# echo
#
# echo "== 11) BOOKING: POST /api/booking (USER) autoSelect=false with explicit roomId =="
# REQ_ID_2="req-$(date +%s)-2"
# # roomId берём из рекомендованных, если есть
# CHOSEN_ROOM_ID=$(echo "$ROOMS_REC" | jq -r '.[0].id // empty' || true)
# if [[ -z "${CHOSEN_ROOM_ID:-}" ]]; then
#     # fallback: try from earlier created
#     CHOSEN_ROOM_ID="${ROOM1_ID:-1}"
# fi
#
# BOOKING2=$(http POST "$BOOKING_URL/api/booking" "$USER_TOKEN" \
#     "{\"requestId\":\"$REQ_ID_2\",\"autoSelect\":false,\"roomId\":$CHOSEN_ROOM_ID,\"startDate\":\"$START_DATE\",\"endDate\":\"$END_DATE\"}")
# echo "$BOOKING2" | json
# BOOKING2_ID=$(echo "$BOOKING2" | jq -r .id)
# echo "BOOKING2_ID=$BOOKING2_ID"
# echo
#
# echo "== 12) BOOKING: GET /api/bookings history (USER) =="
# HISTORY=$(http GET "$BOOKING_URL/api/bookings" "$USER_TOKEN")
# echo "$HISTORY" | json
# echo
#
# echo "== 13) BOOKING: GET /api/booking/{id} (USER) =="
# ONE=$(http GET "$BOOKING_URL/api/booking/$BOOKING1_ID" "$USER_TOKEN")
# echo "$ONE" | json
# echo
#
# echo "== 14) BOOKING: DELETE /api/booking/{id} cancel (USER) =="
# CANCEL_REQ="cancel-$(date +%s)"
# http DELETE "$BOOKING_URL/api/booking/$BOOKING1_ID" "$USER_TOKEN" "" \
#     -H "X-Request-Id: $CANCEL_REQ" || true
# echo "Cancelled booking id=$BOOKING1_ID"
# echo
#
# echo "== 15) BOOKING: GET /api/booking/{id} after cancel => status should be CANCELLED =="
# ONE2=$(http GET "$BOOKING_URL/api/booking/$BOOKING1_ID" "$USER_TOKEN")
# echo "$ONE2" | json
# echo
#
# echo "== 16) NEGATIVE: call protected endpoint without token => 401 =="
# set +e
# curl -i -sS "$BOOKING_URL/api/bookings" | head -n 20
# set -e
# echo
#
# echo "== 17) NEGATIVE: call /api/booking/{id} of another user => 403 (requires second user) =="
# echo "Creating second user..."
# U2="user2"
# U2P="password123"
# U2REG=$(http POST "$BOOKING_URL/user/register" "" "{\"username\":\"$U2\",\"password\":\"$U2P\"}")
# U2TOKEN=$(echo "$U2REG" | jq -r .token)
#
# echo "Second user tries to read booking of first user:"
# set +e
# curl -i -sS -H "Authorization: Bearer $U2TOKEN" "$BOOKING_URL/api/booking/$BOOKING2_ID" | head -n 20
# set -e
# echo
#
# echo "== 18) NEGATIVE (dates conflict / 409) =="
# echo "This depends on your real Hotel confirm-availability/locking logic."
# echo "If you implemented it, create two bookings overlapping same room/dates and expect 409."
# echo "Otherwise skip for now."
# echo

echo "DONE."
