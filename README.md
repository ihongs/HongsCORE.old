HongsCORE
=========

Hong's Common Object Request Engine, a framework for B/S IMS Development.

What is the difference with another frameworks:

It's lightweight, simple and fast. All object(resource) can get by Core, it will build instance automatic. It provides a data storage solution that can automatically handle BLONGS TO,HAS MANY,HAS ONE relatiions. There is also have a simple JS tools to build web application easily.

How to start:

First, download this project to your disk, unzip it and create project in NetBeans or Eclipse (Web application with existing sources).

Then, open doc/ERM/hcum.mwb by MysqlWorkbench, use Database->Synchronize Model to create tables. Change your database,username,password in web/META-INF/context.xml and web/WEB-INF/conf/db-default.xml.

Now you can build this project, open browser and goto http://localhost:8080/HongsCORE/hcum/.
