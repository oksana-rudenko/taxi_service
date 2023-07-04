package taxi.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import taxi.exception.DataProcessingException;
import taxi.lib.Injector;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.model.Manufacturer;

class CarServiceImplTest {
    private static final Injector injector = Injector.getInstance("taxi");
    private static final CarService carService = (CarService) injector.getInstance(CarService.class);
    private static final ManufacturerService manufacturerService = (ManufacturerService) injector
            .getInstance(ManufacturerService.class);
    private static final DriverService driverService =
            (DriverService) injector.getInstance(DriverService.class);
    private static Driver henry;
    private static Driver vick;
    private static Driver olia;
    private static Driver ivan;
    private static Driver dany;
    private static Driver sofi;
    private static Driver fill;
    private static Driver jeni;
    private static Driver bart;

    private static Manufacturer honda;
    private static Manufacturer jeep;
    private static Manufacturer audi;
    private static Manufacturer mitsubishi;
    private static Manufacturer subaru;

    @BeforeAll
    static void beforeAll() {
        henry = driverService.create(new Driver("Henry", "0102", "henry", "2010"));
        vick = driverService.create(new Driver("Victoria", "0123", "vick", "3210"));
        olia = driverService.create(new Driver("Olga", "0552", "olia", "2550"));
        ivan = driverService.create(new Driver("Ivan", "0763", "ivan", "3670"));
        dany = driverService.create(new Driver("Daniel", "0121", "dany", "1210"));
        sofi = driverService.create(new Driver("Sofia", "0332", "sofi", "2330"));
        fill = driverService.create(new Driver("Phillip", "0572", "fill", "2750"));
        jeni = driverService.create(new Driver("Jennifer", "0680", "jeni", "0860"));
        bart = driverService.create(new Driver("Bart", "0511", "bart", "1150"));
        honda = manufacturerService.create(new Manufacturer("Honda", "Japan"));
        jeep = manufacturerService.create(new Manufacturer("Jeep", "USA"));
        audi = manufacturerService.create(new Manufacturer("Audi", "Germany"));
        mitsubishi = manufacturerService.create(new Manufacturer("Mitsubishi", "Japan"));
        subaru = manufacturerService.create(new Manufacturer("Subaru", "Japan"));
    }

    @Test
    void create_newCar_ok() {
        Car expected = new Car("A8", audi);
        Car actual = carService.create(expected);
        Long id = actual.getId();
        expected.setId(id);
        assertEquals(expected, actual);
    }

    @Test
    void create_carNullModel_notOk() {
        Car car = new Car(null, honda);
        assertThrows(DataProcessingException.class, () -> {
            carService.create(car);
        });
    }

    @Test
    void get_carById_ok() {
        Car expected = new Car("Patriot", jeep);
        Car patriotCar = carService.create(expected);
        Long id = patriotCar.getId();
        expected.setId(id);
        Car actual = carService.get(id);
        assertEquals(expected, actual);
    }

    @Test
    void get_carAfterDriverDeleted_ok() {
        Car xt = new Car("XT", subaru);
        List<Driver> drivers = new ArrayList<>();
        drivers.add(vick);
        drivers.add(dany);
        drivers.add(olia);
        xt.setDrivers(drivers);
        Car xtCar = carService.create(xt);
        Long id = xtCar.getId();
        driverService.delete(dany.getId());
        Car car = carService.get(id);
        assertEquals(2, car.getDrivers().size());
    }

    @Test
    void get_carByNotExistingId_notOk() {
        Long id = -1L;
        assertThrows(NoSuchElementException.class, () -> {
            carService.get(id);
        });
    }

    @Test
    void get_deletedCar_notOk() {
        Car outlander = new Car("Outlander", mitsubishi);
        Car outlandrCar = carService.create(outlander);
        Long id = outlandrCar.getId();
        carService.delete(id);
        assertThrows(NoSuchElementException.class, () -> {
            carService.get(id);
        });
    }

    @Test
    void getAll_allCars_ok() {
        Car lancer = new Car("Lancer", mitsubishi);
        Car forester = new Car("Forester", subaru);
        Car wrangler = new Car("Wrangler", subaru);
        Car lancerCar = carService.create(lancer);
        Car foresterCar = carService.create(forester);
        Car wranglerCar = carService.create(wrangler);
        lancer.setId(lancerCar.getId());
        forester.setId(foresterCar.getId());
        wrangler.setId(wranglerCar.getId());
        List<Car> allCars = carService.getAll();
        assertTrue(allCars.contains(lancer)
                && allCars.contains(forester)
                && allCars.contains(wrangler));
    }

    @Test
    void getAll_allCarsDeleted_ok() {
        Car srv = new Car("CR-V", honda);
        carService.create(srv);
        List<Car> allCars = carService.getAll();
        allCars.forEach(c -> carService.delete(c.getId()));
        List<Car> expected = new ArrayList<>();
        List<Car> actual = carService.getAll();
        assertEquals(expected, actual);
    }

    @Test
    void update_car_ok() {
        Car crx = new Car("CR-X", honda);
        Car crxCar = carService.create(crx);
        Long id = crxCar.getId();
        Car expected = new Car("CR-Z", honda);
        expected.setId(id);
        Car actual = carService.update(expected);
        assertEquals(expected, actual);
    }

    @Test
    void update_carIdNotExisting_notOk() {
        Car outback = new Car("Outback", subaru);
        Long id = -1L;
        outback.setId(id);
        carService.update(outback);
        assertThrows(NoSuchElementException.class, () -> {
            carService.get(id);
        });
    }

    @Test
    void update_carNullModel_notOk() {
        Car tt = new Car("TT", audi);
        Car ttCar = carService.create(tt);
        Long id = ttCar.getId();
        Car audiCar = new Car(null, audi);
        audiCar.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            carService.update(audiCar);
        });
    }

    @Test
    void delete_car_ok() {
        Car q7 = new Car("Q7", audi);
        Car q7Car = carService.create(q7);
        Long id = q7Car.getId();
        boolean isDeleted = carService.delete(id);
        assertTrue(isDeleted);
    }

    @Test
    void delete_carIdNotExisting_notOk() {
        Long id = -1L;
        boolean isDeleted = carService.delete(id);
        assertFalse(isDeleted);
    }

    @Test
    void delete_carIsDeleted_ok() {
        Car pajero = new Car("Pajero", mitsubishi);
        Car pajeroCar = carService.create(pajero);
        Long id = pajeroCar.getId();
        carService.delete(id);
        assertFalse(carService.delete(id));
    }

    @Test
    void addDriverToCar_carDriver_ok() {
        Car tribeca = new Car("Tribeca", subaru);
        Car tribecaCar = carService.create(tribeca);
        Long id = tribecaCar.getId();
        carService.addDriverToCar(henry, tribecaCar);
        carService.addDriverToCar(vick, tribecaCar);
        Car car = carService.get(id);
        assertEquals(2, car.getDrivers().size());
    }

    @Test
    void addDriverToCar_carIdNotExisting_notOk() {
        Car a4 = new Car("A4", audi);
        Long id = -1L;
        a4.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            carService.addDriverToCar(olia, a4);
        });
    }

    @Test
    void addDriverToCar_driverAlreadyIncluded_notOk() {
        Car a3 = new Car("A3", audi);
        Car a3Car = carService.create(a3);
        carService.addDriverToCar(dany, a3Car);
        carService.addDriverToCar(sofi, a3Car);
        carService.addDriverToCar(henry, a3Car);
        assertThrows(DataProcessingException.class, () -> {
            carService.addDriverToCar(dany, a3Car);
        });
    }

    @Test
    void addDriverToCar_carDriverIdNotExisting_notOk() {
        Car a4 = new Car("A4", audi);
        Car a4Car = carService.create(a4);
        Driver joe = new Driver("Joe", "0099", "joe", "9900");
        joe.setId(-1L);
        assertThrows(DataProcessingException.class, () -> {
            carService.addDriverToCar(joe, a4Car);
        });
    }

    @Test
    void removeDriverFromCar_carDriver_ok() {
        Car a6 = new Car("A6", audi);
        List<Driver> drivers = new ArrayList<>();
        drivers.add(sofi);
        drivers.add(olia);
        drivers.add(henry);
        a6.setDrivers(drivers);
        Car a6Car = carService.create(a6);
        Long id = a6Car.getId();
        carService.removeDriverFromCar(henry, a6Car);
        Car car = carService.get(id);
        assertEquals(2, car.getDrivers().size());
    }

    @Test
    void removeDriverFromCar_carIdNotExisting_notOk() {
        Car q3 = new Car("Q3", audi);
        Long id = -1L;
        q3.setId(id);
        List<Driver> drivers = new ArrayList<>();
        drivers.add(dany);
        drivers.add(vick);
        q3.setDrivers(drivers);
        assertThrows(DataProcessingException.class, () -> {
            carService.removeDriverFromCar(dany, q3);
        });
    }

    @Test
    void removeDriverFromCar_carDriverIdNotExisting_ok() {
        Car q1 = new Car("Q1", audi);
        Car q1Car = carService.create(q1);
        Long id = q1Car.getId();
        Driver paul = new Driver();
        paul.setId(-1L);
        carService.removeDriverFromCar(paul, q1Car);
        Car car = carService.get(id);
        List<Driver> expected = new ArrayList<>();
        List<Driver> actual = car.getDrivers();
        assertEquals(expected, actual);
    }

    @Test
    void getAllByDriver_driverCars_ok() {
        Car sambar = new Car("Sambar", subaru);
        List<Driver> sambarDrivers = new ArrayList<>();
        sambarDrivers.add(bart);
        sambarDrivers.add(vick);
        sambar.setDrivers(sambarDrivers);
        Car etron = new Car("e-tron", audi);
        List<Driver> etronDrivers = new ArrayList<>();
        etronDrivers.add(vick);
        etronDrivers.add(sofi);
        etronDrivers.add(olia);
        etronDrivers.add(fill);
        etron.setDrivers(etronDrivers);
        Car clarity = new Car("Clarity", honda);
        List<Driver> clarityDrivers = new ArrayList<>();
        clarityDrivers.add(bart);
        clarityDrivers.add(fill);
        clarity.setDrivers(clarityDrivers);
        carService.create(sambar);
        carService.create(etron);
        carService.create(clarity);
        List<Car> henryCars = carService.getAllByDriver(fill.getId());
        List<Car> danyCars = carService.getAllByDriver(bart.getId());
        assertTrue(henryCars.size() == 2 && danyCars.size() == 2);
    }

    @Test
    void getAllByDriver_driverCarsDeleted_ok() {
        Car libero = new Car("Libero", subaru);
        List<Driver> liberoDrivers = new ArrayList<>();
        liberoDrivers.add(jeni);
        liberoDrivers.add(olia);
        liberoDrivers.add(henry);
        libero.setDrivers(liberoDrivers);
        Car liberoCar = carService.create(libero);
        Car q3 = new Car("Q3", audi);
        List<Driver> q3Drivers = new ArrayList<>();
        q3Drivers.add(dany);
        q3Drivers.add(jeni);
        q3.setDrivers(q3Drivers);
        Car q3Car = carService.create(q3);
        carService.delete(liberoCar.getId());
        carService.delete(q3Car.getId());
        List<Car> vickCars = carService.getAllByDriver(jeni.getId());
        assertEquals(0, vickCars.size());
    }

    @Test
    void getAllByDriver_driverIdNOtExisting_ok() {
        List<Car> expected = new ArrayList<>();
        Long id = -1L;
        List<Car> actual = carService.getAllByDriver(id);
        assertEquals(expected, actual);
    }

    @Test
    void getAllByDriver_driverCarsListEmpty_ok() {
        List<Car> expected = new ArrayList<>();
        List<Car> actual = carService.getAllByDriver(ivan.getId());
        assertEquals(expected, actual);
    }
}