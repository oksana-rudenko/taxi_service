package taxi.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import taxi.exception.AuthenticationException;
import taxi.lib.Injector;
import taxi.model.Driver;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceImplTest {
    private static final Injector injector = Injector.getInstance("taxi");
    private final AuthenticationService authenticationService = (AuthenticationService) injector
            .getInstance(AuthenticationService.class);
    private static DriverService driverService =
            (DriverService) injector.getInstance(DriverService.class);
    private static Driver den;
    private static Driver denDriver;

    @BeforeAll
    static void beforeAll() {
        den = new Driver("Denis", "0013", "den", "1300");
        denDriver = driverService.create(den);
    }
    
    @Test
    void login_correctDriverData_ok() {
        String login = "den";
        String password = "1300";
        Driver driver = new Driver();
        try {
            driver = authenticationService.login(login, password);
        } catch (AuthenticationException e) {
            fail("Driver must be authenticate");
        }
        assertEquals(denDriver.getId(), driver.getId());
    }

    @Test
    void login_wrongLogin_notOk() {
        String login = "DEN";
        String password = "0013";
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login(login, password);
        });
    }

    @Test
    void login_wrongPassword_notOk() {
        String login = "den";
        String password = "0000";
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login(login, password);
        });
    }

    @Test
    void login_nullLogin_notOk() {
        String login = null;
        String password = "1234";
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login(login, password);
        });
    }

    @Test
    void login_nullPassword_notOk() {
        String login = "den";
        String password = null;
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login(login, password);
        });
    }
}