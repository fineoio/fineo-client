JReport is an embeddable BI solution that empowers users to create reports, dashboards, and data 
analytics. Users can quickly and easily implement their own reporting to develop agile, 
on-the-fly visualizations that can be embedded into applications and drive fast decision making.

After completing the following simple steps, you will be able to analyze your Fineo data in JReport.

1. Install the Fineo JDBC driver in JReport
2. Create a new JReport catalog to manage the Fineo connection
3. Use the JReport designer to access the data and create a report

# Step 1: Install the Fineo JDBC driver in JReport

Fineo provides standard JDBC connectivity that easily integrates into JReport. This guide has been
tested with JReport version 13.5.

For general instructions on using the Fineo JDBC driver, see the [JDBC guide].

1. Download the latest Fineo JDBC jar from the general [JDBC guide].

2. Copy the JDBC Driver jar into the lib folder

```%REPORTHOME%\lib\```

For example, on Windows copying the Fineo JDBC driver jar file in C:\JReport\Designer\lib folder:

```C:\JReport\Designer\lib\fineo-jdbc-|version|-all.jar

3. Add the file path into the JReport ADDCLASSPATH variable. For Windows, you will need to modify 
the `%REPORTHOME%\bin\setenv.bat`
file (for Mac OSX/Linux it will be `%REPORTHOME%\bin\setenv.sh`). 


# Step 2: Create a new JReport catalog to manage the Fineo connection

1. Click Create New -> Catalog…

2. Provide a catalog file name and click “…” to choose the file saving location.

3. Click View -> Catalog Browser

4. Right click on “Data Source 1” and select “Add JDBC Connection”

5. Fill in the “Driver”, “URL”, “User” and “Password” with:

* Driver: io.fineo.read.Driver
* Url: jdbc:fineo:api_key=<your api key>
* User: <your user name>
* Password: <your password>

You can also utilize the other standard JDBC authentication methods as described in the [JDBC guide]

6. Click Options>> select the “Qualifier” tab.
   
7. In the Quote Qualifier section, choose “User Defined” and change the “Quote Character” from “ to ` (back tick)


8. Click “OK”, JReport will verify the connection and save all information.

9. Add Tables and Views to the JReports catalog by right-clicking on the connection node and 
choose “Add Table”. From there, you can browse the schemas and add tables you wish to 
make available for query building. 

10. Click “Done” once you add all desired tables.

# Step 3: Use the JReport designer to access the data and create a report

1. In Catalog Browser right-click on “Queries” and select “Add Query…”

2. Define a JReport Query using the Query Editor. You may also import your own SQL statements.

3. Click “OK” to close the Query Editor and click the “Save Catalog” button to save your progress to the catalog file.

4. Use JReport Designer to query the data and create a report.

[JDBC guide]: /jdbc/intro
