local function shell_quote(s)
   return "'" .. tostring(s):gsub("'", "'\"'\"'") .. "'"
end

local function run_capture(cmd)
   local f = io.popen(cmd .. " 2>&1")

   if not f then
      return nil, "popen failed"
   end

   local out = f:read("*a") or ""
   local ok_, _, code = f:close()

   if ok_ == true then
      return out, 0
   end

   if type(code) == "number" then
      return out, code
   end

   return out, 1
end

local M = {
   colors = {
      ok = "1;32",
      warn = "1;33",
      error = "1;31",
   },
}

function M.http(method, url, token, body)
   local args = {
      "curl",
      "-sS",
      "-X",
      method,
      url,
      "-w",
      "\n__HTTP_CODE__:%{http_code}\n",
   }
   if token and token ~= "" then
      args[#args + 1] = "-H"
      args[#args + 1] = "Authorization: Bearer " .. token
   end
   if body and body ~= "" then
      args[#args + 1] = "-H"
      args[#args + 1] = "Content-Type: application/json"
      args[#args + 1] = "-d"
      args[#args + 1] = body
   end

   -- Build safe shell command
   local cmd_parts = {}
   for _, a in ipairs(args) do
      cmd_parts[#cmd_parts + 1] = shell_quote(a)
   end
   local cmd = table.concat(cmd_parts, " ")

   if LOGGING_REQUESTS then
      print(cmd)
   end

   local raw, code = run_capture(cmd)

   if LOGGING_REQUESTS then
      print(raw)
   end

   if raw == nil then
      return { status = 0, body = "", raw = "", error = "popen failed" }
   end

   -- Extract last __HTTP_CODE__
   local status = raw:match("__HTTP_CODE__:(%d+)%s*$")
   local body_out = raw:gsub("\n__HTTP_CODE__:%d+%s*$", "")
   local status_num = tonumber(status) or 0

   -- If curl itself failed (code != 0) but we got http_code maybe 000; keep status=0 for clarity
   if code ~= 0 and status_num == 0 then
      return {
         status = 0,
         body = body_out,
         raw = raw,
         error = "curl exit code " .. tostring(code),
      }
   end

   return { status = status_num, body = body_out, raw = raw, error = nil }
end

function M.getenv(name, default)
   local v = os.getenv(name)
   if v == nil or v == "" then
      return default
   end
   return v
end

function M.color(code, s)
   return string.char(27) .. "[" .. code .. "m" .. s .. string.char(27) .. "[0m"
end

function M.eprint(...)
   local t = {}
   for i = 1, select("#", ...) do
      t[#t + 1] = tostring(select(i, ...))
   end
   io.stderr:write(table.concat(t, " ") .. "\n")
end

return M
