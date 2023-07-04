package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.exception.LoginDuplicationException;
import taxi.lib.Dao;
import taxi.model.Driver;
import taxi.util.ConnectionUtil;

@Dao
public class DriverDaoImpl implements DriverDao {
    private static final Logger logger = LogManager.getLogger(DriverDaoImpl.class);

    @Override
    public Driver create(Driver driver) {
        logger.debug("Method create was called. Params: name={}, license number={}, login={}",
                driver.getName(), driver.getLicenseNumber(), driver.getLogin());
        String login = driver.getLogin();
        if (checkLoginIsUnique(login)) {
            logger.error("Login duplication error. Params: login={}", driver.getLogin());
            throw new LoginDuplicationException("Driver with the same login already exists. "
                    + "Please, enter another login");
        }
        String query = "INSERT INTO drivers (name, license_number, login, password) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenseNumber());
            statement.setString(3, driver.getLogin());
            statement.setString(4, driver.getPassword());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                driver.setId(resultSet.getObject(1, Long.class));
            }
            return driver;
        } catch (SQLException e) {
            logger.error("Can't create a driver. Params: name={}, license number={}, login={}",
                    driver.getName(), driver.getLicenseNumber(), driver.getLogin(), e);
            throw new DataProcessingException("Can't create a driver " + driver, e);
        }
    }

    public boolean checkLoginIsUnique(String login) {
        logger.debug("Method checkLoginIsUnique was called. Params: login={}", login);
        String query = "SELECT login FROM drivers WHERE is_deleted = FALSE";
        List<String> logins = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                logins.add(resultSet.getString("login"));
            }
        } catch (SQLException e) {
            logger.error("Can't get any login", e);
            throw new DataProcessingException("Can't get any login", e);
        }
        return logins.contains(login);
    }

    @Override
    public Optional<Driver> get(Long id) {
        logger.debug("Method get a driver was called. Params: driver Id={}", id);
        String query = "SELECT * FROM drivers WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error("Can't get a driver. Params: id={}", id, e);
            throw new DataProcessingException("Can't get a driver by id " + id, e);
        }
    }

    @Override
    public List<Driver> getAll() {
        logger.debug("Method getAll drivers was called");
        String query = "SELECT * FROM drivers WHERE is_deleted = FALSE";
        List<Driver> drivers = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            return drivers;
        } catch (SQLException e) {
            logger.error("Can't get a list of drivers", e);
            throw new DataProcessingException("Can't get a list of drivers.", e);
        }
    }

    @Override
    public Driver update(Driver driver) {
        logger.debug("Method update a driver was called. Params: driver Id={}", driver.getId());
        Driver driverDataFromDB = get(driver.getId()).orElseThrow(() ->
                new NoSuchElementException("Can't find a driver by id " + driver.getId()));
        if (!driverDataFromDB.getLogin().equals(driver.getLogin())) {
            throw new LoginDuplicationException("You can't change your login!");
        }
        String query = "UPDATE drivers "
                + "SET name = ?, license_number = ?, login = ?, password = ? "
                + "WHERE id = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement
                        = connection.prepareStatement(query)) {
            statement.setString(1, driver.getName());
            statement.setString(2, driver.getLicenseNumber());
            statement.setString(3, driver.getLogin());
            statement.setString(4, driver.getPassword());
            statement.setLong(5, driver.getId());
            statement.executeUpdate();
            return driver;
        } catch (SQLException e) {
            logger.error("Can't update a driver. Params: driver Id={}", driver.getId(), e);
            throw new DataProcessingException("Can't update a driver" + driver, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Method delete a driver was called. Params: driver Id={}", id);
        String query = "UPDATE drivers SET is_deleted = TRUE WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Can't delete a driver. Params: driver Id={}", id, e);
            throw new DataProcessingException("Can't delete a driver with id " + id, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("Method parseDriverFromResultSet was called");
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String licenseNumber = resultSet.getString("license_number");
        String login = resultSet.getString("login");
        String password = resultSet.getString("password");
        Driver driver = new Driver();
        driver.setId(id);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        driver.setPassword(password);
        return driver;
    }

    @Override
    public Optional<Driver> findByLogin(String login) {
        logger.debug("Method findByLogin a driver was called. Params: login={}", login);
        String query = "SELECT * FROM drivers WHERE login = ? AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            Driver driver = null;
            if (resultSet.next()) {
                driver = parseDriverFromResultSet(resultSet);
            }
            return Optional.ofNullable(driver);
        } catch (SQLException e) {
            logger.error("Can't get a driver by login. Params: login={}", login, e);
            throw new DataProcessingException("Can't get a driver by login " + login, e);
        }
    }
}
