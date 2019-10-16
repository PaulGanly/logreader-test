package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.LogEntry;
import models.db.EventDetail;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LogReaderService {

    static Logger logger = Logger.getLogger(LogReaderService.class.getName());

    public static final Integer LONG_RUNNING_PROCESS_LIMIT = 4;
    AtomicInteger startIndex = new AtomicInteger(0);

    /**
     * A method that reads the file at the path provided and for each pair of events calculates the time taken
     * and stores the details to a database.
     *
     * @param logFilePath
     * @param batchSize
     * @param threadNumber
     */
    public void readLogFileAndStoreDetailsToDb(String logFilePath, Integer batchSize, Integer threadNumber) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber);
        List<Future<List<LogEntry>>> startedProcessingLogEntriesFutures = new ArrayList<>();
        List<LogEntry> unmatchedEntries = new ArrayList<>();
        Long totalLines = getTotalNumberOfLinesInLog(logFilePath);

        for (int i = 0; i < totalLines + batchSize; i += batchSize) {
            List<LogEntry> logEntries = selectBatchOfLines(logFilePath, batchSize);
            Callable<List<LogEntry>> callableTask = () ->  processBatchOfLines(logEntries);
            startedProcessingLogEntriesFutures.add(executor.submit(callableTask));
        }

        collateUnmatchedEntriesInEachBatch(startedProcessingLogEntriesFutures, unmatchedEntries);
        processBatchOfLines(unmatchedEntries.stream().collect(Collectors.toList()));
    }

    /**
     * Calculates the total number of lines in the log file
     *
     * @param logFilePath
     * @return
     */
    private Long getTotalNumberOfLinesInLog(String logFilePath) {
        Long totalLines = 0L;
        try{
            totalLines = Files.lines(Paths.get(logFilePath), StandardCharsets.UTF_8).count();
        }catch(Exception e){
            logger.error("Error reading number of lines in file: " + logFilePath + " error is: " + e.getMessage());
        }
        return totalLines;
    }

    /**
     * Each of the separate batches will return a list of log lines that have no corresponding start or end line. Each of these unmatched events are
     * added to a list and processed when all the previous batches have been completed.
     *
     * @param startedProcessingLogEntriesFutures
     * @param unmatchedEntries
     */
    private void collateUnmatchedEntriesInEachBatch(List<Future<List<LogEntry>>> startedProcessingLogEntriesFutures, List<LogEntry> unmatchedEntries) {
        waitForAllFuturesToComplete(startedProcessingLogEntriesFutures);
        for(Future<List<LogEntry>> future: startedProcessingLogEntriesFutures){
            try{
                unmatchedEntries.addAll(future.get());
            }catch (Exception e){
                logger.error("Error adding futures: " + e.getMessage());
            }
        }
    }

    /**
     * Wait of all the seperate threads to complete processing their log entries and return unmatched entries
     *
     * @param startedProcessingLogEntriesFutures
     */
    private void waitForAllFuturesToComplete(List<Future<List<LogEntry>>> startedProcessingLogEntriesFutures) {
        while (futuresStillActive(startedProcessingLogEntriesFutures)) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                logger.error("Waiting for futures to finish: " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether the futures are still active
     *
     * @param futures
     * @return
     */
    private Boolean futuresStillActive(List<Future<List<LogEntry>>> futures) {
        for (Future<List<LogEntry>> future : futures) {
            if (!future.isDone()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sorts the current list of Log Entries in order of their id and state. After this we process the entries in order.
     * If a starting event has a corresponding ending event, we generate the Event tdtail and store it to the DB, otherwise it
     * is added to the unmatched events and returned.
     *
     * @param entries
     * @return unmatched events
     */
    private List<LogEntry> processBatchOfLines(List<LogEntry> entries) {
        Collections.sort(entries);
        List<LogEntry> unmatchedEntries = new ArrayList<>();
        List<EventDetail> events = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            LogEntry currentEntry = entries.get(i);
            if(i < entries.size() - 1){
                LogEntry nextEntry = entries.get(i+1);
                if(currentEntry.getId().equals(nextEntry.getId())){
                    Boolean isLongRunningProcess = nextEntry.getTimestamp() - currentEntry.getTimestamp() > LONG_RUNNING_PROCESS_LIMIT ? true : false;
                    events.add(new EventDetail(currentEntry.getId(), currentEntry.getType(), currentEntry.getHost(), isLongRunningProcess));
                    i++;
                }else{
                    unmatchedEntries.add(entries.get(i));
                }
            }else{
                unmatchedEntries.add(entries.get(i));
            }
        }
        saveEventsToDb(events);
        return unmatchedEntries;
    }

    /**
     * Save the list of events to the DB
     *
     * @param events
     */
    private void saveEventsToDb(List<EventDetail> events) {
        logger.debug("Saving events: " + events);
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        for (EventDetail event: events) {
            session.save(event);
        }
        tx.commit();
        session.close();
    }

    /**
     * Read the next batch of lines from the log file and converts it into a list of log entry objects.
     *
     * @param filePath
     * @param batchSize
     * @return a list of log entries
     */
    public List<LogEntry> selectBatchOfLines(String filePath, Integer batchSize){
        int start = startIndex.getAndAdd(batchSize);
        List<LogEntry> logEntries = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            List<String> lines = reader.lines().skip(start).limit(batchSize).collect(Collectors.toList());
            for (String line: lines) {
                try{
                    logEntries.add(new ObjectMapper().readerFor(LogEntry.class).readValue(line));
                } catch (Exception e){
                    logger.error("Mapping line: " + line + " to Log Entry Object " + e.getMessage());
                }
            }
        }catch (Exception e){
            logger.error("Error reading file: " + filePath + " error is: " + e.getMessage());
        }
        return logEntries;
    }

}
