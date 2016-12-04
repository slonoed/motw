.PHONY: all js css

all: js css

# js
FIND_CLJS_SOURCES=find src -name \*.cljc -or -name \*.cljs

js: resources/public/js/motw.js

resources/public/js/motw.js: $(shell { $(FIND_CLJS_SOURCES); echo project.clj; }) |  js_dir
	lein with-profile ui cljsbuild once prod

js_dir:
	@mkdir -p resources/public/js


# Styles
FIND_SCSS_SOURCES=find scss -name \*.scss

css: resources/public/css/motw.css

resources/public/css/motw.css: $(shell $(FIND_SCSS_SOURCES)) | css_dir
	sassc -t compressed scss/core.scss resources/public/css/motw.css

css_dir:
	@mkdir -p resources/public/css

watch_scss:
	fswatch --recursive  scss/ | xargs -n1 -I{} sassc scss/core.scss resources/public/css/motw.css
