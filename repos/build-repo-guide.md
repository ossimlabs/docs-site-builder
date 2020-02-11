# build repo Docs## Readme.md

# Blueground Build Repository
This file serves as a build file for all Blueground libraries and applications.

## Clone these repositories
To clone these repositories, create a directory and run the following:

1. `git clone git@bitbucket.org:radiantsolutions/build.git`
2. `cd build`
3. `./clone.sh`

That's it. All necessary repos will be cloned in the right place.

## Eclipse Setup
To set up Eclipse, do the following:

1. Create a new workspace
2. Show the 'Git Repositories' view
3. Add all of the Blueground repositories
4. Add the build, common, managers, webapps, and webservices projects by right-clicking each repo and importing the projects. It is recommended to use Eclipse 'working sets' to keep them organized. 
  
## Project Structure
Your 'projects' directory should look similar to this:

**Projects**  
| .git  
| common
| dependency  
| documents  
| libraries  
| managers  
| plugins
| rb-analytics 
| repo  
| tools  
| webapps
| webservices
| .gitignore  
| build.gradle  
| clone.sh  
| README.md  
| settings.gradle  
  
## Building applications
WARs will be located in PROJECT/build/libs

To build a single application from the root directory:

* `gradle clean :asm:build`
* `gradle clean :MANAGERNAME:build`

To build all applications:

* `gradle clean build`

You can also step into a directory to build an application. From webapps/asm (to build ASM) or managers/ (to build all managers):

* `gradle clean build`  

  
## Adding Dependencies
**Note**: Please be sure to talk with the team before introducing a new dependency. We have to fill out security paperwork for any new dependency.  

To introduce a new dependency:  

* Open up dependency/pom.xml  
* Add your dependency  
* From the dependency directory, run `mvn dependency:resolve`  
** This will download the dependency to repo  
* Commit and push the new dependency  
* Add the dependency to your project's build.gradle file