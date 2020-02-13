package com.maxar.manager.dataingest;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.radiantblue.analytics.core.log.SourceLogger;

public abstract class FileIngester extends
		Ingester
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Value("#{T(java.io.File).new('${fileingester.datapath}')}")
	protected File inDirectory;

	@Value("#{T(java.io.File).new('${fileingester.archivepath}')}")
	protected File successDir;

	@Value("#{T(java.io.File).new('${fileingester.rejectpath}')}")
	protected File errorDir;

	@Value("${fileingester.filesuffix}")
	protected String fileSuffix;

	@Value("${fileingester.filepattern}")
	protected String filePattern;

	private boolean bulkFileLoading = false;

	public FileIngester() {}

	public String ingestFile(
			final File f,
			final List<File> associatedFiles ) {

		try {

			ingestToDatabase(	f,
								associatedFiles);

		}
		catch (final Exception e) {
			final String error = "Error ingesting file " + f.getAbsolutePath() + ": \n" + e;
			logger.error(error);
			for (final StackTraceElement el : e.getStackTrace()) {
				logger.error(el);
			}
			return error;
		}
		return "SUCCESS";
	}

	protected InputStream openInputStream(
			final File f )
			throws Exception {

		InputStream input;
		if (f.getName()
				.toLowerCase()
				.endsWith("gz")) {
			input = new GZIPInputStream(
					new FileInputStream(
							f));
		}
		else {
			input = new FileInputStream(
					f);
		}

		return input;
	}

	abstract public void ingestToDatabase(
			final File f,
			final List<File> associatedFiles )
			throws Exception;

	public List<File> findAssociatedFiles(
			final File f,
			final File inboxDir ) {
		return null;
	}

	public boolean isIngestible(
			final File f ) {
		// Make sure the file is one of appropriate filetype, and
		// suffix indicates is done writing
		if (!f.getName()
				.matches(filePattern)) {
			logger.debug("Doesn't match regex");
			return false;
		}
		else if (!f.getName()
				.endsWith(fileSuffix)) {
			logger.debug("Doesn't match suffix");
			return false;
		}
		else if (f.isDirectory()) {
			logger.debug("Is a directory");
			return false;
		}
		return true;
	}

	private void moveFiles(
			final File fileForIngest,
			final List<File> associatedFiles,
			final File dir ) {
		try {
			final File ingestedFile = new File(
					dir.getAbsolutePath(),
					fileForIngest.getName());
			Files.move(	fileForIngest.toPath(),
						ingestedFile.toPath(),
						REPLACE_EXISTING);
			if (associatedFiles != null) {
				for (final File fileToMove : associatedFiles) {
					if (fileToMove.exists()) {
						final File movedFile = new File(
								dir.getAbsolutePath(),
								fileToMove.getName());
						Files.move(	fileToMove.toPath(),
									movedFile.toPath(),
									REPLACE_EXISTING);
					}
				}
			}
		}
		catch (final IOException e) {
			logger.error(e);
		}
	}

	private void doIngest(
			final File fileForIngest ) {
		final List<File> associatedFiles = findAssociatedFiles(	fileForIngest,
																inDirectory);

		logger.info("Ingesting file: " + fileForIngest.getName());

		String ingestComment = ingestFile(	fileForIngest,
											associatedFiles);

		final boolean success = ingestComment.equals("SUCCESS");
		if (success) {
			ingestComment = null;
		}

		if (success) {
			moveFiles(	fileForIngest,
						associatedFiles,
						successDir);
		}
		else {
			moveFiles(	fileForIngest,
						associatedFiles,
						errorDir);
		}
	}

	@Override
	protected void doLoopIngest() {

		logger.debug("Ingesting files from: " + inDirectory.getAbsolutePath());

		if (!inDirectory.exists()) {
			logger.error("Ingest directory not found: " + inDirectory.getAbsolutePath());
			return;
		}
		setBulkFileLoading(false);

		WatchService watcher;
		WatchKey watchKey;
		final Path dataPath = inDirectory.toPath();
		try {
			watcher = FileSystems.getDefault()
					.newWatchService();
			watchKey = dataPath.register(	watcher,
											ENTRY_CREATE);
		}
		catch (final IOException e) {
			logger.error(	"Failed to initialize data ingestion...exiting/n" + e.getLocalizedMessage(),
							e);
			return;
		}

		// infinite loop waiting for events
		while (true) {
			WatchKey key;
			try {
				// Wait for key to be signaled
				key = watcher.take();

				// Is this the dir we are watching?
				if (key != watchKey) {
					continue;
				}

				// loop over events
				for (final WatchEvent<?> event : key.pollEvents()) {
					final WatchEvent.Kind<?> kind = event.kind();

					// TODO: How to handle overflow?
					if (kind == OVERFLOW) {
						continue;
					}

					@SuppressWarnings("unchecked")
					final WatchEvent<Path> ev = (WatchEvent<Path>) event;

					// parent should be directory we are watching
					final Path parent = (Path) key.watchable();
					if (parent != dataPath) {
						continue;
					}

					// child should be new target that was created
					final Path child = ev.context();

					logger.debug("parent=" + parent.toString() + "and child=" + child.toString());

					final File fileForIngest = new File(
							parent.toFile(),
							child.toString());
					if (fileForIngest.exists() && isIngestible(fileForIngest)) {
						doIngest(fileForIngest);
					}
				}

				final boolean valid = key.reset();
				if (!valid) {
					logger.error("Key not valid?");
					break;
				}
			}
			catch (final InterruptedException e) {
				logger.error(	"Watcher was interupted: " + e.getLocalizedMessage(),
								e);
			}
			catch (final Exception e) {
				logger.error(	"Caught exception ingesting from inbox: " + inDirectory.getAbsolutePath(),
								e);
				logger.error(e);
			}
		}
	}

	@Override
	protected void catchUpIngest() {
		if ((inDirectory != null) && !inDirectory.exists()) {
			logger.error("Ingest directory not found: " + inDirectory.getAbsolutePath());
			return;
		}

		logger.debug("Ingesting files from: " + inDirectory.getAbsolutePath());

		try {
			final File[] filesForIngest = inDirectory.listFiles();

			// Remove any non-ingestible files
			final List<File> ingestFiles = new ArrayList<>();
			if ((filesForIngest != null) && (filesForIngest.length > 0)) {
				logger.debug("Found " + filesForIngest.length + " potential files in: "
						+ inDirectory.getAbsolutePath());
				for (final File fileForIngest : filesForIngest) {
					if (fileForIngest.exists() && isIngestible(fileForIngest)) {
						ingestFiles.add(fileForIngest);
					}
				}
			}

			setBulkFileLoading(true);

			Collections.sort(	ingestFiles,
								new Comparator<File>() {
									@Override
									public int compare(
											final File f1,
											final File f2 ) {
										return Long.valueOf(f1.lastModified())
												.compareTo(f2.lastModified());
									}
								});

			if ((ingestFiles != null) && (ingestFiles.size() > 0)) {
				logger.debug("Found " + ingestFiles.size() + " files in: " + inDirectory.getAbsolutePath());

				for (final File fileForIngest : ingestFiles) {
					if (fileForIngest == ingestFiles.get(ingestFiles.size() - 1)) {
						setBulkFileLoading(false);
					}
					doIngest(fileForIngest);
				}
			}

		}
		catch (final Exception e) {
			logger.error("Caught exception ingesting from inbox: " + inDirectory.getAbsolutePath());
			logger.error(e);
		}
		finally {
			// just in case
			setBulkFileLoading(false);
		}
	}

	public File getInDirectory() {
		return inDirectory;
	}

	public void setInDirectory(
			final File inDirectory ) {
		this.inDirectory = inDirectory;
	}

	public File getSuccessDir() {
		return successDir;
	}

	public void setSuccessDir(
			final File successDir ) {
		this.successDir = successDir;
	}

	public File getErrorDir() {
		return errorDir;
	}

	public void setErrorDir(
			final File errorDir ) {
		this.errorDir = errorDir;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(
			final String fileSuffix ) {
		this.fileSuffix = fileSuffix;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(
			final String filePattern ) {
		this.filePattern = filePattern;
	}

	public boolean isBulkFileLoading() {
		return bulkFileLoading;
	}

	private void setBulkFileLoading(
			final boolean bulkFileLoading ) {
		this.bulkFileLoading = bulkFileLoading;
	}
}
