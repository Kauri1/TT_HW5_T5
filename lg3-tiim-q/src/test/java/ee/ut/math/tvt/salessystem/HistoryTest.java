package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.HistoryEntry;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

public class HistoryTest {
    private InMemorySalesSystemDAO dao = spy(new InMemorySalesSystemDAO());
    private SoldItem item1;
    private SoldItem item2;


    public void setUp(){
        dao.beginTransaction();

        item1 = new SoldItem(dao.findStockItem(1), 3, LocalDateTime.of(2024, 11, 1, 10, 52, 15));
        item2 = new SoldItem(dao.findStockItem(2), 1, LocalDateTime.of(2024, 11, 1, 10, 52, 15));
        SoldItem item3 = new SoldItem(dao.findStockItem(3), 8, LocalDateTime.of(2024, 11, 1, 1, 2, 5));
        SoldItem item4 = new SoldItem(dao.findStockItem(4), 50, LocalDateTime.of(2024, 10, 31, 10, 52, 15));

        dao.saveHistoryEntry(new HistoryEntry(item1.getDateTimeSold(), Arrays.asList(item1, item2)));
        dao.saveSoldItem(item1, dao.getLastHistoryEntryId());
        dao.saveSoldItem(item2, dao.getLastHistoryEntryId());
        dao.saveHistoryEntry(new HistoryEntry(item3.getDateTimeSold(), List.of(item3)));
        dao.saveSoldItem(item3, dao.getLastHistoryEntryId());
        dao.saveHistoryEntry(new HistoryEntry(item4.getDateTimeSold(), List.of(item4)));
        dao.saveSoldItem(item4, dao.getLastHistoryEntryId());

        dao.commitTransaction();
    }

    @Test
    public void testRetrievingAllEntries(){
        setUp();
        List<HistoryEntry> entries = dao.getNentries(-1);

        // asserting the entries are properly grouped up
        assertEquals(3, entries.size());
        // asserting they are sorted the correct way
        assertEquals(entries.getFirst().toString(), new HistoryEntry(LocalDateTime.of(2024, 11, 1, 10, 52, 15), List.of(item1,item2)).toString());
    }

    @Test
    public void testRetrievingLast10WithLesserAmount() {
        setUp();
        List<HistoryEntry> entries = dao.getNentries(10);
        assertEquals(3, entries.size());
    }

    @Test
    public void testRetrievingLast10WithEmptyHistory() {
        List<HistoryEntry> entries = dao.getNentries(10);

        assertNotNull(entries);
        System.out.println(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    public void testRetrievingLast10EntriesWithEnoughToRetrieve() {
        setUp();
        // adding a bunch of test items so that there is more than 10 entries
        dao.beginTransaction();
        for (int i = 1; i < 9; i++) {
            LocalDateTime time = LocalDateTime.of(2024, 10, i, 10, 52, 15);
            SoldItem item = new SoldItem(dao.findStockItem(1), i, time);
            dao.saveHistoryEntry(new HistoryEntry(time, List.of(item)));
            dao.saveSoldItem(item, dao.getLastHistoryEntryId());
        }
        dao.commitTransaction();
        List<HistoryEntry> entries = dao.getNentries(10);
        assertEquals(10, entries.size());

        // assert that the last entry returned is the 2nd to last entry in the history (because it contains 11 entries, so only one is left unreturned)
        SoldItem secondDummy = new SoldItem(dao.findStockItem(1), 2, LocalDateTime.of(2024, 10, 2, 10, 52, 15));
        assertEquals(new HistoryEntry(secondDummy.getDateTimeSold(), List.of(secondDummy)).toString(), entries.getLast().toString());
    }


    @Test
    public void testBetweenDatesExpectedWorkflow() {
        setUp();
        LocalDate start = LocalDate.of(2024,10,1);
        LocalDate end = LocalDate.of(2024,11,1);

        List<HistoryEntry> entries = dao.getEntriesByDate(start, end);

        assertNotNull(entries);
        assertEquals(3, entries.size());
        assertTrue(entries.getFirst().getDateSold().isBefore(end) || entries.getFirst().getDateSold().isEqual(end));
        assertTrue(entries.getLast().getDateSold().isAfter(start) || entries.getLast().getDateSold().isEqual(start));
    }

    @Test
    public void testBetweenDatesWithEqualDates() {
        setUp();
        LocalDate start = LocalDate.of(2024,11,1);
        LocalDate end = LocalDate.of(2024,11,1);

        List<HistoryEntry> entries = dao.getEntriesByDate(start, end);

        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(entries.size(), 2);

        assertEquals(entries.getFirst().getDateSold(),start);
        assertEquals(entries.getLast().getDateSold(),start);
    }

    @Test
    public void testBetweenDatesWithEndBeforeStart() {
        setUp();
        LocalDate start = LocalDate.of(2024,11,1);
        LocalDate end = LocalDate.of(2024,10,1);

        List<HistoryEntry> entries = dao.getEntriesByDate(start, end);

        assertTrue(entries.isEmpty());
    }
}
