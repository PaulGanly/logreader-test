package com.test.services;

import com.test.models.LogEntry;
import com.test.models.db.EventDetail;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import com.test.util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.File;
import java.util.List;

public class LogReaderServiceTest {

    @Test
    public void testReadingLogFileMoreThanAvailable(){
        LogReaderService logReaderService = new LogReaderService();
        File file = new File(getClass().getClassLoader().getResource("testlog.txt").getFile());
        List<LogEntry> entries = logReaderService.selectBatchOfLines(file.getPath(), 10);
        Assert.assertTrue(entries.size() == 6);
    }

    @Test
    public void testReadingLogFileLessThanAvailable(){
        LogReaderService logReaderService = new LogReaderService();
        File file = new File(getClass().getClassLoader().getResource("testlog.txt").getFile());
        List<LogEntry> entries = logReaderService.selectBatchOfLines(file.getPath(), 3);
        Assert.assertTrue(entries.size() == 3);
    }

    @Test
    public void testProcessingFileCreatesCorrectNumberOfEntriesAndAlertFlagSet(){
        LogReaderService logReaderService = new LogReaderService();
        File file = new File(getClass().getClassLoader().getResource("testlog.txt").getFile());
        logReaderService.readLogFileAndStoreDetailsToDb(file.getPath(), 2, 4);
        Session session = HibernateUtil.getSessionFactory().openSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<EventDetail> criteria = builder.createQuery(EventDetail.class);
        criteria.from(EventDetail.class);
        List<EventDetail> data = session.createQuery(criteria).getResultList();
        Assert.assertTrue(data.size() == 3);
        for (EventDetail detail: data) {
            if("scsmbstgra".equals(detail.getEventId()) || "scsmbstgrc".equals(detail.getEventId())){
                Assert.assertTrue(detail.getAlert());
            } else {
                Assert.assertFalse(detail.getAlert());
            }
        }
    }

}
