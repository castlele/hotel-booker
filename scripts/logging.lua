local M = {}

local utils = require("utils")

function M.step(msg)
   utils.eprint("\n" .. utils.color("1;34", "▶ " .. msg))
end

function M.ok(msg)
   utils.eprint(utils.color("1;32", "✔ " .. msg))
end

function M.warn(msg)
   utils.eprint(utils.color("1;33", "⚠ " .. msg))
end

function M.die(msg)
   utils.eprint(utils.color("1;31", "✘ " .. msg))
   os.exit(1)
end

return M
