all: clean

clean:
	-rm -rf *.jpg *.dot a.out*
	-find test -type f -regex ".*\.c" | xargs rm
	-find test -type f -regex ".*\.class" | xargs rm
