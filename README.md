# logreader-test

Is an application that reads log file entries in the following format:
```
{"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495212}
{"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}
{"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}
{"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495217
}
{"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}
{"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}
...
```

Each log file entry is a START and FINISH event for a particular process.
The application processes each of the entries, calculates if the process took a long time and stores the details to a HSQL Database.

The user is allowed specify the number of lines in the log file that should be processed at once. This allows large log files to be processed without running out of memory.

The user is also allowed specify the number of seperate threads they want to create to process the log file entries.

To complie the project, run:

```
gradle jar
```
When the project has been compiled navigate to the folder containing the jar file and run (for example, where c:/testlog.txt is the path to the log file, where 1000 is the number of log lines to process at once and 4 is the max number of threads to process the entries):

```
java -jar test-1.0-SNAPSHOT.jar c:/testlog.txt 1000 4
```
