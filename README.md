# Carrier
[![Travis](https://img.shields.io/travis/stevesoltys/carrier.svg)](https://travis-ci.org/stevesoltys/carrier.svg?branch=master)
[![codecov](https://img.shields.io/codecov/c/github/stevesoltys/carrier.svg)](https://codecov.io/gh/stevesoltys/carrier)
[![Codacy Badge](https://img.shields.io/codacy/grade/88a25f1833a647cf8161bdc136fd117c.svg)](https://www.codacy.com/app/stevesoltys/carrier/dashboard)

Carrier is a self-hosted e-mail service which can generate 'masked' addresses.
When mail is sent to a masked address, it’s forwarded to your actual address.
When you notice spam coming from a masked address, just block it!

## Installation
Since this is a mail server/client and uses an external database, installation does require configuration.

Check out [the wiki](https://github.com/stevesoltys/carrier/wiki) for more information.

## Configuration
By default, Carrier will look for a JSON configuration file in your home directory: ```$HOME/.config/carrier/config.json```.

An example configuration file can be seen below:

```json
{
    "server": {
        "localhost": "server.mydomain.com",
        "force_tls": true
    },

    "client": {
        "dkim": true,
        "dkim_selector": "mail",
        "dkim_private_key": "/home/tomcat7/.config/carrier/dkim.der",

        "domain": "mydomain.com",
        "keystore": "/home/tomcat7/.config/carrier/keystore",
        "keystore_password": "1234567"
    },

    "accounts": [
        { "username": "username", "password": "password" }
    ]
}
```

Check out [this page](https://github.com/stevesoltys/carrier/wiki/Configuration) for more information.

## Development
After checking out the repo, run `gradle build` to install dependencies and build the project. You can run `gradle war` to
build a WAR file for deployment.

## Contributing
Bug reports and pull requests are welcome on GitHub at https://github.com/stevesoltys/carrier. This project is intended to
be a safe, welcoming space for collaboration, and contributors are expected to adhere to the
[Contributor Covenant](http://contributor-covenant.org) code of conduct.

## License
This application is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
