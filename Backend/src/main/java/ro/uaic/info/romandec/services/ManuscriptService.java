package ro.uaic.info.romandec.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.uaic.info.romandec.exceptions.InvalidDataException;
import ro.uaic.info.romandec.exceptions.NoAvailableImageForAnnotator;
import ro.uaic.info.romandec.models.Manuscript;
import ro.uaic.info.romandec.models.ManuscriptMetadata;
import ro.uaic.info.romandec.repository.ManuscriptMetadataRepository;
import ro.uaic.info.romandec.repository.ManuscriptRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@Service
public class ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;

    private final ManuscriptMetadataRepository manuscriptMetadataRepository;

    private final String databaseDirectory = "../Database/";

    @Autowired
    public ManuscriptService(ManuscriptRepository manuscriptRepository,
                             ManuscriptMetadataRepository manuscriptMetadataRepository)
    {
        this.manuscriptRepository = manuscriptRepository;
        this.manuscriptMetadataRepository = manuscriptMetadataRepository;
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
    throws NoAvailableImageForAnnotator
    {
        String pathOfRandomNotDecipheredImage = manuscriptRepository.getRandomNotDecipheredManuscript();
        if (pathOfRandomNotDecipheredImage == null)
        {
            throw new NoAvailableImageForAnnotator("No image available for annotators");
        }
        return new File(pathOfRandomNotDecipheredImage);
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
}
