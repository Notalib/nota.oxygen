# Project homepage
http://nlbdev.github.io/nordic-epub3-dtbook-migrator/

# Repository
https://github.com/nlbdev/nordic-epub3-dtbook-migrator

# New builds
https://dl.dropboxusercontent.com/u/6370535/nordic-epub3-dtbook-migrator/index.html

# Getting the code
- clone https://github.com/nlbdev/nordic-epub3-dtbook-migrator.git
- change to dtbook110-ybk branch (if we want to push some changes to https://github.com/nlbdev/nordic-epub3-dtbook-migrator, then do it from branch dtbook110).
- pull origin master

# Importing
- should be imported as a Maven project in Eclipse
- File->Import->Maven->Existing Maven Projects
- press Next
- browse and choose own copy of nordic-epub3-dtbook-migrator project
- project pom.xml file should be shown
- press Finish

# Changes
- add own changes if any
- save own changes to file https://github.com/Notalib/nota.oxygen/tree/gh-pages/dp2/nota_changes.txt to keep track with our own changes

# Building
- right click project->Run As->Maven install
- if building is successful a jar file will be found beneath target folder

# Debugging
- add jar file to Daisy Pipeline 2 modules folder ex. C:\Program Files (x86)\DAISY Pipeline 2\daisy-pipeline\modules
- run/restart Daisy Pipeline 2

# Deploying
- upload jar to https://github.com/Notalib/nota.oxygen/tree/gh-pages/dp2
- edit Dockerfile (daisy-pipeline.test.dbb.dk|daisy-pipeline.live.dbb.dk) to reference new jar file
- build with build.sh script
- run with run.sh script
