all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf ./**/*.class

compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -cp ".:reed-solomon-erasure-coding.jar" ./**/*.java
