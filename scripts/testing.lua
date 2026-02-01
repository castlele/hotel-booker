local log = require("logging")

local function assert_status(resp, expected, context)
   if resp.status ~= expected then
      log.die(
         context
            .. " (expected "
            .. expected
            .. ", got "
            .. tostring(resp.status)
            .. ")"
      )
      return false
   end

   return true
end

local function assert_nonempty(v, msg)
   if v == nil or v == "" or v == "null" then
      log.die(msg)
      return false
   end

   return true
end

local function assert_recommended_rooms_order(rooms)
   if #rooms <= 1 then
      log.warn("Too few rooms to assert recommendations")
      return false
   end

   local prev = rooms[1]

   for index = 2, #rooms do
      if prev.timesBooked > rooms[index].timesBooked then
         log.die("Not an ascending order of timesBooks")
         return false
      end

      prev = rooms[index]
   end

   return true
end

local function assert_all_available_true(rooms)
   for _, room in ipairs(rooms) do
      if room.available ~= true then
         log.die("found available != true")

         return false
      end
   end

   return true
end

local function assert_equals(expected, real, msg)
   if expected ~= real then
      log.die(msg or "assert_equal error")

      return false
   end

   return true
end

return {
   assert_recommended_rooms_order = assert_recommended_rooms_order,
   assert_equal = assert_equals,
   assert_status = assert_status,
   assert_nonempty = assert_nonempty,
   assert_all_available_true = assert_all_available_true,
}
