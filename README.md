# ring-proxy-example

An example of a clojure ring web application with a proxy inside.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

Change `handler.clj` and set your own proxy routes to the file.

My example routes everything to another web server at `http://localhost:8765`

```clojure
(wrap-proxy "/" "http://localhost:8765")
```

To start a web server for the application, run:

```bash
lein ring server
```

## License

APL 2.0
