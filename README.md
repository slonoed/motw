# Movie on the wheel
[movieonthewheel.slonoed.net](http://movieonthewheel.slonoed.net)
Clojure/script project that represent application for small bike rent business.
Frontend part based on reactjs and [rum](https://github.com/tonsky/rum) libratry.

## Good part
The main goal of this project is to try few approaches of building web app on clojure stack.
The ideas I was interested in:

### State management
Many complex frameworks cover data flow, but rum isn't. It is lightweight React.js wrapper.
I found that keep all state in one immutable atom is nice solution. You know exactly where is your data. Any state mutation caused by action (similar to flux, redux). Also is good to place all state mutations into strict bounds. I have *transforms* for these purposes. Transforms are just functions of `state -> state`. They don't have to be pure. But important part that if one transform need to change state asynchronously it have to dispatch new action.

### Rum interacting with third party libraries
Google Maps javascript API has object oriented code. I checked how functional clojure works with this kind of libraries. Spoiler: works perfect.
Clojure has useful tools for working with mutable data.

### Selenium tests
I decided to use same language for e2e testing. Clojure has nice [cls-webdrver](https://github.com/semperos/clj-webdriver).
API is clear and I will use this in my future projects. I have some issues with browsers, but this part more about my OS setup. FIrefox testing works perfect.

## Bad part
This project is not intended to implement real app, has limitations and need some improvements.
First of all real app that contains only features are present here don't need react.
Bundle size is ~100KB gziped. It is unacceptable for such small app. All these features can be written in plain JS without losing readability (but may lose it during growth).

Second issue with image cache. I didn't implement cache for maps images.
Therefore my credit card could be drained during high usage of app.

Third – data stored on disk and updates manually. Real product should update data time to time.

## Ugly? part
I skip some parts due to limited time or because they are not interesting for me in terms of education. These parts seems important and can be easily implemented.

No unit test here (but e2e tests). Unit tests are our friend, but I decide not to add them to this project. It small enough and clojure development pipeline assume developer tries every line of code in repl before put in file.

No server render. Rum allows use components code inside JVM, I already know how implement it and decide not to use it here. It easy to add server render because components don't use any browser specific API.

No sessions, DB, etc. I decide to work only with frontend here,

Location not always correct. I use GMaps to get lat&lng by location name, but some locations have wrong values.
I fixed few, but keep some to show situation when GMaps can't build route.

Marketing text not that good... Ok, they awful.

I don't have real rental business therefore :(

# Development
Install lein http://leiningen.org/.
Also you need to obtain Google Maps key and setup rules for embedded map API and direction API.

Start repl
```
GMAPS_KEY=your_gmaps_key lein repl
```
To start server, enter inside repl
```
(reset)
```
Every time you need to reload server code, just call `(reset)` in repl. No need to restart.

Start different repl for frontend
```
lein figwheel
```
Hot reload is turn on in dev mode. Save file and figwheel push changes to frontend.

# Build and run with docker
You can build `docker build -t motw .` docker container and run it:
```
docker run -d -p "8080:8080" -e "GMAPS_KEY=your_gmaps_key" -e "GTM_KEY=your_gtm_key" --name motw motw
```
GMAPS_KEY env variable for Google maps API.
GTM_KEY env variable for Google tag manager (eg. analytics).
