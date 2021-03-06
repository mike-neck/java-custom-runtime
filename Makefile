.PHONY: build runtime bootstrap runtime zip debug

clean:
	./gradlew clean

jar:
	./gradlew modJar

runtime:
	./create-runtime.sh

bootstrap:
	cp script/bootstrap build/mod/bootstrap

debug:
	cp ./build/classes/java/main/Exec.class ./build/mod/Exec.class
	cp ./build/classes/java/main/LambdaApp.class ./build/mod/LambdaApp.class

zip:
	cd ./build/mod/ && \
	zip lambda.zip bootstrap java-custom-runtime.jar && \
	zip lambda.zip LambdaApp.class && \
	zip lambda.zip Exec.class && \
	zip -r lambda.zip lambda-custom-java-runtime

build: clean jar runtime bootstrap debug zip
