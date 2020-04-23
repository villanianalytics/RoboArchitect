[![codecov](https://codecov.io/gh/villanianalytics/RoboArchitect/branch/master/graph/badge.svg)](https://codecov.io/gh/https://codecov.io/gh/villanianalytics/RoboArchitect)

# RoboArchitect
 RoboArchitect is a command-line application that can help you with zipping and unzipping files, convert from
     .csv file to .xlsx file and vice versa, send an email,
     encrypt a password and write it to a file, send a request to specified url with a username and a password and get a
     response, parse data from JSON, and run SQL against a .csv/.txt file or create SQLite database and run SQL request
     there.
## Description
This is application can:
1) <i>zip</i> and <i>unzip</i> files and directories.
2) convert <i>.csv</i> files to <i>.xlsx</i> and vice versa.
3) encrypt your password and save it to a specified file.
4) connect to any web service and get a full or partial response.
5) parse <i>JSON</i> files and get specified values.
6) send customizable emails.
7) executes SQL queries using delimited files as the basis.
8) create an SQLite database, add tables there with data from delimited files or adds
       data to an existing table. It can also execute SQL queries to the database and write the result to a file or display
       it on the console.
## Functions
### Zip
To make <i>.zip</i> file you need to pass to RoboArchitect <i>-zip</i> command with <i>/srcDir</i>
 and <i>/destFile</i>.<br>
For example:
<pre>RoboArchitect -zip /srcDir="file.txt" /destFile="file.zip"</pre>
### Unzip
To get sources of a <i>.zip</i> file you need to pass to RoboArchitect <i>-unzip</i>
 command with <i>/srcFile</i> and <i>/destDir</i>.<br>
For example:
<pre>RoboArchitect -unzip /srcFile="file.zip" /destDir="temp"</pre>
### Convert
To make conversion of <i>.csv</i> file to <i>.xlsx</i> file you need to pass to RoboArchitect
 <i>-convert</i> command with <i>/src</i>, <i>/destFile</i>, <i>/delim</i> and <i>/sheetname</i>.<br>
For example:
<pre>RoboArchitect -convert /src="file.csv" /destFile="file.xlsx" /delim=";" /sheetname="SheetName1" </pre>

To make conversion of <i>.xlsx</i> file to <i>.csv</i> file you need to pass to RoboArchitect
 <i>-convert</i> command with <i>/src</i>, <i>/destFile</i> and <i>/sheetname</i>.<br>
For example:
<pre>RoboArchitect -convert /src="file.xlsx" /destFile="file.csv" /sheetname="SheetName1" </pre>
### Password
To encrypt your password you need to pass to RoboArchitect <i>-password</i> command with <i>/passwordFile</i>.<br>
For example:
<pre>RoboArchitect -password /passwordFile="password.txt"</pre>
### Connect
To connect to any web service you need:
 1) To create <i><a href="#connect">config.properties</a></i>.
 2) To pass to RoboArchitect <i>-connect</i> command with <i>/config</i>, other parameters, such as 
  <i>/destFile</i>, <i>/jsonPath</i>, <i>/passwordFile</i>, <i>/reqType</i> and <i>/inputJSON</i> are optional.<br>
Here is an example of <i>config.properties</i> (# -> marked optional fields):
<pre id="connect">
     url=url
     user=username
     #password=password
     #httpmethod=GET
</pre>
Here is some examples of connect function:
<pre>RoboArchitect -connect /destFile="response.json" /config="config.properties" /passwordFile="password.txt"</pre>
<pre>RoboArchitect -connect /config="config.properties"</pre>
<pre>RoboArchitect -connect /destFile="response.json" /config="config.properties" /jsonPath="$.items[?(@.name==\"NAME\"||@.name==\"NAME\")].id"</pre>
### JSON Path
To get specified <i>JSON</i> part you need to pass to RoboArchitect
 <i>-jsonpath</i> command with <i>/srcFile</i> and <i>/jsonPath</i>, <i>/destFile</i> is optional.<br>
 For example:
 <pre>RoboArchitect -jsonpath /srcFile="response.json" /jsonPath="$.items[?(@.name==\"NAME\"||@.name==\"NAME\")].id" /destFile="filtered.json"</pre>
### Email
To send an email you need:
1) To create <i><a href="#email">email.properties</a></i>.
2) To pass to RoboArchitect <i>-email</i> command with <i>/config</i>,  other parameters, such as 
<i>/destFile</i>, <i>/subject</i>, <i>/body</i> and <i>/passwordFile</i> are optional.<br>
Here is an example of <i>email.properties</i> (# -> marked optional fields):
<pre id="connect">
     fromaddress=email@gmail.com
     #password=password
     toaddress=emailTo@gmail.com
     #ccaddress=copyEmail@xrpmail.com
     #bccaddress=sicretCopyEmail@enayu.com
     #subject=Here must be a subject of a letter.
     htmlbody=Y
     #body=&lt;h1> Here could be a text. &lt;/h1>
     #attachment=file.txt
     ctype=TLS
     smtpserver=smtp.gmail.com
     port=587
       ### SSL -> port 465; TLS -> port 587.
</pre>
Here is some examples of connect function:
<pre>RoboArchitect -email /config=email.properties /passwordFile=password.txt</pre>
<pre>RoboArchitect -email /config=email.properties</pre>
<pre>RoboArchitect -email /subject="SUBJECT" /body="Let's make next Line\nNext Line" /config=email.properties /log=test.log</pre>
## Query delim
To run SQL request on .csv or .txt file you need:
1) Choose which file you want to use as a basis <i>/srcFile</i>.
2) Write SQL request and pass it to <i>/query</i>.
3) Other attributes as <i>/delim</i>, <i>/suppressHeaders</i>, <i>/skipLines</i>, <i>/skipDataLines</i> are optional.

Run SQL on the <samp>test.csv</samp> file in the <samp>converted</samp> directory with SQL request from a
    command line.
<pre>RoboArchitect -querydelim /srcFile="converted/test.csv" /query="Select * from test where \"3\"='c' or \"1\"='qwerty'"</pre>


Run SQL for the <samp>test.csv</samp> file in the <samp>converted</samp> directory with SQL request from a
    command line with <samp>,</samp> delimiter and write all logs to <samp>app.log</samp> file.
<pre>RoboArchitect -querydelim /srcFile="converted/test.csv" /query="Select * from test where \"3\"='c' or \"1\"='qwerty'" /delim="," /destFile="query/out.txt" /log="app.log"</pre>


Run SQL for the <samp>test.csv</samp> file in the <samp>converted</samp> directory with SQL request from a
    command line with default delimiter, skip two file's lines and write all logs to <samp>app.log</samp> file.
<pre>RoboArchitect -querydelim /srcFile="converted/test.csv" /query="Select * from test" /destFile="query/out.txt" /skipDataLines=2 /log="app.log"</pre>

## As of Version 1.2.2, the SQLite function has been replaced with SQL 
The SQL command has been developed in order to allow future expansion to support multiple database types. See below for updated syntax.

## SQL
Create empty SQLite <samp>store.db</samp> database.
<pre>RoboArchitect -sql /connection="sqlite" /op=createDB /db=store.db</pre>

Create empty SQLite <samp>store.db</samp> database.
<pre>RoboArchitect -sql /connection="sqlite" /op=createDB /db=store.db</pre>

Creating a table named <samp>ABCD</samp> in the <samp>store.db</samp> database and filling it with data from the <samp>test.csv</samp> file.
<pre>RoboArchitect -sql /connection="sqlite" /op=importTable /db=store.db /mode=overwrite /srcFile=test.csv /table=ABCD</pre>

Add <samp>A</samp> table in SQLite <samp>store.db</samp> database by adding <samp>test2.csv</samp> file data.
<pre>RoboArchitect -sql /connection="sqlite" /op=importTable /db=store.db /mode=overwrite /srcFile=test2.csv /table=A</pre>

To overwrite <samp>A</samp> table in SQLite <samp>store.db</samp> database by adding <samp>test3.csv</samp> file data.
<pre>RoboArchitect -sql /connection="sqlite" /op=importTable /db=store.db /mode=overwrite /srcFile=test3.csv /table=A</pre>

To add data to <samp>A</samp> table in SQLite <samp>store.db</samp> database by adding <samp>test4.csv</samp> file data.
<pre>RoboArchitect -sql /connection="sqlite" /op=importTable /db=store.db /mode=overwrite /srcFile=test4.csv /table=A</pre>

To run SQL query on <samp>A</samp> table in SQLite <samp>store.db</samp> database and print result with headers to a console.
<pre>RoboArchitect -sql /connection="sqlite" /op=queryDB /db=store.db /query="SELECT * from A" /header=true</pre>

To run SQL query on <samp>A</samp> table in SQLite <samp>store.db</samp> database and print result without headers to a file.
<pre>RoboArchitect -sql /connection="sqlite" /op=queryDB /db=store.db /query="SELECT * from A WHERE \"NAME\"=\"TOM\"" /destFile=file.txt</pre>

 To run SQL query from a <samp>query.txt</samp> file on <samp>A</samp> table in SQLite <samp>store.db</samp> database and print result without headers to a console.
<pre>RoboArchitect -sql /connection="sqlite" /op=queryDB /db=store.db /query="query.txt"</pre>
## Maven settings
1) To make the executable files you need to add to in pom.xml file
path to OpenJDK <i>.zip</i> file. <strong>Keep attention that OpenJDK needs to be for X64 Systems.</strong>
<pre>&lt;!--Need to be changed to your linux64(mac) openJDK-->
&lt;argument>packaging/zulu8.44.0.11-ca-jdk8.0.242-linux_x64.zip&lt;/argument></pre>
## Create executable files
To create executable files you need to run the next command in Maven.
### Windows executable
<pre>mvn clean package</pre>

### Linux executable
<pre>mvn clean package -P linux</pre>
 
### Mac executable
<pre>mvn clean package -P mac</pre>

### All executables
<pre>mvn clean package -P windows,linux,mac</pre>
