
JC = javac
JVM = java
JFLAGS = -g

MAIN = Main

SRCS = $(wildcard *.java)
CLASSES = $(SRCS:.java=.class)

#compile everything
all: $(CLASSES)

%.class: %.java
	$(JC) $(JFLAGS) $<

#Run parser
run: all
	$(JVM) $(MAIN)

clean:
	rm -f *.class