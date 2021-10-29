# EPUB plugin and frameworks

A plugin and frameworks to support editing of EPUBs in oXygen.

All components are tested with oXygen 20.

## Plugin

In oXygen terms, a plugin is an extension that adds new functionality implemented in Java. The EPUB plugin included here provides operations that manipulate EPUB archives and the files within them.

## Frameworks

Frameworks are extensions that use pre-existing customization options within oXygen, with no coding required. A framework may be used to style and validate documents. Each framework includes rules that determine when the framework is activated. Supported formats are:

- NCX
- OPF
- XHTML

Although the frameworks themselves are no-code, they are allowed to depend on Java classes in a plugin. The frameworks above use custom operations provided by the EPUB plugin, which is therefore required to be present.

## Instructions

Build the plugin from source using the JRE and libraries included in your installation of oXygen (jre/ and lib/, respectively). Frameworks require no compilation. Refer to oXygen documentation for instructions regarding installation of extensions.