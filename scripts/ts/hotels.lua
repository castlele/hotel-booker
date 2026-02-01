local ok = true

local function now_ms()
   return tostring(os.time()) .. "-" .. tostring(math.random(1000, 9999))
end

local function mk_reqid(prefix)
   prefix = prefix or "req"
   return prefix .. "-" .. now_ms()
end

local function create_hotel(admin_token, name, address)
   local payload = json.encode { name = name, address = address }
   local resp =
      utils.http("POST", CFG.HOTEL_URL .. "/api/hotels", admin_token, payload)
   ok = t.assert_status(resp, 200, "create hotel failed")
   local id = json.decode(resp.body).id
   ok = t.assert_nonempty(id, "hotel id missing")
   return id
end

local function create_room(admin_token, hotel_id, number_, available)
   local payload = json.encode {
      hotelId = tonumber(hotel_id),
      number = number_,
      available = available,
   }
   local resp =
      utils.http("POST", CFG.HOTEL_URL .. "/api/rooms", admin_token, payload)
   ok = t.assert_status(resp, 201, "create room failed")
   local id = json.decode(resp.body).id
   ok = t.assert_nonempty(id, "room id missing")
   return id
end

local function get_rooms(user_token)
   local resp = utils.http("GET", CFG.HOTEL_URL .. "/api/rooms", user_token)
   ok = t.assert_status(resp, 200, "GET /api/rooms failed")
   return json.decode(resp.body)
end

local function delete_all_rooms(admin_token)
   local rooms = get_rooms(admin_token)

   for _, room in ipairs(rooms) do
      utils.http(
         "DELETE",
         CFG.HOTEL_URL .. "/api/rooms/" .. room.id,
         admin_token
      )
   end
end

local function get_recommended_rooms(user_token)
   local r1 =
      utils.http("GET", CFG.HOTEL_URL .. "/api/rooms/recommended", user_token)

   return json.decode(r1.body)
end

local function create_booking(
   user_token,
   reqid,
   autoSelect,
   roomId,
   startDate,
   endDate
)
   local payload = {
      requestId = reqid,
      autoSelect = autoSelect,
      startDate = startDate,
      endDate = endDate,
   }

   if roomId ~= nil then
      payload.roomId = tonumber(roomId)
   end

   local resp = utils.http(
      "POST",
      CFG.BOOKING_URL .. "/api/booking",
      user_token,
      json.encode(payload)
   )

   ok = t.assert_status(
      resp,
      200,
      "create booking failed (reqId=" .. reqid .. ")"
   )

   local result = json.decode(resp.body)
   local id = result.id
   local status = result.status

   ok = t.assert_nonempty(id, "booking id missing (reqId=" .. reqid .. ")")
   ok = t.assert_nonempty(
      status,
      "booking status missing (reqId=" .. reqid .. ")"
   )

   return id, status
end

local function confirm_availability(
   admin_token,
   room_id,
   requestId,
   bookingId,
   startDate,
   endDate
)
   local payload = json.encode {
      requestId = requestId,
      bookingId = bookingId,
      startDate = startDate,
      endDate = endDate,
   }
   local resp = utils.http(
      "POST",
      CFG.HOTEL_URL
         .. "/api/rooms/"
         .. tostring(room_id)
         .. "/confirm-availability",
      admin_token,
      payload
   )
   return resp
end

local function release_lock(admin_token, room_id, requestId)
   local url = CFG.HOTEL_URL
      .. "/api/rooms/"
      .. tostring(room_id)
      .. "/release?requestId="
      .. requestId
   local resp = utils.http("POST", url, admin_token, "")
   return resp
end

-- Test cases

local function create_new_hotel()
   log.step("Creating hotel")
   local hotel_id = create_hotel(ADMIN_TOKEN, "Test Hotel", "Berlin")
   log.ok("Got a hotel with id: " .. hotel_id)

   return hotel_id
end

local function create_rooms_for_hotel(hotel_id)
   delete_all_rooms(ADMIN_TOKEN)
   log.step("Creating 3 hotel (2 available, 1 unavailable)")

   local room1_id = create_room(ADMIN_TOKEN, hotel_id, 101, true)
   log.ok("Got a room with id" .. room1_id)
   local room2_id = create_room(ADMIN_TOKEN, hotel_id, 102, true)
   log.ok("Got a room with id" .. room2_id)
   local room3_id = create_room(ADMIN_TOKEN, hotel_id, 103, false)
   log.ok("Got a room with id" .. room3_id)

   return room1_id, room2_id, room3_id
end

local function getting_rooms_returns_only_available()
   log.step("Scenario: GET /api/rooms returns ONLY available=true")
   local rooms = get_rooms(USER1_TOKEN)

   ok = t.assert_all_available_true(rooms)
   ok = t.assert_equal(
      2,
      #rooms,
      "Amount of rooms isn't 2, got instead: " .. #rooms
   )
   log.ok("All rooms available=true")
end

local function getting_recommended_rooms_returns_available_and_sorted(r1, r2)
   log.step(
      "Scenario: recommended rooms sorted by times_booked asc then id asc"
   )
   local _, s1 = create_booking(
      USER1_TOKEN,
      mk_reqid("bump"),
      false,
      r1,
      "2026-03-01",
      "2026-03-02"
   )
   local _, s2 = create_booking(
      USER1_TOKEN,
      mk_reqid("bump"),
      false,
      r1,
      "2026-03-03",
      "2026-03-04"
   )
   local _, s3 = create_booking(
      USER1_TOKEN,
      mk_reqid("bump"),
      false,
      r2,
      "2026-03-05",
      "2026-03-06"
   )

   if s1 ~= "CONFIRMED" or s2 ~= "CONFIRMED" or s3 ~= "CONFIRMED" then
      log.warn(
         "Some bookings not CONFIRMED; times_booked may not increment as expected"
      )
      ok = false
      return
   end

   local recommended = get_recommended_rooms(USER1_TOKEN)

   ok = t.assert_all_available_true(recommended)
   ok = t.assert_recommended_rooms_order(recommended)
   log.ok("recommended sorting OK")
end

local function booking_idempotency(hotel_id)
   log.step("Scenario: booking autoSelect=true is CONFIRMED + idempotency")

   local req = mk_reqid("auto")
   local tmp_room_id = create_room(ADMIN_TOKEN, hotel_id, 999, true)

   local booking_id, status = create_booking(
      USER1_TOKEN,
      req,
      true,
      tmp_room_id,
      CFG.START_DATE,
      CFG.END_DATE
   )

   if status ~= "CONFIRMED" then
      log.die("autoSelect booking must be CONFIRMED (got " .. status .. ")")
      ok = false
   end

   log.ok("Booking confirmed id=" .. booking_id)

   log.step(
      "Scenario: booking idempotency (same requestId) returns same booking id"
   )

   local booking2_id = select(
      1,
      create_booking(
         USER1_TOKEN,
         req,
         true,
         tmp_room_id,
         CFG.START_DATE,
         CFG.END_DATE
      )
   )

   if booking2_id ~= booking_id then
      log.die("idempotency broken: booking id changed")
      ok = false
   end

   log.ok("Booking idempotency OK")

   return booking_id
end

local function end_points_protection(booking_id)
   log.step("Scenario: protected endpoint without token => 401")

   do
      local resp = utils.http("GET", CFG.BOOKING_URL .. "/api/bookings", "", "")

      if resp.status ~= 401 then
         log.die("expected 401 without token, got " .. tostring(resp.status))
         ok = false
      end

      log.ok("401 without token OK")
   end

   log.step("Scenario: USER2 cannot read USER1 booking => 500")

   do
      local resp = utils.http(
         "GET",
         CFG.BOOKING_URL .. "/api/booking/" .. booking_id,
         USER2_TOKEN,
         ""
      )
      if resp.status ~= 500 then
         log.die("expected 500 other user, got " .. tostring(resp.status))
         ok = false
      end

      log.ok("access control OK")
   end
end

local function confirm_availability_by_admin(r1)
   log.step("Scenario: confirm-availability success (ADMIN)")
   local lock_req = mk_reqid("lock")
   local lock_resp = confirm_availability(
      ADMIN_TOKEN,
      r1,
      lock_req,
      "b-1",
      CFG.START_DATE,
      CFG.END_DATE
   )

   ok = t.assert_status(lock_resp, 200, "confirm-availability expected 200")

   local lock_id = json.decode(lock_resp.body).id
   local lock_status = json.decode(lock_resp.body).status

   ok = t.assert_nonempty(lock_id, "lock id missing")

   if lock_status ~= "HELD" then
      log.die("lock status must be HELD (got " .. tostring(lock_status) .. ")")
      ok = false
   end

   log.ok("lock created id=" .. lock_id)

   return lock_req, lock_id
end

local function overlapping_locks_availability(r1)
   log.step("Scenario: confirm-availability overlap => 409")
   local ov = confirm_availability(
      ADMIN_TOKEN,
      r1,
      mk_reqid("lock"),
      "b-2",
      "2026-02-11",
      "2026-02-13"
   )

   if ov.status ~= 409 then
      log.die("overlap must be 409 (got " .. tostring(ov.status) .. ")")
      ok = false
   end

   log.ok("overlap rejected 409")
end

local function lock_availability_idempotency(r1, lock_req, lock_id)
   log.step(
      "Scenario: confirm-availability idempotency (same requestId) => same lock id"
   )
   local lock_repeat = confirm_availability(
      ADMIN_TOKEN,
      r1,
      lock_req,
      "b-1",
      CFG.START_DATE,
      CFG.END_DATE
   )

   ok = t.assert_status(lock_repeat, 200, "idempotent confirm expected 200")

   local lockId2 = json.decode(lock_repeat.body).id

   if lockId2 ~= lock_id then
      log.die("idempotency broken: lock id changed")
      ok = false
   end

   log.ok("confirm idempotency OK")

   log.step("Scenario: release => RELEASED + idempotency")

   local rel = release_lock(ADMIN_TOKEN, r1, lock_req)

   ok = t.assert_status(rel, 200, "release expected 200")

   local relStatus = json.decode(rel.body).status

   if relStatus ~= "RELEASED" then
      log.die("release status must be RELEASED (got " .. relStatus .. ")")
      ok = false
   end

   log.ok("released")

   local rel2 = release_lock(ADMIN_TOKEN, r1, lock_req)

   ok = t.assert_status(rel2, 200, "release repeat expected 200")

   local relStatus2 = json.decode(rel2.body).status

   if relStatus2 ~= "RELEASED" then
      log.die("release repeat must remain RELEASED")
   end

   log.ok("release idempotency OK")

   log.step("Scenario: confirm again after release => 200")

   local c2 = confirm_availability(
      ADMIN_TOKEN,
      r1,
      mk_reqid("lock"),
      "b-3",
      CFG.START_DATE,
      CFG.END_DATE
   )

   ok = t.assert_status(c2, 200, "confirm after release expected 200")

   log.ok("confirm after release OK")
end

local function confirm_non_existing_room()
   log.step("Scenario: confirm non-existing room => 404")

   local c404 = confirm_availability(
      ADMIN_TOKEN,
      999999,
      mk_reqid("lock"),
      "b-404",
      CFG.START_DATE,
      CFG.END_DATE
   )

   if c404.status ~= 404 then
      log.die(
         "missing room must return 404 (got " .. tostring(c404.status) .. ")"
      )
      ok = false
   end

   log.ok("404 missing room OK")
end

local hotel_id = create_new_hotel()
local r1, r2, r3 = create_rooms_for_hotel(hotel_id)

log.ok(
   "Hotel=" .. hotel_id .. " Rooms=[" .. r1 .. "," .. r2 .. "," .. r3 .. "]"
)

getting_rooms_returns_only_available()
getting_recommended_rooms_returns_available_and_sorted(r1, r2)

local booking_id = booking_idempotency(hotel_id)
end_points_protection(booking_id)

local lock_req, lock_id = confirm_availability_by_admin(r1)

overlapping_locks_availability(r1)
lock_availability_idempotency(r1, lock_req, lock_id)
confirm_non_existing_room()

return {
   ok = ok,
}
