# nota.oxygen add-ins

## Description

nota.oxygen is an collection of add-ins for Oxygen Xml Editor providing support for DAISY/NISO Z39.86 dtbook xml files - supported versions are v1.1.0 and 2005-3 and improved support for EPub 3

Oxygen version 16.0 is supported, may also work with earlier versions.

## License

nota.oxygen is copyright [Nota](http://nota.nu/) and distritributed under [LGPL version 3](/Notalib/dtbookOxygen/blob/master/lgpl-license.txt).

## Setup

The nota.oxygen add-ins is an eclipse project. In order to develop the add-ins, you need eclipse. Eclipse Luna package "Eclipse IDE for Java EE Developers" is recommended.

An installation of oXygen Xml Editor v 16 is also needed. In the following I will assume that eclipse is installed at c:\eclipse and that oxygen is installed at c:\oxygen.

After the project has been cloned from github, you need to import it into eclipse (File -> Import -> General -> Existing Projects into Workspace).

A bit of additional setup is needed:

- In Java Build Path, you need to make sure that the variable OXYGEN_HOME points to the local installation of oxygen (c:\oxygen).
- In Java Build Path, make sure that the project uses the same jre as oxygen (can be found in the subfolder jre of the oxygen installation folder)
- Make sure the project has the following two builders: Java Builder and Ant Builder (the Ant Builder must build the build.ant file)

The project should now be able to build.

In order to debug, you need to create a debug configuration of type "Java Application" (Run->Debug Configurations):

- Name: oxygen
- Main: Project nota.oxygen, Main class ro.sync.exml.Oxygen
- Arguments: VM Arguments: `-Dcom.oxygenxml.app.descriptor=ro.sync.exml.EditorFrameDescriptor -Xmx1024m -XX:MaxPermSize=384m`

When you debug using this configuraion, oxygen should open. 

In order for oxygen to recognize the three frameworks in this project, you need to add the note.oxygen/addins/frameworks directory as an additional framework directory in oxygen (Options->Preferences->Docuemnt Type Associations->Locations).
