package taxi.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import taxi.exception.DataProcessingException;
import taxi.exception.LoginDuplicationException;
import taxi.lib.Injector;
import taxi.model.Driver;

class DriverServiceImplTest {
    private static final Injector injector = Injector.getInstance("taxi");
    private static final DriverService driverService =
            (DriverService) injector.getInstance(DriverService.class);

    @Test
    void create_newDriver_ok() {
        Driver expected = new Driver("Ann", "0022", "ann", "2200");
        Driver actual = driverService.create(expected);
        Long annId = actual.getId();
        expected.setId(annId);
        assertEquals(expected, actual);
    }

    @Test
    void create_emptyDriver_notOk() {
        Driver driver = new Driver();
        assertThrows(DataProcessingException.class, () -> {
            driverService.create(driver);
        });
    }

    @Test
    void create_driverWithExistingLogin_notOk() {
        Driver kate = new Driver("Kate", "0002", "kate", "2000");
        driverService.create(kate);
        Driver kateDubl = new Driver("Kateryna", "0018", "kate", "1800");
        assertThrows(LoginDuplicationException.class, () -> {
            driverService.create(kateDubl);
        });
    }

    @Test
    void create_driverNullName_notOk() {
        Driver driver = new Driver();
        driver.setLicenseNumber("0007");
        driver.setLogin("karl");
        driver.setPassword("7000");
        assertThrows(DataProcessingException.class,() -> {
            driverService.create(driver);
        });
    }

    @Test
    void create_driverNullLicenseNumber_notOk() {
        Driver stev = new Driver();
        stev.setName("Steven");
        stev.setLogin("stev");
        stev.setPassword("5769");
        assertThrows(DataProcessingException.class, () -> {
            driverService.create(stev);
        });
    }

    @Test
    void create_driverNullLogin_notOk() {
        Driver colin = new Driver();
        colin.setName("Colin");
        colin.setLicenseNumber("0080");
        colin.setPassword("0800");
        assertThrows(DataProcessingException.class, () -> {
            driverService.create(colin);
        });
    }

    @Test
    void create_driverNullPassword_notOk() {
        Driver mary = new Driver();
        mary.setName("Mary");
        mary.setLicenseNumber("0011");
        mary.setLogin("maria");
        assertThrows(DataProcessingException.class, () -> {
            driverService.create(mary);
        });
    }

    @Test
    void get_driverById_ok() {
        Driver kamila = new Driver("Kamila", "0002", "kamila", "2000");
        Driver expectedDriver = driverService.create(kamila);
        Long driverId = expectedDriver.getId();
        Driver actualDriver = driverService.get(driverId);
        assertEquals(expectedDriver, actualDriver);
    }

    @Test
    void get_driverByNotExistingId_notOk() {
        Long id = -1L;
        assertThrows(NoSuchElementException.class, () -> {
            driverService.get(id);
        });
    }

    @Test
    void get_deletedDriver_notOk() {
        Driver alex = new Driver("Alexandr", "0008", "alex", "8000");
        Driver alexDriver = driverService.create(alex);
        Long id = alexDriver.getId();
        driverService.delete(id);
        assertThrows(NoSuchElementException.class, () -> {
            driverService.get(id);
        });
    }

    @Test
    void getAll_allDrivers_ok() {
        Driver lisa = new Driver("Elisabeth", "0009", "lisa", "9000");
        Driver john = new Driver("John", "0010", "john", "0100");
        Driver lisaDriver = driverService.create(lisa);
        Driver johnDriver = driverService.create(john);
        List<Driver> allDrivers = driverService.getAll();
        assertTrue(allDrivers.contains(lisaDriver) && allDrivers.contains(johnDriver));
    }

    @Test
    void getAll_allDriversDeleted_ok() {
        List<Driver> drivers = driverService.getAll();
        drivers.forEach(d -> driverService.delete(d.getId()));
        List<Driver> expected = new ArrayList<>();
        List<Driver> actual = driverService.getAll();
        assertEquals(expected, actual);
    }

    @Test
    void update_driver_ok() {
        Driver will = new Driver("William", "0110", "will", "0110");
        Driver willDriver = driverService.create(will);
        Long id = willDriver.getId();
        Driver expected = new Driver("William", "0003", "will", "3000");
        expected.setId(id);
        Driver actual = driverService.update(expected);
        assertEquals(expected, actual);
    }

    @Test
    void update_driverIdNotExisting_notOk() {
        Driver pam = new Driver();
        pam.setId(-1L);
        pam.setName("Pamela");
        pam.setLicenseNumber("0032");
        pam.setLogin("pam");
        pam.setPassword("2300");
        assertThrows(NoSuchElementException.class, () -> {
            driverService.update(pam);
        });
    }

    @Test
    void update_driverNullName_notOk() {
        Driver mike = new Driver("Mike", "0042", "mike", "2400");
        Driver mikeDriver = driverService.create(mike);
        Long id = mikeDriver.getId();
        Driver mikeUpdate = new Driver(null, "0044", "mike", "4400");
        mikeUpdate.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            driverService.update(mikeUpdate);
        });
    }

    @Test
    void update_driverNullLicenseNumber_notOk() {
        Driver bill = new Driver("William", "0076", "bill", "6700");
        Driver billDriver = driverService.create(bill);
        Long id = billDriver.getId();
        Driver billUpdate = new Driver("Billy", null, "bill", "6500");
        billUpdate.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            driverService.update(billUpdate);
        });
    }

    @Test
    void update_driverNullLogin_notOk() {
        Driver nick = new Driver("Nick", "0018", "nick", "8100");
        Driver nickDriver = driverService.create(nick);
        Long id = nickDriver.getId();
        Driver nickUpdate = new Driver("Nicky", "0033", null, "3300");
        nickUpdate.setId(id);
        assertThrows(LoginDuplicationException.class, () -> {
            driverService.update(nickUpdate);
        });
    }

    @Test
    void update_driverNullPassword_notOk() {
        Driver david = new Driver("David", "0015", "david", "5100");
        Driver davidDriver = driverService.create(david);
        Long id = davidDriver.getId();
        Driver davidUpdate = new Driver("David", "0071", "david", null);
        davidUpdate.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            driverService.update(davidUpdate);
        });
    }

    @Test
    void update_changeLogin_notOk() {
        Driver dwight = new Driver("Dwight", "0038", "dwight", "8300");
        Driver dwightDriver = driverService.create(dwight);
        Long id = dwightDriver.getId();
        Driver dwightUpdated = new Driver("Dwight", "0036", "wight", "6300");
        dwightUpdated.setId(id);
        assertThrows(LoginDuplicationException.class, () -> {
            driverService.update(dwightUpdated);
        });
    }

    @Test
    void delete_driver_ok() {
        Driver tony = new Driver("Antony", "0017", "tony", "7100");
        Driver tonyDriver = driverService.create(tony);
        Long id = tonyDriver.getId();
        boolean isDeleted = driverService.delete(id);
        assertTrue(isDeleted);
    }

    @Test
    void delete_driverIdNotExisting_notOk() {
        Long id = -1L;
        boolean isDeleted = driverService.delete(id);
        assertFalse(isDeleted);
    }

    @Test
    void delete_driverIsDeleted_ok() {
        Driver andy = new Driver("Andrew", "0019", "andy", "9100");
        Driver andyDriver = driverService.create(andy);
        Long id = andyDriver.getId();
        driverService.delete(id);
        boolean isDeleted = driverService.delete(id);
        assertTrue(isDeleted);
    }

    @Test
    void findByLogin_driver_ok() {
        Driver rich = new Driver("Richard", "0029", "rich", "9200");
        Driver expected = driverService.create(rich);
        String login = expected.getLogin();
        Driver actual = driverService.findByLogin(login).get();
        assertEquals(expected, actual);
    }

    @Test
    void findByLogin_deletedDriver_ok() {
        Driver ross = new Driver("Ross", "0039", "ross", "9300");
        Driver rossDriver = driverService.create(ross);
        Long id = rossDriver.getId();
        String login = rossDriver.getLogin();
        driverService.delete(id);
        assertTrue(driverService.findByLogin(login).isEmpty());
    }

    @Test
    void findByLogin_notExistingDriver_ok() {
        String login = "oks";
        assertTrue(driverService.findByLogin(login).isEmpty());
    }

    @Test
    void findByLogin_nullLogin_ok() {
        assertTrue(driverService.findByLogin(null).isEmpty());
    }
}