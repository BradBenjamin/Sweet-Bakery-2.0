package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final String DATA_FORMAT_KEY = "data.format";
    private static final String DATA_FILE_PATH_KEY = "data.file.path";

    public static String getDataFormat() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("settings.properties"));
        return props.getProperty(DATA_FORMAT_KEY);
    }

    public static String getDataFilePath() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("settings.properties"));
        return props.getProperty(DATA_FILE_PATH_KEY);
    }
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream("settings.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    public static String getRepositoryType() {
        return properties.getProperty("repository.type", "text");
    }

    public static String getRepositoryFilePath() {
        return properties.getProperty("repository.filepath", "data/cakes.txt");
    }
}
