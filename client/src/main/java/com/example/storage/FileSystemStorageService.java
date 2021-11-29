package com.example.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileSystemStorageService {

	Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

	private Path rootLocation;

	@Value( "${storage.path}" )
	private String storagePath;
	@Value( "${storage.filesFolder}" )
	public String filesFolder;

	public int packBufferSize = 100 * 1024;

	@PostConstruct
	void postConstruct(){
		this.rootLocation = Paths.get(storagePath);
	}

	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(
					Paths.get(file.getOriginalFilename()))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

//	@Override
//	public void store(Resource file) {
//		logger.info("Trying to store a file...");
//		try {
//			logger.info("Storing the file: " + file.getFile().getTotalSpace());
//			if (file.contentLength() == 0) {
//				throw new StorageException("Failed to store empty file.");
//			}
//			Path destinationFile = this.rootLocation.resolve(
//							Paths.get(Objects.requireNonNull(file.getFilename())))
//					.normalize().toAbsolutePath();
//			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
//				// This is a security check
//				throw new StorageException(
//						"Cannot store file outside current directory.");
//			}
//			try (InputStream inputStream = file.getInputStream()) {
//				Files.copy(inputStream, destinationFile,
//						StandardCopyOption.REPLACE_EXISTING);
//			}
//		}
//		catch (IOException e) {
//			throw new StorageException("Failed to store file.", e);
//		}
//	}

	public void store(byte[] fileBytes, String fileName) {
		logger.info("Trying to store a file...");


		try {
			//logger.info("Storing the file: " + file.getFile().getTotalSpace());
			if (fileBytes.length == 0) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(
							Paths.get(Objects.requireNonNull(fileName)))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
//			try (InputStream inputStream = file.getInputStream()) {
//				Files.copy(inputStream, destinationFile,
//						StandardCopyOption.REPLACE_EXISTING);
//			}
			Files.write(destinationFile, fileBytes);
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}


	public Stream<Path> loadAllPacks() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}
	public Stream<Path> loadAllFiles() {
		Path filesFolderPath = rootLocation.resolve(filesFolder);
		try {
			return Files.walk(filesFolderPath, 1)
					.filter(path -> !path.equals(filesFolderPath))
					.map(filesFolderPath::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}


	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	public Resource loadPackFileAsResource(String packId) {
		List<Path> neededFile = loadAllPacks()
				.filter(path -> (path.getFileName().toString().contains(packId)))
				.collect(Collectors.toList());
		return loadAsResource(neededFile.get(0).getFileName().toString());
	}

	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	public void splitFileToPacks(String fullFileName) {
		Path filesFolderPath = rootLocation.resolve(filesFolder);
		Path file = filesFolderPath.resolve(fullFileName);

		String fileId = UUID.randomUUID().toString();

		try {
			Resource resource = new UrlResource(file.toUri());
			splitFileAndStorePacks(resource.getFile(), fileId);
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + fullFileName, e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void splitFileAndStorePacks(File file, String fileId) throws IOException {
		try {
			long packNumber = 0;
			int sizeOfFiles = packBufferSize;
			byte[] buffer = new byte[sizeOfFiles];

			try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {

				int bytesAmount = 0;
				while ((bytesAmount = bis.read(buffer)) > 0) {
					try (OutputStream out = new ByteArrayOutputStream()) {
						out.write(buffer, 0, bytesAmount);
						out.flush();

						String packId = UUID.randomUUID().toString();
						store(buffer, joinPackId_FileId_PackNumber(packId, fileId, packNumber));
						packNumber++;
					}
				}
			}
		} catch (Exception e) {
			//get the error
			logger.error("EEE: " + e.getMessage());
		}
	}

	public void constructFilesFromPacks(String fileId) {
		logger.info("Constructing a file: " + fileId);
		constructFileFromPacks(fileId, rootLocation.resolve(filesFolder));
	}
	public void constructFileFromPacks(String fileId, Path storageDir){


		List<Path> thisFilePacks = loadAllPacks()
				.filter(path -> {
					String packFileName = path.getFileName().toString();
					if(!packFileName.startsWith(".") && !packFileName.equals(filesFolder)){
						String[] packId_fileId_packNumber = path.getFileName().toString().split("\\|");

						if(packId_fileId_packNumber[1].equals(fileId))
							logger.info("Found pack of this file: " + packId_fileId_packNumber[0]);
						return packId_fileId_packNumber[1].equals(fileId);
					}
					return false;
				})
				.collect(Collectors.toList());

		logger.info("Found file packs: " + thisFilePacks.size());
		if(thisFilePacks.size() > 1) {
			thisFilePacks.sort((o1, o2) -> {
				String[] packId_fileId_packNumber1 = o1.getFileName().toString().split("\\|");
				String[] packId_fileId_packNumber2 = o2.getFileName().toString().split("\\|");
				return Integer.compare(Integer.parseInt(packId_fileId_packNumber1[2]), Integer.parseInt(packId_fileId_packNumber2[2]));
			});
		}




		try {
			FileOutputStream fileOutputStream = new FileOutputStream(storageDir.resolve(fileId).toAbsolutePath().toString());
			for (Path packPath:thisFilePacks) {
				String[] packId_fileId_packNumber = packPath.getFileName().toString().split("\\|");
				try {
					logger.info("Adding pack to the destination file... ");
					addPackToFileOutputStream(loadPackFileAsResource(packId_fileId_packNumber[0]).getFile(), fileOutputStream);
				} catch (IOException e) {
					logger.error("Cannot get pack file with pack id: " + packId_fileId_packNumber[0]);
				}
			}

			fileOutputStream.flush();
		} catch (FileNotFoundException e) {
			logger.error("Cannot open dest file to construct: " + fileId);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	public void addPackToFileOutputStream(File packFile, FileOutputStream fileOutputStream){
		try (FileInputStream fis = new FileInputStream(packFile); BufferedInputStream bis = new BufferedInputStream(fis)) {
			byte[] buffer = new byte[packBufferSize];
			int bytesAmount = 0;
			while ((bytesAmount = bis.read(buffer)) > 0) {

				logger.info("Writing to file: " + bytesAmount);
				fileOutputStream.write(buffer, 0, bytesAmount);
				fileOutputStream.flush();
//					out.write(buffer, 0, bytesAmount);
//					out.flush();
//
//					String packId = UUID.randomUUID().toString();
//					store(buffer, joinPackId_FileId_PackNumber(packId, fileId));

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String joinPackId_FileId_PackNumber(String packId, String fileId, long packNumber){
		return packId + "|" + fileId + "|" + packNumber;
	}


}
