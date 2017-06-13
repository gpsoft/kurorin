# Kurorin

Kurorin is a web application relating to GitHub and Kindle. It's main purpose is my learning:

- Clojure
- ClojureScript
- boot
- re-frame
- Datomic

Besides that, Kurorin actually is a practical tool; it collects any top-page (aka README) on GitHub repositories and composes them into a Kindle mobi file. Because some of them are really good readings, you know.

# TODO (or just an idea)

- Async publishing
- Call kindlegen on the server
- Cache contents
- Download mobi files
- Save to/load from Datomic
- Dockerfile to run the server
- Go out of readmes
- Go out of GitHub
- Manage books
- More config
- Better UI
- Better jacket

# Configuration

    app-config.edn

# Developing

    $ boot dev

And browse `http://localhost:3000`.

# Release

    $ boot release
    $ java -Dconf="./app-config.edn" -jar release/kurorin.jar
And browse `http://localhost:3333`.

# Screenshot

![ss](ss.png)
