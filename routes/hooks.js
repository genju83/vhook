const express = require('express');
const router = express.Router();
const axios = require('axios').default;

router.get('/', function (req, res, next) {
    res.json({ result: "OK" });
});

router.post('/', function (req, res, next) {
    let url = req.body.url;
    let config = { headers: req.body.headers };
    let data = req.body.body;

    axios.post(url, data, config)
        .then(function (response) {
            res.json({ result: "OK" });
        }).catch(function (error) {
            console.log(error);
            res.json({ result: "NOK" });
        });
});

module.exports = router;
