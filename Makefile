all: clean

clean:
	-rm *.jpg *.dot
	-find test -type f -regex ".*\.c" | xargs rm
	-find test -type f -regex ".*\.class" | xargs rm
