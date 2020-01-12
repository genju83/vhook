var express = require('express');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var hooks = require('./routes/hooks');

var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser())

app.use('/api/v1/hooks', hooks);

module.exports = app;
