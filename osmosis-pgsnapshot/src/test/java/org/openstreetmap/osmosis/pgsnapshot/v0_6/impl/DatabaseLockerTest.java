// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import static org.junit.Assert.fail;

import javax.sql.DataSource;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstreetmap.osmosis.core.database.DatabaseLocker;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Tests the basic functionality around the {@link DatabaseLocker}.
 *
 * @author mcuthbert
 */
@FixMethodOrder(MethodSorters.JVM)
public class DatabaseLockerTest {

    /**
     * Simple test to make sure that we can obtain a write lock and then release it.
     */
    @Test
    public void writeLockTest() {
        try (DatabaseLocker locker = new DatabaseLocker(this.dataSource(), true)) {
            locker.lockDatabase("WriteLock", "WRITE_TEST_LOCK");
        } catch (final Exception e) {
            fail("Write lock should not have thrown an exception");
        }
    }

    /**
     * Simple test to make sure that we can obtain a read lock and then release it.
     */
    @Test
    public void readLockTest() {
        try (DatabaseLocker locker = new DatabaseLocker(this.dataSource(), false)) {
            locker.lockDatabase("ReadLock", "READ_LOCK_TEST");
        } catch (final Exception e) {
            fail("Read lock should not have thrown an exception");
        }
    }

    /**
     * Test to show that if we try to obtain a write lock when there is a read lock that we will
     * get an exception.
     *
     * @throws Exception if the datasource cannot be established
     */
    @Test
    public void writeWithReadLockTest() throws Exception {
        final DatabaseLocker writeLocker = new DatabaseLocker(this.dataSource(), true);
        final DatabaseLocker readLocker = new DatabaseLocker(this.dataSource(), false);
        readLocker.lockDatabase("ReadLock", "WRITE_WITH_READ_LOCK_TEST_1");
        try {
            writeLocker.lockDatabase("WriteLock", "WRITE_WITH_READ_LOCK_TEST_2");
            fail("Write lock should have thrown an exception");
        } catch (final Exception e) {
            // we should expect an exception to be thrown
        }
        readLocker.unlockDatabase();
        writeLocker.lockDatabase("WriteLock", "WRITE_WITH_READ_LOCK_TEST_3");
        writeLocker.unlockDatabase();
    }

    /**
     * Test to show that if we try to obtain a write lock when there is already a write lock that we
     * will get an exception.
     *
     * @throws Exception if the datasource cannot be established
     */
    @Test
    public void writeWithWriteLockTest() throws Exception {
        final DatabaseLocker writeLocker1 = new DatabaseLocker(this.dataSource(), true);
        final DatabaseLocker writeLocker2 = new DatabaseLocker(this.dataSource(), true);
        writeLocker1.lockDatabase("WriteLock1", "WRITE_WITH_WRITE_LOCK_TEST_1");
        try {
            writeLocker2.lockDatabase("WriteLock2", "WRITE_WITH_WRITE_LOCK_TEST_2");
            fail("Write lock should have thrown an exception");
        } catch (final Exception e) {
            // we should expect an exception to be thrown
        }
        writeLocker1.unlockDatabase();
        writeLocker2.lockDatabase("WriteLock2", "WRITE_WITH_WRITE_LOCK_TEST_3");
        writeLocker2.unlockDatabase();
    }

    /**
     * Test to show that we can obtain multiple read locks.
     *
     * @throws Exception if the datasource cannot be established
     */
    @Test
    public void readWithReadLockTest() throws Exception {
        final DatabaseLocker[] lockers = new DatabaseLocker[10];
        try {
            for (int i = 0; i < 10; i++) {
                lockers[i] = new DatabaseLocker(this.dataSource(), false);
                lockers[i].lockDatabase("ReadLock" + i, "READ_WITH_READ_LOCK_TEST_" + i);
            }
        } finally {
            // unlock all the databases
            for (int i = 0; i < 10; i++) {
                if (lockers[i] != null) {
                    lockers[i].unlockDatabase();
                }
            }
        }
    }

    /**
     * Test to show that if we try to obtain a read lock when there is already a write lock that we
     * will get an exception.
     *
     * @throws Exception if the datasource cannot be established
     */
    @Test
    public void readWithWriteLockTest() throws Exception {
        final DatabaseLocker readLocker = new DatabaseLocker(this.dataSource(), false);
        final DatabaseLocker writeLocker = new DatabaseLocker(this.dataSource(), true);
        writeLocker.lockDatabase("WriteLock", "READ_WITH_WRITE_LOCK_TEST_1");
        try {
            readLocker.lockDatabase("ReadLock", "READ_WITH_WRITE_LOCK_TEST_2");
            fail("Read lock should have thrown an exception");
        } catch (final Exception e) {
            // we should expect an exception to be thrown
        }
        writeLocker.unlockDatabase();
        readLocker.lockDatabase("ReadLock", "READ_WITH_WRITE_LOCK_TEST_3");
        readLocker.unlockDatabase();
    }

    private DataSource dataSource() throws Exception {
        Class.forName("org.postgresql.Driver");
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://db:5432/pgosmsnap06_test");
        dataSource.setUsername("osm");
        dataSource.setPassword("password");
        dataSource.setSchema("public");
        return dataSource;
    }
}
