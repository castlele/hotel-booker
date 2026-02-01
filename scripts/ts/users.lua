local ok = true

local function auth_user(username, password)
   local payload = json.encode { username = username, password = password }
   local resp =
      utils.http("POST", CFG.BOOKING_URL .. "/api/user/auth", "", payload)
   ok = t.assert_status(resp, 200, "auth " .. username .. " failed")
   local token = json.decode(resp.body).token or ""
   ok = t.assert_nonempty(token, "auth " .. username .. " returned no token")
   return token
end

local function delete_all_users(admin_token)
   local resp = utils.http("GET", CFG.BOOKING_URL .. "/api/user", admin_token)
   local body = json.decode(resp.body)

   if resp.status ~= 200 or #body <= 1 then
      return
   end

   log.ok("Prepare users for deleteion: " .. resp.body)

   for _, user in ipairs(body) do
      if user.role ~= "ADMIN" then
         local path = CFG.BOOKING_URL .. "/api/user/" .. user.id
         resp = utils.http("DELETE", path, admin_token)
      end
   end

   resp = utils.http("GET", CFG.BOOKING_URL .. "/api/user", admin_token)
end

local function register_user(username, password)
   local payload = json.encode { username = username, password = password }
   local resp =
      utils.http("POST", CFG.BOOKING_URL .. "/api/user/register", "", payload)
   ok = t.assert_status(resp, 200, "register " .. username .. " failed")
   local token = json.decode(resp.body).token or ""
   ok =
      t.assert_nonempty(token, "register " .. username .. " returned no token")
   return token
end

local function admin_user_can_be_authed()
   log.step("Given ADMIN token exists (auth)")

   local token = auth_user(CFG.ADMIN_USERNAME, CFG.ADMIN_PASSWORD)

   log.ok("ADMIN token acquired")

   return token
end

local function users_can_be_created(admin_token)
   delete_all_users(admin_token)

   log.step("Given USER1 exists (register)")
   local u1_token = register_user(CFG.USER1, CFG.USER_PASSWORD)
   log.ok("USER1 token acquired")

   log.step("Given USER2 exists (register)")
   local u2_token = register_user(CFG.USER2, CFG.USER_PASSWORD)
   log.ok("USER2 token acquired")

   return u1_token, u2_token
end

local admin_token = admin_user_can_be_authed()
local u1, u2 = users_can_be_created(admin_token)

return {
   ok = ok,
   admin = admin_token,
   u1_token = u1,
   u2_token = u2,
}
