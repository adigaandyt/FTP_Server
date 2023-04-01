[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-c66648af7eb3fe8bc4f294546bfd86ef473780cde1dea487d3c4ff354943c9ae.svg)](https://classroom.github.com/online_ide?assignment_repo_id=9604712&assignment_repo_type=AssignmentRepo)


# Assignment2 File Server

### Hours worked
Andy - 40 hours

### List of students in the group
Andy Thaok, adigaandyt, 311555221


### Compilation instructions
The project was compiled using Intellij

File -> Project Structure -> Artifacts -> + -> Type: Platform specific package -> From module client - > Pick main class -> OK

Build -> Build Artifacts... -> Client/Server -> Build

### Running instructions
1. Make sure the config.txt is in the jar directory 
2. Make sure there's a x64 Compressed Archive of JDK 19 in the jar directory (or know where it is)
3. Both programs are ran using a bat file in the jar directory
4. Bat file contains : " [jdk-19 folder\bin\java.exe]  -jar [program name].jar"
5. Run the bat file

Download link for the jdk-19 folder https://download.oracle.com/java/19/latest/jdk-19_windows-x64_bin.zip

### Client Configuration File
Line 1) [Client name]

Rest of lines) [Server name] [Server IP] [Server Port]

(Space between IP and PORT and not a colon)

The client download directroy is the jar working directory


### Server Configuration File (config.txt):
Line 1) [File storage directory]

Line 2) [Server name] [Server IP] [Server Port]

(Space between IP and PORT and not a colon)

Server log file is in the jar working directory

### Server Configuration File(serverlist.txt):
Each line has the name, ip and port of all the other servers
[Server name] [Server IP] [Server Port]
for example the serverlist for serverOne with 4 other serrvers is
ServerTwo localhost 6000
ServerThree localhost 7000
ServerFour localhost 8000

### Syncing
on startup the server requests a file list from every server, comparing the files each time
for example if server3 is syncing and recieved file1 info from server 1
server3 will hold the info in a ConcurrentHashMap, if Server3 gets file1 from server 2 aswell it will 
compare the files and decide wether or not to keep or replace the file and at the end once server3 has
all the info, it asks the correct server for each file

### Locking and unlocking
Locking and unlocked works based off of client's names, so only clients named Alice can unlock a file locked by Alice
Lock and Unlock can also be sent as an offer OFFERLOCK and OFFERSYNC.
OFFERLOCK and OFFERSYNC just mean that the LOCK request is coming from another server on behalf of client so the 
requesting server sends the file name and the client name 

### Thread Safety
Thread safety is insured mainly by giving each thread it’s local fields and not using static ones so one thread doesn’t effect another and if static objects are used they are not changed in the thread or while a thread is running, and files are handled using a ConcurrentHashMap
