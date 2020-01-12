const express = require('express');
const router = express.Router();

router.get('/', function (req, res, next) {
    res.json({ greetings: "hello vhook" });
});

module.exports = router;
