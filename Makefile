.PHONY:	help ? test

ARCHIVA_USERNAME = $(shell grep access_key ~/.s3cfg | head -n1 | awk -F ' = ' '{print $$2 }')
ARCHIVA_PASSPHRASE = $(shell grep secret_key ~/.s3cfg | head -n1 | awk -F ' = ' '{print $$2}')

LEIN = HTTP_CLIENT="curl --insecure -f -L -o" lein

LEIN_ENV=ARCHIVA_USERNAME="${ARCHIVA_USERNAME}" ARCHIVA_PASSPHRASE="${ARCHIVA_PASSPHRASE}"

all: deps all-but-deps

all-but-deps: bin/lein-classpath download-lein-libs config-edn log4j2.xml init-db compile compile-js

compile:
	$(LEIN_ENV) $(LEIN) javac

clean:
	$(LEIN_ENV) $(LEIN) clean

download-lein-libs:
	$(LEIN_ENV) $(LEIN) deps

lein-deps: download-lein-libs

bin/lein-classpath: project.clj
	./bin/gen-lein-classpath $@.tmp && mv -f $@.tmp $@

ifdef AGRAJAG_TEST
CONFIG_EDN_FILE=resources/config.test.edn
else
CONFIG_EDN_FILE=resources/config.dev.edn
endif

log4j2.xml:
	cp resources/log4j2.dev.xml resources/log4j2.xml

config-edn:
	cp $(CONFIG_EDN_FILE) resources/config.edn

deps:
	./bin/deps install all

deps-check:
	./bin/deps check all
