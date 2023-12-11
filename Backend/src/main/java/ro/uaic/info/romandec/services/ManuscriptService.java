package ro.uaic.info.romandec.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.models.dtos.SpecificManuscriptDto;
import ro.uaic.info.romandec.models.dtos.ManuscriptDetailedResponseDto;
import ro.uaic.info.romandec.models.dtos.ManuscriptPreviewResponseDto;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableDataForGivenInputException;
import ro.uaic.info.romandec.models.Manuscript;
import ro.uaic.info.romandec.models.ManuscriptMetadata;
import ro.uaic.info.romandec.repository.ManuscriptMetadataRepository;
import ro.uaic.info.romandec.repository.ManuscriptRepository;
import ro.uaic.info.romandec.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final UserRepository userRepository;

    private final ManuscriptMetadataRepository manuscriptMetadataRepository;

    private final String databaseDirectory = "../Database/";

    @Autowired
    public ManuscriptService(ManuscriptRepository manuscriptRepository,
                             ManuscriptMetadataRepository manuscriptMetadataRepository,
                             UserRepository userRepository)
    {
        this.manuscriptRepository = manuscriptRepository;
        this.manuscriptMetadataRepository = manuscriptMetadataRepository;
        this.userRepository = userRepository;
    }
    public void saveAnnotatorDecipheredManuscript(String originalImageFilename, String decipheredText)
            throws InvalidDataException, IOException {
        String filenameWithoutExtension = extractFilenameWithoutExtension(originalImageFilename);
        Path imageDirectory = Paths.get(databaseDirectory, filenameWithoutExtension);
        verifyImageDirectory(imageDirectory);
        Path filePath = resolveFilePath(imageDirectory, originalImageFilename);
        updateManuscript(imageDirectory, originalImageFilename, filePath);
        Files.writeString(filePath, decipheredText, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    public List<ManuscriptPreviewResponseDto> getAllUsersManuscripts(UUID userId)
            throws NoAvailableDataForGivenInputException {
        List<Manuscript> usersManuscripts = manuscriptRepository.getAllByUserId(userId);
        if (usersManuscripts == null) {
            throw new NoAvailableDataForGivenInputException("No available manuscripts for this user.");
        }
        return usersManuscripts.stream()
                .map(m -> ManuscriptPreviewResponseDto.builder()
                        .manuscriptId(m.getId())
                        .name(m.getName())
                        .build())
                .collect(Collectors.toList());
    }
    public ManuscriptDetailedResponseDto getSpecificManuscript(SpecificManuscriptDto manuscriptRequest, UUID userId)
            throws InvalidDataException, NoAvailableDataForGivenInputException {

        Manuscript manuscript =  checkAndGetManuscriptByRequest(manuscriptRequest, userId);
        return ManuscriptDetailedResponseDto
                .builder()
                .manuscriptId(manuscript.getId())
                .name(manuscript.getName())
                .title(manuscript.getManuscriptMetadata().getTitle())
                .author(manuscript.getManuscriptMetadata().getAuthor())
                .deciphered(manuscript.getPathToDecipheredText() != null)
                .description(manuscript.getManuscriptMetadata().getDescription())
                .build();
    }
    public void deleteSpecificManuscript(SpecificManuscriptDto manuscriptRequest, UUID userId)
            throws NoAvailableDataForGivenInputException, InvalidDataException {

        manuscriptRepository.delete(checkAndGetManuscriptByRequest(manuscriptRequest, userId));
    }
    public boolean initializeTestData(List<MultipartFile> images)
            throws InvalidDataException
    {
        if (images == null || images.isEmpty()) {
            throw new InvalidDataException("Invalid images for initialization");
        }

        for (MultipartFile image : images) {
            String originalFilename = image.getOriginalFilename();
            String filenameWithoutExtension = extractFilenameWithoutExtension(originalFilename);

            if (filenameWithoutExtension == null || filenameWithoutExtension.isEmpty()) {
                continue;
            }

            Path directoryPath = Paths.get(databaseDirectory, filenameWithoutExtension);
            try {
                if (!Files.exists(directoryPath)) {
                    Files.createDirectories(directoryPath);
                }

                Path imagePath = directoryPath.resolve(originalFilename);
                Files.copy(image.getInputStream(), imagePath);

                ManuscriptMetadata manuscriptMetadata = new ManuscriptMetadata();
                manuscriptMetadata = manuscriptMetadataRepository.save(manuscriptMetadata);

                Manuscript manuscript = Manuscript
                        .builder()
                        .manuscriptMetadata(manuscriptMetadata)
                        .pathToImage(imagePath.toAbsolutePath().toString())
                        .name(filenameWithoutExtension)
                        .build();

                manuscriptRepository.save(manuscript);

            } catch (IOException e) {
                System.out.println("Failed to save the image for: " + originalFilename);
                return false;
            }
        }
        return true;
    }
    public File getRandomNotDecipheredImage()
            throws NoAvailableDataForGivenInputException
    {
        String pathOfRandomNotDecipheredImage = manuscriptRepository.getRandomNotDecipheredManuscript();
        if (pathOfRandomNotDecipheredImage == null)
        {
            throw new NoAvailableDataForGivenInputException("No image available for annotators");
        }
        return new File(pathOfRandomNotDecipheredImage);
    }
    public FileSystemResource downloadSpecificManuscript(SpecificManuscriptDto request, UUID userId)
            throws NoAvailableDataForGivenInputException, InvalidDataException {

        Manuscript manuscript = checkAndGetManuscriptByRequest(request, userId);

        if (manuscript.getPathToDecipheredText() == null){
            throw new InvalidDataException("This manuscript is not deciphered");
        }

        File file = new File(manuscript.getPathToDecipheredText());

        if (!file.exists()) {
            throw new InvalidDataException("File does not exist.");
        }

        return new FileSystemResource(file);
    }
    private void verifyImageDirectory(Path imageDirectory)
            throws InvalidDataException {
        if (!Files.exists(imageDirectory)) {
            throw new InvalidDataException("This image does not a directory associated");
        }
    }
    private Path resolveFilePath(Path imageDirectory, String originalImageFilename) {
        return imageDirectory.resolve(Objects.requireNonNull(getDecipheredTranscriptFilename(originalImageFilename)));
    }
    private void updateManuscript(Path imageDirectory, String originalImageFilename, Path filePath) {
        Path imageCompletePath = imageDirectory.resolve(originalImageFilename);
        Optional<Manuscript> manuscript = manuscriptRepository.getManuscriptByPathToImage(imageCompletePath.toAbsolutePath().toString());
        if (manuscript.isPresent()){
            manuscript.get().setPathToDecipheredText(filePath.toAbsolutePath().toString());
            manuscriptRepository.save(manuscript.get());
        }
    }
    private String extractFilenameWithoutExtension(String filename)
    {
        Path path = Paths.get(filename);
        return path.getName(path.getNameCount() - 1).toString();
    }
    private String getDecipheredTranscriptFilename(String filename)
    {
        String filenameWithoutExtension = extractFilenameWithoutExtension(filename);
        return filenameWithoutExtension + ".txt";
    }
    private Manuscript checkAndGetManuscriptByRequest(SpecificManuscriptDto request, UUID userId)
            throws InvalidDataException, NoAvailableDataForGivenInputException {

        if (request == null){
            throw new InvalidDataException("Manuscript id can't be null.");
        }

        return manuscriptRepository.getManuscriptByIdAndNameAndUserId(
                request.getManuscriptId(),
                request.getName(),
                userId).orElseThrow(() -> new NoAvailableDataForGivenInputException("No manuscript found for id:" +
                request.getManuscriptId()));
    }
}
