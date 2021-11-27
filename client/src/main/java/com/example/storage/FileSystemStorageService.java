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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileSystemStorageService implements StorageService {

	Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

	private Path rootLocation;

	@Value( "${storage.path}" )
	private String storagePath;

	@PostConstruct
	void postConstruct(){
		this.rootLocation = Paths.get(storagePath);
	}

	@Override
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

	@Override
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


	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
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

	@Override
	public Resource loadPackFileAsResource(String packId) {
		List<Path> neededFile = loadAll()
				.filter(path -> (path.getFileName().toString().contains(packId)))
				.collect(Collectors.toList());
		return loadAsResource(neededFile.get(0).getFileName().toString());
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
