Reagent Server Side rendering:
=====

From here: https://github.com/reagent-project/reagent-cookbook

### Why is this nice?

1. Run 'version 1' below, the figwheel + Hot Module Reloading concept
2. run `curl http://localhost:3000/server-data` in a shell, receive a completely server rendered page
3. Change components in src/cljs/reagent_server_rendering/page.cljs and watch the page update them without losing state.

### How to Run:

Version 1: Run with figwheel for REPL + Hot Module reloading. In 3 separate terminal windows
```bash
# start the server side JS build
$ lein cljsbuild auto server-side
# serve the ring handler + static files
$ lein ring server-headless
# start repl, HMR + auto rebuild
$ lein figwheel hmr
```

Version 2: Run without watching and hot module reload, in production\* mode
```bash
# build cljs
$ lein cljsbuild once prod server-side
# start ring server, tell it to use prod js
$ PROD=true lein ring server-headless
```

\* = this setup is in no way production ready

### Pages

* **[/server-data](http://localhost:3000/server-data)** use data that is seeded from the server, render it in clojurescript
* **[/autocomplete](http://localhost:3000/autocomplete)** an example of calling out to jQuery
* **[/compare-argv](http://localhost:3000/compare-argv)** not so interesting
* **[/about](http://localhost:3000/about)** just another page

### Stuff

* **src/clj/reagent_server_rendering/handler.clj** the nashorn stuff to render the cljs on the server, and some ring routes
* **src/cljs/reagent_server_rendering/core.cljs** this is where the rendering starts. Client side routing, etc. has 2 important exports: `main`, for client side app init, and `render-page` that is used by the server side renderer
* **src/cljs/reagent_server_rendering/pages.cljs** reagent components used for each page

### Defining routes

Routes are only defined in CLJS, in reagent-server-rendering.core there's a `pages` map that looks like this:
```clojure
(def pages
  {"home"  pages/home-page
   "about" pages/about-page
   "compare-argv" pages/argv-page
   "server-data" pages/server-data
   "autocomplete" pages/auto-page
   "404" pages/not-found })
```
the string key becomes the URL, the value is a reagent component

### Ideas:
----
- Collect links that i've used
- Discuss alternatives to the nashorn js engine pool
- Run server-rendering in node?
- Display this readme on the homepage
- JS Unit tests
