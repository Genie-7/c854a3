package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>(); // A map to store child nodes
    boolean isEndOfWord = false; // Flag to indicate if this node marks the end of a word
    int frequency = 0; // Frequency count of words ending at this node

    TrieNode() {} // Constructor for TrieNode
}

class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode(); // Initialize root node
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) { // Iterate through each character in the word
            node = node.children.computeIfAbsent(c, k -> new TrieNode()); // Create new node if absent
        }
        node.isEndOfWord = true; // Mark the end of a word
        node.frequency++; // Increment frequency count
    }

    public List<String> findWordsWithPrefix(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) { // Navigate to the end of the prefix
            node = node.children.get(c);
            if (node == null) { // If prefix is not found
                return new ArrayList<>();
            }
        }
        return findWordsFromNode(node, prefix); // Find all words from this node
    }

    private List<String> findWordsFromNode(TrieNode node, String prefix) {
        List<String> result = new ArrayList<>();
        if (node.isEndOfWord) { // If the current node is the end of a word
            result.add(prefix); // Add the prefix to the result list
        }
        for (char c : node.children.keySet()) { // Recursively find all words from this node
            result.addAll(findWordsFromNode(node.children.get(c), prefix + c));
        }
        return result;
    }
}

class Autocomplete {
    private Trie trie;

    public Autocomplete() {
        trie = new Trie(); // Initialize the Trie
    }

    public void buildVocabularyFromRemaxFile(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) { // Read CSV file
            List<String[]> records = reader.readAll();
            boolean isHeader = true;
            for (String[] record : records) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                if (record.length > 2) { // Ensure there are enough columns
                    String address = record[1];
                    String details = record[2];

                    String combined = address + " " + details;

                    String[] words = combined.split("\\W+"); // Split combined string into words
                    for (String word : words) {
                        if (!word.isEmpty() && word.matches("[a-zA-Z]+")) { // Filter out non-alphabetical words
                            trie.insert(word.toLowerCase()); // Insert word into Trie
                        }
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void buildVocabularyFromCombinedFile(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isHeader = true;
            for (String[] record : records) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                for (String cell : record) {
                    String[] words = cell.split("\\W+"); // Split cell content into words
                    for (String word : words) {
                        if (!word.isEmpty() && word.matches("[a-zA-Z]+")) { // Filter out non-alphabetical words
                            trie.insert(word.toLowerCase()); // Insert word into Trie
                        }
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void buildVocabularyFromScrapedDataFile(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isHeader = true;
            for (String[] record : records) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                if (record.length > 4) { // Ensure there are enough columns
                    String address = record[1];
                    String location = record[2];
                    String type = record[3];
                    String listing = record[4];

                    String combined = address + " " + location + " " + type + " " + listing;

                    String[] words = combined.split("\\W+"); // Split combined string into words
                    for (String word : words) {
                        if (!word.isEmpty() && word.matches("[a-zA-Z]+")) { // Filter out non-alphabetical words
                            trie.insert(word.toLowerCase()); // Insert word into Trie
                        }
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void buildVocabularyFromExcelFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                Cell firstColumnCell = row.getCell(0); // Get the first column cell

                if (firstColumnCell != null) {
                    String combinedText = getCellValue(firstColumnCell); // Get cell value as string

                    String[] words = combinedText.split("\\W+"); // Split cell content into words
                    for (String word : words) {
                        if (!word.isEmpty() && word.matches("[a-zA-Z]+")) { // Filter out non-alphabetical words
                            trie.insert(word.toLowerCase()); // Insert word into Trie
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue(); // Return string value
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()); // Return numeric value as string
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()); // Return boolean value as string
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue(); // Return formula result as string
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue()); // Return formula result as numeric string
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue()); // Return formula result as boolean string
                }
            default:
                return "";
        }
    }

    public void buildVocabularyFromZoloWindsorListingsFile(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isHeader = true;
            for (String[] record : records) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                for (int i = 4; i <= 9; i++) { // Loop through specific columns
                    if (i < record.length && record[i] != null) {
                        String[] words = record[i].split("\\W+"); // Split cell content into words
                        for (String word : words) {
                            if (!word.isEmpty() && word.matches("[a-zA-Z]+")) { // Filter out non-alphabetical words
                                trie.insert(word.toLowerCase()); // Insert word into Trie
                            }
                        }
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSuggestions(String prefix) {
        prefix = prefix.toLowerCase(); // Convert prefix to lowercase
        return trie.findWordsWithPrefix(prefix); // Find and return words with given prefix
    }
}

public class Main {
    public static void main(String[] args) {
        Autocomplete autocomplete = new Autocomplete(); // Initialize Autocomplete

        // Build vocabulary from various sources
        autocomplete.buildVocabularyFromRemaxFile("src/main/resources/remax_listings.csv");
        autocomplete.buildVocabularyFromCombinedFile("src/main/resources/combined_scraped_data.csv");
        autocomplete.buildVocabularyFromScrapedDataFile("src/main/resources/scraped_data.csv");
        autocomplete.buildVocabularyFromExcelFile("src/main/resources/ScrapedData.xlsx");
        autocomplete.buildVocabularyFromZoloWindsorListingsFile("src/main/resources/zolo_windsor_listings.csv");

        // Prompt the user to enter a prefix for word completions
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a prefix to get word completions:");
        String prefix = scanner.nextLine();

        // Get and print suggestions based on the prefix
        List<String> suggestions = autocomplete.getSuggestions(prefix);
        System.out.println("Suggestions:");
        for (String suggestion : suggestions) {
            System.out.println(suggestion);
        }
    }
}
