Reagent Server Side rendering:
=====

From here: https://github.com/reagent-project/reagent-cookbook

### how to run:

```bash
$ lein cljsbuild auto
# another browser
$ lein ring server-headless
```

### Pages

* **[/server-data](http://localhost:3000/server-data)** use data that is seeded from the server, render it in clojurescript
* **[/autocomplete](http://localhost:3000/autocomplete)** an example of calling out to jQuery
* **[/compare-argv](http://localhost:3000/compare-argv)** not so interesting
* **[/about](http://localhost:3000/about)** just another page

### Stuff

* **src/clj/reagent_server_rendering/handler.clj** the nashorn stuff to render the cljs on the server, and some ring routes
* **src/cljs/reagent_server_rendering/core.cljs** this is where the rendering starts. Client side routing, etc. has 2 important exports: `main`, for client side app init, and `render-page` that is used by the server side renderer
* **src/cljs/reagent_server_rendering/pages.cljs** reagent components used for each page

Ideas
----
* Client side routing (done, though the routes have to be duplicated)
* How to add Figwheel + HMR?
