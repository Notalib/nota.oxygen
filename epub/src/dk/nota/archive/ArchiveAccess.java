package dk.nota.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Random;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * <p>ArchiveAccess is used to manipulate zipped archives.</p>
 * <p>Note that, even though the class is in some sense a wrapper around NIO
 * filesystem operations, many methods will nevertheless take a
 * {@link java.nio.file.FileSystem} (preferably the one corresponding to the
 * current instance, as obtained by {@link #getArchiveAsFileSystem()}) as a
 * parameter. This is a somewhat clumsy way of allowing the archive to be
 * accessed as a resource only once for various methods, rather than have it be
 * opened and closed within each method. Basically, the responsibility of
 * opening and closing the archive is offloaded to the caller.</p>
 */

public class ArchiveAccess {
	
	private Path archivePath;
	
	/**
	 * Constructor.
	 * @param archiveUri The archive location.
	 */
	
	public ArchiveAccess(URI archiveUri) {
		archivePath = Paths.get(archiveUri);
	}
	
	/**
	 * <p>Create a backup archive, preserving the filename with an additional .bak
	 * extension.</p>
	 * <p><em>Note</em>: Previous backups will be overwritten.</p>
	 * @throws IOException
	 */
	
	public File backupArchive() throws IOException {
		Path backupArchivePath = archivePath.resolveSibling(archivePath
				.getFileName() + ".bak");
		return Files.copy(archivePath, backupArchivePath,
				StandardCopyOption.REPLACE_EXISTING).toFile();
	}
	
	/**
	 * Copy a file to the specified folder within the archive.
	 * @param archiveFileSystem The archive as an <em>open</em> filesystem.
	 * @param internalFolder The path to the folder where this file is to be
	 * copied to.
	 * @param replace Whether or not to replace the file if it exists. If the
	 * file is not to be replaced, the new file will be suffixed with a random
	 * number.
	 * @param file The file to be copied.
	 * @return The URI of the created file, possibly with a random suffix.
	 * @throws IOException
	 */
	
	public URI copyFileToArchiveFolder(FileSystem archiveFileSystem,
			String internalFolder, boolean replace, File file)
			throws IOException {
		Path basePath = archiveFileSystem.getPath(internalFolder);
		Files.createDirectories(basePath);
		String fileName = file.getName();
		Path filePath = basePath.resolve(fileName);
		if (!replace)
			// If files should not be replaced, add a random suffix
			// prior to creating the file
			while (Files.exists(filePath)) {
				// TODO: Find a prettier solution - just decide on a
				// suffix format indicating a file copy and increment
				// as needed
				int suffix = new Random().nextInt(Integer.MAX_VALUE);
				fileName = fileName.replaceFirst("^(.+)(\\..*?)$",
								"$1-" + suffix + "$2");
				filePath = basePath.resolve(fileName);
			}
		return makeArchiveBasedUri(Files.copy(file.toPath(), filePath,
				StandardCopyOption.REPLACE_EXISTING).toString());
	}
	
	/**
	 * Equivalent to 
	 * {@link #copyFileToArchiveFolder(FileSystem, String, boolean, File)} 
	 * with a filesystem provided by {@link #getArchiveAsFileSystem()}.
	 * <p><em>Note</em>: The method will attempt to open the archive as a 
	 * filesystem and may fail if the archive is already open.</p>
	 * @param internalFolder The path to the folder where this file is to be
	 * copied to.
	 * @param replace Whether or not to replace the file if it exists. If
	 * false, the file will be suffixed with a random number.
	 * @param file The file to be copied.
	 * @return The URI of the created file, possibly with a random suffix.
	 * @throws IOException
	 */
	
	public URI copyFileToArchiveFolder(String internalFolder, boolean replace,
			File file) throws IOException {
		try (FileSystem archiveFileSystem = getArchiveAsFileSystem()) {
			return copyFileToArchiveFolder(archiveFileSystem,
					internalFolder, replace, file);
		}
	}
	
	/**
	 * Copies several files to the same folder within the archive by calling
	 * {@link #copyFileToArchiveFolder(String, boolean, File)} for each.
	 * @param internalFolder The path to the folder where this file is to be
	 * copied to.
	 * @param replace Whether or not to replace files if they exist. If false,
	 * files will be suffixed with random numbers.
	 * @param files The files to be copied.
	 * @return A list of URIs corresponding to the created files, some of which
	 * may have suffixes.
	 * @throws IOException
	 */
	
	public LinkedList<URI> copyFilesToArchiveFolder(String internalFolder,
			boolean replace, File... files) throws IOException {
		LinkedList<URI> uris = new LinkedList<URI>();
		try (FileSystem archiveFileSystem = getArchiveAsFileSystem()) {
			for (File file : files)
				uris.add(copyFileToArchiveFolder(archiveFileSystem,
						internalFolder, replace, file));
		}
		return uris;
	}
	
	/**
	 * Get a {@link java.nio.file.FileSystem} instance of the archive.
	 * @return The archive as a filesystem.
	 * @throws IOException
	 */
	
	public FileSystem getArchiveAsFileSystem() throws IOException {
		return FileSystems.newFileSystem(archivePath, null);
	}
	
	/**
	 * Get the archive as a file.
	 * @return The archive file.
	 */
	
	public File getArchiveFile() {
		return archivePath.toFile();
	}
	
	/**
	 * Get the archive as a path (to the archive file, not to the root folder
	 * or any other path within it).
	 * @return The archive path.
	 */
	
	public Path getArchivePath() {
		return archivePath;
	}
	
	/**
	 * Get the root folder of the archive (zip:file://path/to/file!/) as a URI.
	 * @return A URI corresponding to the root of the archive.
	 */
	
	public URI getArchiveInternalUri() {
		return URI.create("zip:" + archivePath.toUri() + "!/");
	}
	
	/**
	 * Get the contents of the specified internal archive folder.
	 * <p><em>Note</em>: The method will attempt to open the archive as a 
	 * filesystem and may fail if the archive is already open.</p>
	 * @param directoryPath The path to a folder within the archive.
	 * @return The contents of the folder.
	 * @throws IOException
	 */
	
	public LinkedList<Path> getDirectoryContents(String directoryPath)
			throws IOException {
		try (FileSystem archiveFileSystem = getArchiveAsFileSystem()) {
			return getDirectoryContents(archiveFileSystem, directoryPath);
		}
	}
	
	/**
	 * Get the contents of the specified internal archive folder.
	 * @param archiveFileSystem The archive as an <em>open</em> filesystem.
	 * @param directoryPath The path to a folder within the archive.
	 * @return The contents of the folder.
	 * @throws IOException
	 */
	
	public LinkedList<Path> getDirectoryContents(FileSystem archiveFileSystem,
			String directoryPath) throws IOException {
		LinkedList<Path> paths = new LinkedList<Path>();
		Files.walk(archiveFileSystem.getPath(directoryPath), 1)
			.sorted()
			.forEach(path -> paths.add(path));
		return paths;
	}
	
	/**
	 * Resolve a path against the URI of the archive root.
	 * @param relativePath A path relative to the root of the archive, with no
	 * leading slash.
	 * @return An absolute URI corresponding to the relative path within the
	 * archive.
	 */
	
	public URI makeArchiveBasedUri(String relativePath) {
		return URI.create(getArchiveInternalUri() + relativePath);
	}
	
	/**
	 * <p>Remove the archive component of an absolute archive-based URI.</p>
	 * <p><em>Note</em>: This is a not a generic, robust method for
	 * relativizing arbitrary archive URIs against each other.</p>
	 * @param uri An absolute URI.
	 * @return The archive-internal component of the URI (following '!/').
	 */
	
	public String relativizeUriToArchive(URI uri) {
		if (!uri.isAbsolute()) throw new IllegalArgumentException(
				String.format("The provided URI (%s) is not absolute", uri));
		String[] components = uri.toString().split("!/");
		if (components.length < 2) throw new IllegalArgumentException(
				String.format("The provided URI (%s) is not an archive URI",
						uri));
		return components[1];
	}
	
	/**
	 * Serialize an XML node to a file within the archive.
	 * @param node The XML content to serialize.
	 * @param uri The absolute URI of the file to serialize to.
	 * @param serializer An XML serializer.
	 * @param archiveFileSystem The archive as an <em>open</em> filesystem.
	 * @throws IOException
	 * @throws SaxonApiException
	 */
	
	public void serializeNodeToArchive(XdmNode node, URI uri,
			Serializer serializer, FileSystem archiveFileSystem)
			throws IOException, SaxonApiException {
		Path path = archiveFileSystem.getPath(relativizeUriToArchive(uri));
		try (OutputStream outputStream = Files.newOutputStream(path,
				StandardOpenOption.CREATE)) {
			serializer.setOutputStream(outputStream);
			serializer.serializeNode(node);
		} finally {
			serializer.close();
		}
	}
	
	/**
	 * Serialize several XML nodes to files within the archive. 
	 * @param nodes A map containing file URIs as key and XML nodes as values.
	 * @param serializer An XML serializer.
	 * @param archiveFileSystem The archive as an <em>open</em> filesystem.
	 * @throws IOException
	 * @throws SaxonApiException
	 */
	
	public void serializeNodesToArchive(Map<URI,XdmNode> nodes,
			Serializer serializer, FileSystem archiveFileSystem)
			throws IOException, SaxonApiException {
		for (Entry<URI,XdmNode> entry : nodes.entrySet())
			serializeNodeToArchive(entry.getValue(), entry.getKey(),
					serializer, archiveFileSystem);
	}

}
