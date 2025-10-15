package src.java.Singletons;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class FileManager {
    private static FileManager instance;

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }


    public List<String> getFile(String pathToRead) {
        List<String> lines;
        try {
            Path path = Paths.get(pathToRead);
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                System.out.println(line);
            }
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("IO Error");
        }

        return lines;
    }

    public void writeToFile(String path, String fileName, String contentToWrite) {
        String fullPath = path + "/" + fileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))) {
            writer.write(contentToWrite);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream serializeObject(Object object, String path, String objectName) {
        ObjectOutputStream out = null;

        try {
            FileOutputStream fileOut = new FileOutputStream(objectName);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public Object getObject (String filePath) throws IOException, ClassNotFoundException {
        Object object = null;
        try (FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
            object = (Object) ois.readObject();
        }
        return object;
    }

    public static void saveHashMapToFile(HashMap<String, Integer> hashMap, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                writer.write("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");
                writer.newLine();
            }
        }
    }

    public static LinkedHashMap<String, Integer> readLinkedHashMapFromFile(String filePath) throws IOException {
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Remove quotes and split by colon
                String[] parts = line.replace("\"", "").split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    Integer value = Integer.parseInt(parts[1].trim());
                    linkedHashMap.put(key, value);
                }
            }
        }
        return linkedHashMap;
    }
    public static void saveStringToFile(String string, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(string);
            writer.newLine();
        }
    }

}