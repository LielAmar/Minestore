// ========================================================
// routes/requests.js is a hub we use to handle requests
// such as: authentication, giving packages in game, etc.
// =======================================================
const express = require("express");
// const requestHandler = require("../handles/requestshandler");

// ========== [Variables] ========== \\
const router = express.Router();

// ========== [Initialization] ========== \\
// router.post("/authentication", requestHandler.authentication);
// router.post("/givepackage", requestHandler.givepackage);

module.exports = router;