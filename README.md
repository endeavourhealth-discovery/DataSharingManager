
# Data Sharing Manager

Hold all of the organisations, services, regions, data sharing agreements, data processing agreements, data flow 
agreements, cohorts and data sets.  

Includes an importer to allow you to import organisations from the trud spreadsheets.

## API Information  
![Version](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/DataSharingManager/version.svg)
![Build Status](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/DataSharingManager/build.svg)
![Unit Tests](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/DataSharingManager/unit-test.svg)

The project is setup as follows.

#### endpoints  
Contains all the endpoints for the API where most of the code is held

#### framework
Contains standard exception classes and swagger bootstrap code.  This is also where the Metrics are initialised on 
loading

#### Metrics 
Contains the standard implementations for the metrics code

#### utility
Contains common utilities used throughout the project

## Model
![Version](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/Models/version.svg)
![Build Status](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/Models/build.svg)
![Unit Tests](https://s3.eu-west-2.amazonaws.com/endeavour-codebuild/badges/Models/unit-test.svg)

The models for the data sharing manager is setup as a separate maven project that can be imported into other applications.  
This gives you the ability to access the models and also directly access the database functions without using the API.  

The structure of the models is as follows

#### database  
Contains all the Java DB entities for the tables in the models folder and PersistenceManager which handles the 
connections to the DB. 

#### enums
Enums for map Types and organisation types
 
#### json
JSON representations of the DB entities to allow communication between frontend and backend


#### Running API
create a run configuration in intelliJ using Tomcat -> Local.  
In deployment tab, click + choose Artifact and select API:war exploded
In the startup tab, click on debug and add these options into the environment variables. 

CONFIG_JDBC_USERNAME=postgres
CONFIG_JDBC_PASSWORD=YOURPOSTGRESPASSWORD
CONFIG_JDBC_URL=jdbc:postgresql://localhost:5432/config

Click run and it should be up and running.

## Frontend Information
This is using Angular CLI so use this for creating any new components.  https://cli.angular.io/ for more information.

service.ts files are used to communicate with the API.

#### Running Frontend.  
Create a run configuration in intelliJ using npm
in the script input type start
Click run and it should start running on [http://localhost:4200](http://localhost:4200/) 

## Building and commiting project

There are 2 Jenkins jobs associated with this project.  One builds the model and deploys it to the artifactory and the 
other builds the actual war files for the application to run.

The models maven artifact is just imported into the pom of the main project.

### Updating Model
If you need to update anything in the model directory, make your changes and use the `release:update-versions` maven 
plugin to increase the version number.

As both Jenkins builds are triggered off one commit, it is recommended that you don't increase the version number in the 
POM in the main data sharing manager application until the Model has been built by Jenkins.  Then you can update the 
version in the POM and the main project will build.

If you find yourself in a situation where you can't make the changes separately and the build fails,
simply manually run the Jenkins build job for Common-Data Sharing Manager and then for the main Data Sharing Manager
application.  

