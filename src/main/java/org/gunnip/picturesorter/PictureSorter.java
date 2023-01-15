package org.gunnip.picturesorter;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
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
				.filter(file -> Files.isRegularFile(file) && 
						(file.toFile().getName().toLowerCase().endsWith(".jpg") || file.toFile().getName().toLowerCase().endsWith(".mp4")))
				.forEach(file -> moveFile(file, targetDir, prefix));
		} catch (IOException e) {
			throw new RuntimeException(e);
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
			throw new RuntimeException(e);
		}
		newFile.toFile().getParentFile().mkdirs();
		System.out.printf("\tFile %s moving to %s\n", file.toFile().getName(), newFile);
		try {
			Files.move(file, newFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Path getNewFileName(Path file, Path targetDir, String modificationDate, String prefix) throws IOException, DuplicateFileException {
		String md5 = DigestUtils.md5Hex(Files.newInputStream(file));
		Path newFile;
		int i = 0;
		do {
			String fileName = i > 0 ? file.toFile().getName().replace(".", "_" + i + ".") : file.toFile().getName();
			newFile = Paths.get(targetDir.toString(), modificationDate, prefix + "_" + fileName);
			if (newFile.toFile().exists() && md5.equals(DigestUtils.md5Hex(Files.newInputStream(newFile)))) {
				throw new DuplicateFileException();
			}
			Path newFileWithoutPrefix = Paths.get(targetDir.toString(), modificationDate, fileName);
			if (newFileWithoutPrefix.toFile().exists() && md5.equals(DigestUtils.md5Hex(Files.newInputStream(newFileWithoutPrefix)))) {
				throw new DuplicateFileException();
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
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
		BasicFileAttributes attr;
		try {
			attr = Files.readAttributes(file, BasicFileAttributes.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return df.format(attr.lastModifiedTime().toMillis());
	}

	private static class DuplicateFileException extends Exception {

	}
}
