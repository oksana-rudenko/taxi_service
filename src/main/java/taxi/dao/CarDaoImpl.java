package taxi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class CarDaoImpl implements CarDao {
    private static final Logger logger = LogManager.getLogger(CarDaoImpl.class);

    @Override
    public Car create(Car car) {
        logger.debug("Method create a car was called. Params: car model={}", car.getModel());
        String query = "INSERT INTO cars (model, manufacturer_id)"
                + "VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                             query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, car.getModel());
            statement.setLong(2, car.getManufacturer().getId());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                car.setId(resultSet.getObject(1, Long.class));
            }
        } catch (SQLException e) {
            logger.error("Can't create a car. Params: car model={}", car.getModel(), e);
            throw new DataProcessingException("Can't create a car " + car, e);
        }
        insertAllDrivers(car);
        return car;
    }

    @Override
    public Optional<Car> get(Long id) {
        logger.debug("Method get a car was called. Params: car Id={}", id);
        String query = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c "
                + "JOIN manufacturers m ON c.manufacturer_id = m.id "
                + "WHERE c.id = ? AND c.is_deleted = FALSE";
        Car car = null;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                car = parseCarFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error("Can't get a car. Params: car Id={}", id, e);
            throw new DataProcessingException("Can't get a car by id: " + id, e);
        }
        if (car != null) {
            car.setDrivers(getAllDriversByCarId(car.getId()));
        }
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> getAll() {
        logger.debug("Method getAll cars was called");
        String query = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m ON c.manufacturer_id = m.id"
                + " WHERE c.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Can't get any car", e);
            throw new DataProcessingException("Can't get all cars", e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        return cars;
    }

    @Override
    public Car update(Car car) {
        logger.debug("Method update a car was called. Params: car Id={}", car.getId());
        String query = "UPDATE cars SET model = ?, manufacturer_id = ? WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setString(1, car.getModel());
            statement.setLong(2, car.getManufacturer().getId());
            statement.setLong(3, car.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Can't update a car. Params: car Id={}", car.getId(), e);
            throw new DataProcessingException("Can't update a car " + car, e);
        }
        deleteAllDrivers(car);
        insertAllDrivers(car);
        return car;
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Method delete a car was called. Params: car Id={}", id);
        String query = "UPDATE cars SET is_deleted = TRUE WHERE id = ?"
                + " AND is_deleted = FALSE";
        try (Connection connection = ConnectionUtil.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(query)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Can't delete a car. Params: car Id={}", id, e);
            throw new DataProcessingException("Can't delete a car by id " + id, e);
        }
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        logger.debug("Method getAllByDriver was called. Params: driver Id={}", driverId);
        String query = "SELECT c.id AS id, "
                + "model, "
                + "manufacturer_id, "
                + "m.name AS manufacturer_name, "
                + "m.country AS manufacturer_country "
                + "FROM cars c"
                + " JOIN manufacturers m ON c.manufacturer_id = m.id"
                + " JOIN cars_drivers cd ON c.id = cd.car_id"
                + " JOIN drivers d ON cd.driver_id = d.id"
                + " WHERE c.is_deleted = FALSE AND driver_id = ?"
                + " AND d.is_deleted = FALSE";
        List<Car> cars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, driverId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cars.add(parseCarFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Can't get all cars by the driver. Params: driver Id={}", driverId, e);
            throw new DataProcessingException("Can't get all cars for driver with id: "
                + driverId, e);
        }
        cars.forEach(car -> car.setDrivers(getAllDriversByCarId(car.getId())));
        return cars;
    }

    private void insertAllDrivers(Car car) {
        logger.debug("Method insertAllDrivers was called. Params: car Id={}", car.getId());
        List<Driver> drivers = car.getDrivers();
        if (drivers.size() == 0) {
            return;
        }
        String query = "INSERT INTO cars_drivers (car_id, driver_id) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, car.getId());
            for (Driver driver : drivers) {
                statement.setLong(2, driver.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Can't insert drivers to car. Params: car Id={}", car.getId(), e);
            throw new DataProcessingException("Can't insert drivers " + drivers, e);
        }
    }

    private void deleteAllDrivers(Car car) {
        logger.debug("Method deleteAllDrivers was called. Params: car Id={}", car.getId());
        String query = "DELETE FROM cars_drivers WHERE car_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, car.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Can't delete drivers for car. Params: car Id={}", car.getId(), e);
            throw new DataProcessingException("Can't delete drivers " + car.getDrivers()
                + " of car with id: " + car.getId(), e);
        }
    }

    private List<Driver> getAllDriversByCarId(Long carId) {
        logger.debug("Method getAllDriversByCarId was called. Params: car Id={}", carId);
        String query = "SELECT id, name, license_number, login "
                + "FROM cars_drivers cd "
                + "JOIN drivers d ON cd.driver_id = d.id "
                + "WHERE car_id = ? AND is_deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(query)) {
            statement.setLong(1, carId);
            ResultSet resultSet = statement.executeQuery();
            List<Driver> drivers = new ArrayList<>();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            return drivers;
        } catch (SQLException e) {
            logger.error("Can't get all drivers. Params: car Id={}", carId, e);
            throw new DataProcessingException("Can't get all drivers by car id" + carId, e);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("Method parseDriverFromResultSet was called");
        Long driverId = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String licenseNumber = resultSet.getString("license_number");
        String login = resultSet.getString("login");
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setName(name);
        driver.setLicenseNumber(licenseNumber);
        driver.setLogin(login);
        return driver;
    }

    private Car parseCarFromResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("Method parseCarFromResultSet was called");
        Long manufacturerId = resultSet.getObject("manufacturer_id", Long.class);
        String manufacturerName = resultSet.getString("manufacturer_name");
        String manufacturerCountry = resultSet.getString("manufacturer_country");
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(manufacturerId);
        manufacturer.setName(manufacturerName);
        manufacturer.setCountry(manufacturerCountry);
        Long carId = resultSet.getObject("id", Long.class);
        String model = resultSet.getString("model");
        Car car = new Car();
        car.setId(carId);
        car.setModel(model);
        car.setManufacturer(manufacturer);
        return car;
    }
}
