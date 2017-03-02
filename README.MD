The Project is aimed at learning Java 8 Promises with Thread pools and event based programming model.


Steps:

1. Install Java 8.
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

2. Install maven.
https://maven.apache.org/download.cgi

3. To Compile and Build
mvn clean install


4. To run

java -jar target/ThreadPoolDownload-1.0-SNAPSHOT.jar

Options:

usage: utility-name

 -d,--save-dir <arg>                    Directory to Save files

 -f,--urls <arg>                        URLs in comma separated format

 -j,--no-of-threads <arg>               Parallelism

 -k,--fresh                             Force The Manager to Download all
                                        parts again, don't resume

 -x,--max-number-of-connections <arg>   Maximum Number of Connections per
                                        URL



Example:

java -jar target/ThreadPoolDownload-1.0-SNAPSHOT.jar -j 10 -x 8 -d /tmp -f https://1.as.dl.wireshark.org/osx/Wireshark%202.2.4%20Intel%2064.dmg,http://s3.amazonaws.com/epubbud_epubs/5FPD5ML6/1984.epub