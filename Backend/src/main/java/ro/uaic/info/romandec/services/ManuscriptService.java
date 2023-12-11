package ro.uaic.info.romandec.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.models.dtos.SpecificManuscriptDto;
import ro.uaic.info.romandec.Response.ManuscriptDetailedResponse;
import ro.uaic.info.romandec.Response.ManuscriptPreviewResponse;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableDataForGivenInput;
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

    private String extractFilenameWithoutExtension(String filename)
    {
        if (filename == null || filename.isEmpty())
        {
            return filename;
        }

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex <= 0)
        {
            return filename;
        }

        return filename.substring(0, dotIndex);
    }

    private String getDecipheredTranscriptFilename(String filename)
    {
        return filename.split("\\.")[0] + ".txt";
    }


    private Manuscript checkAndGetManuscriptByRequest(SpecificManuscriptDto request, UUID userId)
            throws InvalidDataException, NoAvailableDataForGivenInput {

        if (request == null){
            throw new InvalidDataException("Manuscript id can't be null.");
        }

        Optional<Manuscript> manuscript = manuscriptRepository.getManuscriptByIdAndNameAndUserId(
                request.getManuscriptId(),
                request.getName(),
                userId);

        if (manuscript.isEmpty()){
            throw new NoAvailableDataForGivenInput("No manuscript found for id:" + manuscript);
        }

        return manuscript.get();
    }

    public void saveAnnotatorDecipheredManuscript(String originalImageFilename, String decipheredText)
            throws InvalidDataException, IOException
    {
        String filenameWithoutExtension = extractFilenameWithoutExtension(originalImageFilename);
        Path imageDirectory = Paths.get(databaseDirectory, filenameWithoutExtension);

        if (!Files.exists(imageDirectory))
        {
            throw new InvalidDataException("This image does not a directory associated");
        }

        Path filePath = imageDirectory.resolve(Objects.requireNonNull(getDecipheredTranscriptFilename(originalImageFilename)) );

        try
        {
            Path imageCompletePath = imageDirectory.resolve(originalImageFilename);

            Manuscript manuscript = manuscriptRepository.getManuscriptByPathToImage(imageCompletePath.toAbsolutePath().toString());
            manuscript.setPathToDecipheredText(filePath.toAbsolutePath().toString());
            manuscriptRepository.save(manuscript);

            Files.writeString(filePath, decipheredText,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error at creating the file with the deciphered text");
        }
    }

    public List<ManuscriptPreviewResponse> getAllUsersManuscripts(UUID userId)
            throws NoAvailableDataForGivenInput {


        List<Manuscript> usersManuscripts = manuscriptRepository.getAllByUserId(userId);

        if (usersManuscripts == null) {
            throw new NoAvailableDataForGivenInput("No available manuscripts for this user.");
        }

        List<ManuscriptPreviewResponse> response = new ArrayList<>();

        for (Manuscript m : usersManuscripts) {
            response.add(ManuscriptPreviewResponse
                    .builder()
                    .manuscriptId(m.getId())
                    .name(m.getName())
                    .build());
        }

        return response;
    }

    public ManuscriptDetailedResponse getSpecificManuscript(SpecificManuscriptDto manuscriptRequest, UUID userId)
            throws InvalidDataException, NoAvailableDataForGivenInput {

        Manuscript manuscript =  checkAndGetManuscriptByRequest(manuscriptRequest, userId);
        return ManuscriptDetailedResponse
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
            throws NoAvailableDataForGivenInput, InvalidDataException {

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
            throws NoAvailableDataForGivenInput
    {
        String pathOfRandomNotDecipheredImage = manuscriptRepository.getRandomNotDecipheredManuscript();
        if (pathOfRandomNotDecipheredImage == null)
        {
            throw new NoAvailableDataForGivenInput("No image available for annotators");
        }
        return new File(pathOfRandomNotDecipheredImage);
    }

    public FileSystemResource downloadSpecificManuscript(SpecificManuscriptDto request, UUID userId)
            throws NoAvailableDataForGivenInput, InvalidDataException {

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
}
