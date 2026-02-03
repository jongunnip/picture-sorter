package org.gunnip.picturesorter;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Take jpg images or mp4 files from subdirectories of a source dir
 * and moves to a subfolder of a target dir where the subfolder is the YYYY_MM_DD date.
 * If the target file is the same as source file, the file is not moved.
 */
public class PictureSorter {
	private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".mp4"};
	private static final String DATE_FORMAT_PATTERN = "yyyy_MM_dd";
	private static final int MAX_FILENAME_COLLISION_ATTEMPTS = 10000;
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			fail("Usage: PictureSorter <source-dir> <target-dir> <prefix>");
		}
		Path sourceDir = Paths.get(args[0]);
		Path targetDir = Paths.get(args[1]);
		String prefix = args[2];
		new PictureSorter().processImageDir(sourceDir, targetDir, prefix);
	}
	
	void processImageDir(Path dir, Path targetDir, String prefix) {
		checkDir(dir);
		checkDir(targetDir);
		System.out.printf("Checking directory %s\n", dir);
		try {
			Files.list(dir)
				.filter(file -> {
					if (!Files.isRegularFile(file)) {
						return false;
					}
					String nameLower = file.toFile().getName().toLowerCase();
					for (String ext : SUPPORTED_EXTENSIONS) {
						if (nameLower.endsWith(ext)) {
							return true;
						}
					}
					return false;
				})
				.forEach(file -> moveFile(file, targetDir, prefix));
		} catch (IOException e) {
			throw new RuntimeException("Failed to process directory: " + dir, e);
		}
	}

	private static void moveFile(Path file, Path targetDir, String prefix)  {
		String modificationDate = getModificationDate(file);
		Path newFile;
		try {
			newFile = getNewFileName(file, targetDir, modificationDate, prefix);
		}
		catch (DuplicateFileException e) {
			// ignore
			System.out.printf("\t%s already exists with same name and content in %s\n", file.toFile().getName(), targetDir.toFile().getAbsolutePath());
			return;
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to process file: " + file, e);
		}
		newFile.toFile().getParentFile().mkdirs();
		System.out.printf("\tFile %s moving to %s\n", file.toFile().getName(), newFile);
		try {
			Files.move(file, newFile);
		} catch (IOException e) {
			throw new RuntimeException("Failed to move file " + file + " to " + newFile, e);
		}
	}

	private static Path getNewFileName(Path file, Path targetDir, String modificationDate, String prefix) throws IOException, DuplicateFileException {
		String sourceMd5;
		try (InputStream is = Files.newInputStream(file)) {
			sourceMd5 = DigestUtils.md5Hex(is);
		}

		Path newFile;
		int i = 0;
		do {
			if (i >= MAX_FILENAME_COLLISION_ATTEMPTS) {
				throw new RuntimeException(String.format(
					"Failed to find unique filename for %s after %d attempts",
					file.toFile().getName(),
					MAX_FILENAME_COLLISION_ATTEMPTS
				));
			}

			String fileName = i > 0 ? file.toFile().getName().replace(".", "_" + i + ".") : file.toFile().getName();
			newFile = Paths.get(targetDir.toString(), modificationDate, prefix + "_" + fileName);

			if (newFile.toFile().exists()) {
				try (InputStream is = Files.newInputStream(newFile)) {
					if (sourceMd5.equals(DigestUtils.md5Hex(is))) {
						throw new DuplicateFileException();
					}
				}
			}

			Path newFileWithoutPrefix = Paths.get(targetDir.toString(), modificationDate, fileName);
			if (newFileWithoutPrefix.toFile().exists()) {
				try (InputStream is = Files.newInputStream(newFileWithoutPrefix)) {
					if (sourceMd5.equals(DigestUtils.md5Hex(is))) {
						throw new DuplicateFileException();
					}
				}
			}

			i++;
			System.out.printf("\tChecking for %s if target file %s exists\n", file.toFile().getName(), newFile.toFile().getName());
		} while (newFile.toFile().exists());
		return newFile;
	}

	private static void checkDir(Path sourceDir) {
		if (!sourceDir.toFile().exists()) {
			fail(String.format("Directory %s does not exist", sourceDir));
		}
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(1);
	}

	private static String getModificationDate(Path file) {
		DateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		BasicFileAttributes attr;
		try {
			attr = Files.readAttributes(file, BasicFileAttributes.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read attributes for file: " + file, e);
		}
		return df.format(attr.lastModifiedTime().toMillis());
	}

	private static class DuplicateFileException extends Exception {

	}
}
