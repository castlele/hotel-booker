LOGGING_REQUESTS = false

---@diagnostic disable: lowercase-global
json = require("libs.json")
log = require("logging")
utils = require("utils")
t = require("testing")

CFG = {
   BOOKING_URL = utils.getenv("BOOKING_URL", "http://localhost:8081"),
   HOTEL_URL = utils.getenv("HOTEL_URL", "http://localhost:8082"),

   ADMIN_USERNAME = utils.getenv("ADMIN_USERNAME", "admin"),
   ADMIN_PASSWORD = utils.getenv("ADMIN_PASSWORD", "admin"),

   USER1 = utils.getenv("USER1", "user1"),
   USER2 = utils.getenv("USER2", "user2"),
   USER_PASSWORD = utils.getenv("USER_PASSWORD", "password123"),

   START_DATE = utils.getenv("START_DATE", "2026-02-10"),
   END_DATE = utils.getenv("END_DATE", "2026-02-12"),
}

----------------
-- Test cases --
----------------

local users = require("ts.users")

if not users.ok then
   log.die("Users test cases failed")
   return
end

ADMIN_TOKEN = users.admin
USER1_TOKEN = users.u1_token
USER2_TOKEN = users.u2_token

local hotels = require("ts.hotels")

if not hotels.ok then
   log.die("Hotels test cases failed")
   return
end

log.step("ALL INTEGRATION TESTS PASSED")
