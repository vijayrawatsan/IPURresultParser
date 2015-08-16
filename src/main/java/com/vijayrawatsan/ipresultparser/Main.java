package com.vijayrawatsan.ipresultparser;

import au.com.bytecode.opencsv.CSVWriter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vijay.rawat01 on 7/29/15.
 */
public class Main {

    public static String INPUT_PATH = null;
    public static String OUTPUT_DIRECTORY_PATH = null;
    public static final HashSet<String> ALREADY_CREATED = new HashSet<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new RuntimeException("Input or output not specified.");
        }
        INPUT_PATH = args[0];
        if (!new File(INPUT_PATH).exists()) {
            throw new RuntimeException("Input file does not exists.");
        }
        OUTPUT_DIRECTORY_PATH = args[1];
        createOutputDirectory();
        PdfReader pdfReader = new PdfReader(INPUT_PATH);
        PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
        Triplet previousInstituteAndSemesterInfo = null;
        Map<String, Triplet> subjectIdToColumnsMap = null;
        CSVWriter writer = null;
        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
            TextExtractionStrategy strategy = pdfReaderContentParser.processContent(i, new CustomSimpleTextExtractionStrategy());
            String page = strategy.getResultantText();
            if (page.contains("SCHEME OF EXAMINATIONS")) {
                Triplet currentInstituteAndSemesterInfo = getInstituteAndSemesterInfo(page);
                if (previousInstituteAndSemesterInfo == null || !previousInstituteAndSemesterInfo.equals(currentInstituteAndSemesterInfo)) {
                    subjectIdToColumnsMap = getSubjectsMap(page);
                    previousInstituteAndSemesterInfo = currentInstituteAndSemesterInfo;
                    if (writer != null) {
                        writer.close();
                    }
                    String fileName = OUTPUT_DIRECTORY_PATH + currentInstituteAndSemesterInfo.concatenated() + ".csv";
                    writer = new CSVWriter(new FileWriter(fileName, true), ',', '\0');
                    if (!ALREADY_CREATED.contains(fileName)) {
                        System.out.println(fileName);
                        writeHead(writer, subjectIdToColumnsMap);
                        ALREADY_CREATED.add(fileName);
                    }
                }
            } else {
                writeStudentMarksForPage(writer, subjectIdToColumnsMap, previousInstituteAndSemesterInfo, page);
            }
        }
        if (writer != null) {
            writer.close();
        }
    }

    private static void createOutputDirectory() {
        if (!OUTPUT_DIRECTORY_PATH.endsWith("/")) {
            OUTPUT_DIRECTORY_PATH = OUTPUT_DIRECTORY_PATH + "/";
        }
        File file = new File(OUTPUT_DIRECTORY_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static Triplet getInstituteAndSemesterInfo(String page) {
        Triplet triplet = new Triplet("", "", "");
        Scanner scanner = new Scanner(page);
        while (scanner.hasNext()) {
            String next = scanner.nextLine();
            if (next.startsWith("Institution")) {
                Pattern pattern = Pattern.compile("Institution Code:([\\s0-9]+)Institution:([\\sA-Z,]+)");
                Matcher matcher = pattern.matcher(next);
                matcher.find();
                triplet.first = matcher.group(1).trim();
                triplet.second = matcher.group(2).trim().replaceAll(",", "").replaceAll(" +", " ");
            }
            if (next.contains("SEMESTER") || next.contains("ANNUAL")) {
                Pattern pattern = Pattern.compile(".*:([\\s0-9]+)[SEMESTER|ANNUAL]");
                Matcher matcher = pattern.matcher(next);
                matcher.find();
                String group = matcher.group(1);
                String semester = group.trim();
                triplet.third = semester;
            }
        }
        return triplet;
    }

    private static void writeHead(CSVWriter writer, Map<String, Triplet> subjectIdToColumnsMap) {
        StringBuilder builder = new StringBuilder();
        builder.append("Institute,").append("name,").append("semester");
        for (String key : subjectIdToColumnsMap.keySet()) {
            Triplet triplet = subjectIdToColumnsMap.get(key);
            builder.append(",").append(triplet.first.trim().replaceAll(" +", " ").replaceAll(" ", "_"));
            builder.append(",").append(triplet.second.trim().replaceAll(" +", " ").replaceAll(" ", "_"));
            builder.append(",").append(triplet.third.trim().replaceAll(" +", " ").replaceAll(" ", "_"));
        }
        writer.writeNext(builder.toString().split(","));
    }

    private static void writeStudentMarksForPage(CSVWriter writer, Map<String, Triplet> subjectIdToColumnsMap, Triplet instituteInfo, String resultantText) {
        Scanner scanner = new Scanner(resultantText);
        String previousLine = "";
        String name = "";
        String previousStudentData = "";
        boolean firstSIDFound = false;
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("SID:")) {
                firstSIDFound = true;
                if (!previousStudentData.isEmpty()) {
                    parseAndWritePreviousStudentData(name, previousStudentData, writer, subjectIdToColumnsMap, instituteInfo);
                    previousStudentData = "";
                }
                name = previousLine;
                name = name + ":" + line.split(":")[1].trim();
            }
            if (firstSIDFound) {
                previousStudentData = previousStudentData + "," + line;
            }
            previousLine = line;
        }
        parseAndWritePreviousStudentData(name, previousStudentData, writer, subjectIdToColumnsMap, instituteInfo);
    }

    private static void parseAndWritePreviousStudentData(String name, String previousStudentData, CSVWriter writer, Map<String, Triplet> subjectIdToColumnsMap, Triplet instituteInfo) {
        StringBuilder builder = new StringBuilder("");
        builder.append(instituteInfo.second + ",").append(name + ",").append(instituteInfo.third);
        for (String key : subjectIdToColumnsMap.keySet()) {
            Triplet triplet = getMarksTriplet(key, previousStudentData);
            builder.append(",").append(triplet.first.trim());
            builder.append(",").append(triplet.second.trim());
            builder.append(",").append(triplet.third.trim());
        }
        writer.writeNext(builder.toString().split(","));
    }

    private static Triplet getMarksTriplet(String subjectCode, String marksLine) {
        Triplet result = new Triplet("NA", "NA", "NA");
        Pattern pattern = Pattern.compile(".*" + subjectCode + "\\([0-9]+\\),([\\s0-9A-Z\\*]+,[\\s0-9A-Z\\*]+),.*");
        Matcher matcher = pattern.matcher(marksLine);

        while (matcher.find()) {
            String group = matcher.group(1);
            String[] split = group.split(",");
            String total = split[1];
            if (split[0].matches("[A-Z][A-Z]+")) {
                if (split[0].length() >= 3) {
                    result.first = split[0].substring(0, 2);
                    result.second = split[0].substring(2, split[0].length());
                } else {
                    result.first = split[0].substring(0, 1);
                    result.second = split[0].substring(1, 2);
                }
            } else {
                String[] pair = split[0].trim().replaceAll(" +", " ").split(" ");
                result.first = pair[0];
                result.second = pair[1];
            }
            result.third = total;
        }
        return result;
    }

    private static Map<String, Triplet> getSubjectsMap(String page) throws IOException {
        Map<String, Triplet> result = new LinkedHashMap<>();
        Scanner scanner = new Scanner(page);
        while (scanner.hasNext()) {
            String next = scanner.nextLine();
            if (next.startsWith("0")) {
                String[] split = next.split("___");
                String subjectId = split[1];
                String subject = split[3];
                String[] subjectContainingArray = subject.split("\\d");
                String suffix1 = "_minor";
                String suffix2 = "_major";
                String suffix3 = "_total";
                result.put(subjectId.trim().toLowerCase(),
                        new Triplet(
                                subjectContainingArray[0].trim().toLowerCase() + suffix1,
                                subjectContainingArray[0].trim().toLowerCase() + suffix2,
                                subjectContainingArray[0].trim().toLowerCase() + suffix3));
            }
        }
        return result;
    }
}
