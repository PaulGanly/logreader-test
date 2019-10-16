package services;

import models.LogEntry;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class LogReaderServiceTest {

    @Test
    public void testReadingLogFile(){
        LogReaderService logReaderService = new LogReaderService();
        File file = new File(getClass().getClassLoader().getResource("testlog.txt").getFile());
        List<LogEntry> entries = logReaderService.selectBatchOfLines(file.getPath(), 10);
        System.out.println(entries);
    }

    @Test
    public void testProcessingFile(){
        LogReaderService logReaderService = new LogReaderService();
        File file = new File(getClass().getClassLoader().getResource("testlog.txt").getFile());
        logReaderService.readLogFileAndStoreDetailsToDb(file.getPath(), 2, 4);
        System.out.println("Hello");
    }
}
