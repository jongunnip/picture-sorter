package org.gunnip.picturesorter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Take jpg images or mp4 files from subdirectories of a source dir matching a patterns
 * and moves to a subfolder of a target dir where the subfoler is the YYYY_MM_DD date. 
 */
public class PictureSorter {
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			fail("Usage: PictureSorter <source-dir> <source-dir-patttern> <target-dir>");
		}
		Path sourceDir = Paths.get(args[0]);
		checkDir(sourceDir);
		String sourceDirPattern = args[1];
		Path targetDir = Paths.get(args[2]);
		checkDir(targetDir);
		
		Files.list(sourceDir)
			.filter(dir -> Files.isDirectory(dir) && dir.toFile().getName().matches(sourceDirPattern))
			.forEach(dir -> processImageDir(dir, targetDir) );
	}
	
	private static void processImageDir(Path dir, Path targetDir) {
		System.out.printf("Checking directory %s\n", dir);
		try {
			Files.list(dir)
				.filter(file -> Files.isRegularFile(file) && 
						(file.toFile().getName().toLowerCase().endsWith(".jpg") || file.toFile().getName().toLowerCase().endsWith(".mp4")))
				.forEach(file -> moveFile(file, targetDir));
			
			if (Files.list(dir).count() == 0) {
				System.out.printf("\tDeleting directory %s\n", dir);
				dir.toFile().deleteOnExit();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void moveFile(Path file, Path targetDir)  {
		String modificationDate = getModificationDate(file);
		Path newFile = Paths.get(targetDir.toString(), modificationDate, file.toFile().getName());
		newFile.toFile().getParentFile().mkdirs();
		System.out.printf("\tFile %s moving to %s\n", file.toFile().getAbsolutePath(), newFile);
		try {
			Files.move(file, newFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
}
