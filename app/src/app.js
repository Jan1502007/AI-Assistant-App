const express = require('express');
const session = require('express-session');
const passport = require('./config/passport');
function ensureAuthenticated(req, res, next) {
  if (req.isAuthenticated()) return next();
  res.redirect('/login');
}

// usage
app.get('/dashboard', ensureAuthenticated, (req, res) => {
  res.send(`Welcome ${req.user.name}`);
});

const app = express();

app.use(session({
  secret: process.env.SESSION_SECRET,
  resave: false,
  saveUninitialized: false,
  cookie: { secure: process.env.NODE_ENV === 'production' } // true if HTTPS
}));

app.use(passport.initialize());
app.use(passport.session());

app.use('/auth', require('./routes/auth'));

app.listen(3000, () => console.log('Server running on port 3000'));