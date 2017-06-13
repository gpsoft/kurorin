# Kurorin

Kurorin is an app relating to GitHub and Kindle. It uses:

- Clojure
- ClojureScript
- boot
- re-frame

# Developing

    $ boot dev

Served on port 3000.

# Release

    $ boot release
    $ java -Dconf="./app-config.edn" -jar release/kurorin.jar

Served on port 3333.

