# Hugbúnaðarverkefni 1: Very Wow Chat

A chat room application.

Members:

* Róman (ror9@hi.is)
* Vilhelm (vilhelml@hi.is)
* Davíð (dah38@hi.is)

## Architecture
This server exposes a REST interface.  The request and responses are JSON objects.
Redis is used to store temporary data.  Neo4j is used to store chat rooms, users and tags.  MongoDB is used to store chat messages.
Chat messages and emails are encrypted.

## Introduction
The project is split into three parts 

1. The server part (this repository)
2. The client part (another repository),
   can be found at: <https://github.com/RomanDatabasePimp/Hugbunadverkefni1-Webapp>
3. The email server, can be found at: <https://github.com/vilhelml/cluster-3f-mail-service>. <br/>NOTE: You do not need to deploy this on your local machine, this server is running on: <https://hugbomailserver.herokuapp.com/>

## Dependencies for this project
This README will cover all the steps on how to configure all the dependencies, except for Maven.<br>

These are the dependencies that are required to deploy the server:

1. Maven: <https://maven.apache.org/>
2. Neo4j Desktop version 1.1.10 or higher: <https://neo4j.com/>
3. MongoDB Version 4.0.3 or higher: <https://www.mongodb.com/>
4. Redis Version 5.0.0 or higher: <https://redis.io/>
  
## Guide on how to set up the services

I recomend you follow these steps before cloning the repository, there is a lot of stuff that needs to be done before strarting the project.

For macOS, do these things before

1. Go to App Store and install Xcode.
2. After installing Xcode install Homebrew: <https://brew.sh/>

### Neo4j

1. Open <https://neo4j.com/>.
2. On the top right corner there is a download button.
3. Choose the free Desktop version (the default pop up should be it, you just click download).
4. Fill the form and click download
5. Once the download is complete just run the setup with all the default settings.
6. Run `Neo4j Desktop` as administrator.
7. In Neo4j Desktop create a project with name `Very_Wow_Chat` (this name doesn't really matter).
8. In that project create a graph with the name `Very_Wow_Chat`  (this name matters)!
9. You may choose any password you wish, but remember to copy that password  to `application.properties` 
10. Once the graph is created verify that it was successfully created. You can do this by clicking "Manage" in the graph and in there click the play button it will say status Running.

NOTE: for macOS, you should download neo4j Desktop aswell.

### Redis

#### Windows

1. Open <https://redis.io/>.
2. Click on the link that says " Check the downloads page "
3. Download the latest stable version of redis (its a rar file)
4. I recomend you extract your .rar file on your root my pc's root is c:\
5. open the command promp in the file you extracted
6. type : redis-server.exe
7. you should see the server boot and running on PORT 6379 keep it this way
( PLEASE NOTE : there are more settings that need to be added for long term usage <br>
  the default setting will do for testing, you might be greeted with that redis dosent want to save <br>
  files or something like that, in that case just turn off redis and turn it on again)
  
#### macOS

1. Run `brew install redis` in Terminal to install Redis.
2. Run `redis-server` in Terminal to start Redis server.

### MongoDB

#### Windows

1. Open <https://www.mongodb.com/>.
2. In the top right corner click on "Get MongoDB".
3. In the Tabs Select the "Server" tab .
4. Select the "Current version" and choose download MSI.
5. Run the MSI -> Click Next until you reach what kind of mongodb install you want.
6. Select Complete install
7. In services configuration click on the radio button "Run Service as local or domain user"
8. Keep the domain as "." (means local) , The account name and password is the your local machines account and password <br>
   NOTE: pin dosent work you have to create a password if you are using pin to loggin into your machine
9. Finsh the install.
10. If you installed MongoDB with all the default paths you can navigate to `C:\Program Files\MongoDB\Server\4.1\bin` 
11. Run `mongod` as administrator, and it will open and close right away. If you open your task manager you will see it running.

#### macOS

1. Run `brew install mongodb` in Terminal to install MongoDB.
2. Run `mongod` to start server. 

## Cloning the repo and running the server

1. Clone this repository onto your computer.
2. Please note this is a Maven project.
3. Open the project in your preferred IDE
4. Navigate to `src/main/resources` and find the `application.properties.example`, remove the `.example` extension from the file and fill in the following constants (recommend you just use these)
   - `server.port=9090`  (keep at 9090 unless you change the .env file in the web app)
   - `spring.data.neo4j.uri=bolt://localhost` (keep it like this)
   - `spring.data.neo4j.username=neo4j `      (default user is neo4j unless you change it)
   - `spring.data.neo4j.password=1234`          (this is the password you created in the neo4j phase)
   - `spring.data.mongodb.uri=mongodb://localhost`  (keep it like this)
   - `spring.data.mongodb.database=very_wow_chat` (it dosent matter if you haven't created it in mongodb it will be create automaticly)
   - `cryptography.security.password=12345`            (up to you)
   - `cryptography.storage.password=123456` (messages and email will be encrypted with this key)
   - `cryptography.storage.salt=baf1` (salt for the password)
   - ` email.server.serverRunningOn=http://localhost:3000/` (tells you on what is the frontend running on)
   - `email.server.url=https://hugbomailserver.herokuapp.com/`  (don't change unless u deploy the email server localy also)
   - `email.server.secretkey=VeryStrongPassword      `          (don't change)
   - `logging.level.org.neo4j.ogm.drivers.bolt.request.BoltRequest=WARN`  (keep it like this unless you need to debug Neo4j)
7. Run `Application.java` and you will be fine !!!

## Creating Test data

1. If you have [Postman](https://www.getpostman.com/) or [curl](https://curl.haxx.se/) you can create data in for the database.
2. Send a HTTP GET REQUEST on `http://localhost:9090/createdata` and you will create 3 users,
   - Username: `ror9`,  password: `Test$1234`
   - Username: `vilhelml`,  password: `Test$1234`
   - Username: `dah38`,  password: `Test$1234`
3. Open `https://hugbomailserver.herokuapp.com/` just to wake up the service since this is a free version of heroku (the services goes to sleep after 30 min. of inactivity).

### You thought you were done ??? O nononono
As we said in the beginning this is only the backend, you still have to deploy the frontend which can be found here: `https://github.com/RomanDatabasePimp/Hugbunadverkefni1-Webapp`
