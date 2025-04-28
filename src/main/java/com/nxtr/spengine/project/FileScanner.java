package com.nxtr.spengine.project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FileScanner {

	public static final int NEW_ENTRY_ONLY = 0;
	public static final int ALL = 1;
	private Map<String, FileMeta> existingFiles = new HashMap<>();
	private Map<String, File> modifiedFiles = new HashMap<>();
	private Map<String, File> newFiles = new HashMap<>();
	private Map<String, File> deletedFiles = new HashMap<>();
	private File baseDirectory;
	private long time;

	private boolean scannerRunning = false;
	private Consumer<FileScanner> consumer;
	private int mode = ALL;

	public FileScanner(File directory, Consumer<FileScanner> consumer, long time) {
		this.baseDirectory = directory;
		this.time = time;
		this.consumer = consumer;
	}

	public Map<String, FileMeta> getExistingFiles() {
		return existingFiles;
	}

	public Map<String, File> getModifiedFiles() {
		return modifiedFiles;
	}

	public Map<String, File> getNewFiles() {
		return newFiles;
	}

	public Map<String, File> getDeletedFiles() {
		return deletedFiles;
	}

	public void start() {
		if (this.time == -1) {
			scannerRunning = false;
			scan(mode);
		} else if (!scannerRunning) {
			scannerRunning = true;
			new Thread(() -> {
				while (scannerRunning) {
					try {
						scan(mode);
						Thread.sleep(this.time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void stop() {
		scannerRunning = false;
	}

	public void skipe(int mode) {
		scan(mode, true);
	}

	public void scan(int mode) {
		scan(mode, false);
	}

	private void scan(int mode, boolean skipe) {
		newFiles.clear();
		deletedFiles.clear();
		modifiedFiles.clear();
		scan(baseDirectory, existingFiles, modifiedFiles, newFiles, mode);
		existingFiles.entrySet().forEach(e -> {
			if (!e.getValue().file.exists())
				deletedFiles.put(e.getKey(), e.getValue().file);
		});
		if (!skipe)
			consumer.accept(this);
		newFiles.forEach((k, v) -> existingFiles.put(k, new FileMeta(v, v.lastModified())));
	}

	private void scan(File directory, Map<String, FileMeta> existingFiles, Map<String, File> modifiedFiles,
			Map<String, File> newFiles, int mode) {
		for (File file : directory.listFiles()) {
			String relPath = toRelativePath(file.getPath());
			var existing = existingFiles.get(relPath);
			if (existing != null) {
				if (file.lastModified() != existing.lastMofidied) {
					modifiedFiles.put(relPath, file);
					existing.lastMofidied = file.lastModified();
				}
			} else {
				newFiles.put(relPath, file);
				if (mode == NEW_ENTRY_ONLY)
					continue;
			}
			if (file.isDirectory())
				scan(file, existingFiles, modifiedFiles, newFiles, mode);
		}
	}

	private String toRelativePath(String path) {
		return path.substring(baseDirectory.getAbsolutePath().length() + File.separator.length());
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

}
