.PHONY: build runtime bootstrap runtime zip

clean:
	./gradlew clean

jar:
	./gradlew modJar

runtime:
	./create-runtime.sh

bootstrap:
	cp script/bootstrap build/mod/bootstrap

zip:
	cd ./build/mod/ && \
	zip lambda.zip bootstrap java-custom-runtime.jar && \
	zip -r lambda.zip lambda-custom-java-runtime

build: clean jar runtime bootstrap zip
