package helpers;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVFile {
    static File file;
    static FileWriter output;
    static CSVWriter writer;

    public static void createCSVFile(String agentName){
        // create the csv file to store the coordinates
        String filename = String.format("temp\\DualAgentCoordinates_%s.csv", agentName);
        file = new File(filename);
        if(file.exists() && !file.isDirectory()) {
            System.out.println("CSV File Is Already Exists");
            try {
                output = new FileWriter(filename, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(output);
        }
        else{
            System.out.println("Creating CSV File");
            try {
                output = new FileWriter(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(output);
            String[] header = {"Timestamp", "X1", "Y1"};
            writer.writeNext(header);
        }
    }

    public static void updateCSV(String[] data) throws IOException {
        // write the coordinates x and y to the csv file
//        System.out.println("Writing to CSV File");
        try {
            writer.writeNext(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void closeCSVFile() throws IOException {
        System.out.println("Closing CSV File");
        //close the cvs file writer
        writer.close();
    }

}
