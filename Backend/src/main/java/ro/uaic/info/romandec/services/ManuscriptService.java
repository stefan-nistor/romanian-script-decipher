package ro.uaic.info.romandec.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.models.ManuscriptMetadata;
import ro.uaic.info.romandec.models.dtos.*;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableDataForGivenInputException;
import ro.uaic.info.romandec.models.Manuscript;
import ro.uaic.info.romandec.repository.ManuscriptMetadataRepository;
import ro.uaic.info.romandec.repository.ManuscriptRepository;
import ro.uaic.info.romandec.repository.UserRepository;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ManuscriptMetadataRepository manuscriptMetadataRepository;
    private final File databaseDirectory;

    @Autowired
    public ManuscriptService(ManuscriptRepository manuscriptRepository,
                             ManuscriptMetadataRepository manuscriptMetadataRepository,
                             UserRepository userRepository,
                             RestTemplate restTemplate)
    {
        this.manuscriptRepository = manuscriptRepository;
        this.manuscriptMetadataRepository = manuscriptMetadataRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;

        File currentDirectory = new File(System.getProperty("user.dir")).getParentFile();
        databaseDirectory = new File(currentDirectory, "Database");
    }
    public List<ManuscriptPreviewResponseDto> getAllUsersManuscripts(UUID userId) {
        List<Manuscript> usersManuscripts = manuscriptRepository.getAllByUserId(userId);
        if (usersManuscripts == null) {
            throw new NoAvailableDataForGivenInputException("No available manuscripts for this user.");
        }
        return usersManuscripts.stream()
                .map(m -> ManuscriptPreviewResponseDto.builder()
                        .manuscriptId(m.getId())
                        .title(m.getManuscriptMetadata().getTitle())
                        .yearOfPublication(m.getManuscriptMetadata().getYearOfPublication())
                        .author(m.getManuscriptMetadata().getAuthor())
                        .build())
                .collect(Collectors.toList());
    }
    public ManuscriptDetailedResponseDto getSpecificManuscript(UUID manuscriptId, UUID userId) {

        Manuscript manuscript =  checkAndGetManuscriptByRequest(manuscriptId, userId);
        return ManuscriptDetailedResponseDto
                .builder()
                .manuscriptId(manuscript.getId())
                .filename(manuscript.getFilename())
                .title(manuscript.getManuscriptMetadata().getTitle())
                .author(manuscript.getManuscriptMetadata().getAuthor())
                .yearOfPublication(manuscript.getManuscriptMetadata().getYearOfPublication())
                .description(manuscript.getManuscriptMetadata().getDescription())
                .build();
    }
    public void deleteManuscript(UUID manuscriptId, UUID userId) {
        Manuscript manuscript = checkAndGetManuscriptByRequest(manuscriptId, userId);

        Path manuscriptDirectory = Paths.get(manuscript.getPathToOriginalText()).getParent();

        try {
            Files.walk(manuscriptDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Error at emptying the directory");
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        manuscriptRepository.delete(manuscript);
        manuscriptMetadataRepository.delete(manuscript.getManuscriptMetadata());

    }
    public File getRandomNotDecipheredImage() {
        String pathOfRandomNotDecipheredImage = manuscriptRepository.getRandomNotDecipheredManuscript();
        if (pathOfRandomNotDecipheredImage == null)
        {
            throw new NoAvailableDataForGivenInputException("No image available for annotators");
        }
        return new File(pathOfRandomNotDecipheredImage);
    }
    public FileSystemResource downloadOriginalManuscript(UUID manuscriptId, UUID userId) {

        Manuscript manuscript = checkAndGetManuscriptByRequest(manuscriptId, userId);

        if (manuscript.getPathToOriginalText() == null){
            throw new InvalidDataException("This manuscript is not deciphered");
        }

        File file = new File(manuscript.getPathToOriginalText());

        if (!file.exists()) {
            throw new InvalidDataException("File does not exist.");
        }
        return new FileSystemResource(file);

    }
    public ManuscriptPreviewResponseDto decipherTranscript(MultipartFile manuscriptFile, String decipherManuscriptJSON, UUID userId) {
        try {

            //deserialize the JSON
            DecipherManuscriptDto decipherManuscriptDto = new ObjectMapper().readValue(decipherManuscriptJSON, DecipherManuscriptDto.class);

//        URI otherUrl = URI.create("URL-FOR-DECIPHER");
//        ResponseEntity<byte[]> response = restTemplate.getForEntity(otherUrl, byte[].class);

            //create the directory for the newly added manuscript
            Path uploadedManuscriptDirectory = createDirectoryForUploadedManuscript(manuscriptFile.getOriginalFilename(), userId);

            //add the original file to the new directory
            if (Files.exists(uploadedManuscriptDirectory)){
                Path originalManuscriptPath = Paths.get(uploadedManuscriptDirectory.toString(), manuscriptFile.getOriginalFilename());
                manuscriptFile.transferTo(originalManuscriptPath);
            }

            //save into the db the manuscript with the associated metadata
            ManuscriptMetadata manuscriptMetadata = manuscriptMetadataRepository.save(ManuscriptMetadata
                    .builder()
                    .title(decipherManuscriptDto.getTitle())
                    .description(decipherManuscriptDto.getDescription())
                    .author(decipherManuscriptDto.getAuthor())
                    .yearOfPublication((decipherManuscriptDto.getYearOfPublication()))
                    .build());

            Manuscript manuscript =  manuscriptRepository.save(Manuscript
                    .builder()
                    .filename(manuscriptFile.getOriginalFilename())
                    .user(userRepository.getReferenceById(userId))
                    .pathToOriginalText(String.valueOf(Paths.get(uploadedManuscriptDirectory.toString(),
                                        manuscriptFile.getOriginalFilename())))
                    .pathToDecipheredText("not yet")
                    .manuscriptMetadata(manuscriptMetadata)
                    .build());

            //return the necessary info
            return ManuscriptPreviewResponseDto
                    .builder()
                    .manuscriptId(manuscript.getId())
                    .title(manuscript.getManuscriptMetadata().getTitle())
                    .yearOfPublication(manuscript.getManuscriptMetadata().getYearOfPublication())
                    .author(manuscript.getManuscriptMetadata().getAuthor())
                    .build();
        }
        catch (IOException e){
            return null;
        }
    }
    private Manuscript checkAndGetManuscriptByRequest(UUID manuscriptId, UUID userId) {

        if (manuscriptId == null){
            throw new InvalidDataException("Manuscript id can't be null.");
        }

        return manuscriptRepository.getManuscriptByIdAndUserId(
                manuscriptId,
                userId).orElseThrow(() -> new NoAvailableDataForGivenInputException("No manuscript found for id:" +
                manuscriptId));
    }
    private String getFilenameWithoutExtension(String filename){
        return filename.substring(0, filename.lastIndexOf('.'));
    }
    private String getFilenameForDecipheredText(String filename){
        return filename.substring(0, filename.lastIndexOf('.')) + "_deciphered";
    }
    private Path createDirectoryForUploadedManuscript(String filename, UUID userId) throws IOException {

        String filenameWithoutExtension = getFilenameWithoutExtension(filename);
        Path userDirectory = Paths.get(databaseDirectory.getAbsolutePath(), userId.toString());

        if (!Files.exists(userDirectory)) {
            Files.createDirectories(userDirectory);
        }

        Path uploadedManuscriptDirectory = Paths.get(userDirectory.toString(), filenameWithoutExtension);

        if (!Files.exists(uploadedManuscriptDirectory)) {
            Files.createDirectories(uploadedManuscriptDirectory);
        }

        return uploadedManuscriptDirectory;
    }
}
