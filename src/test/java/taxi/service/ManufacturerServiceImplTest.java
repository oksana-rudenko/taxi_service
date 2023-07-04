package taxi.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import taxi.exception.DataProcessingException;
import taxi.lib.Injector;
import taxi.model.Manufacturer;

class ManufacturerServiceImplTest {
    private static final Injector injector = Injector.getInstance("taxi");
    private final ManufacturerService manufacturerService = (ManufacturerService) injector
            .getInstance(ManufacturerService.class);

    @Test
    void create_newManufacturer_ok() {
        Manufacturer expected = new Manufacturer("Volvo", "Germany");
        Manufacturer actual = manufacturerService.create(expected);
        Long id = actual.getId();
        expected.setId(id);
        assertEquals(expected, actual);
    }

    @Test
    void create_emptyManufacturer_notOk() {
        Manufacturer manufacturer = new Manufacturer();
        assertThrows(DataProcessingException.class, () -> {
            manufacturerService.create(manufacturer);
        });
    }

    @Test
    void create_manufacturerNullName_notOk() {
        Manufacturer manufacturer = new Manufacturer(null, "Italy");
        assertThrows(DataProcessingException.class, () -> {
            manufacturerService.create(manufacturer);
        });
    }

    @Test
    void create_manufacturerNullCountry_notOk() {
        Manufacturer manufacturer = new Manufacturer("Fiat", null);
        assertThrows(DataProcessingException.class, () -> {
            manufacturerService.create(manufacturer);
        });
    }

    @Test
    void get_manufacturerById_ok() {
        Manufacturer toyota = new Manufacturer("Totyota", "Japan");
        Manufacturer expected = manufacturerService.create(toyota);
        Long id = expected.getId();
        Manufacturer actual = manufacturerService.get(id);
        assertEquals(expected, actual);
    }

    @Test
    void get_manufacturerByNullId_notOk() {
        assertThrows(NullPointerException.class, () -> {
            manufacturerService.get(null);
        });
    }

    @Test
    void get_manufacturerByNotExistingId_notOk() {
        Long id = -1L;
        assertThrows(NoSuchElementException.class, () -> {
            manufacturerService.get(id);
        });
    }

    @Test
    void get_deletedManufacturer_notOk() {
        Manufacturer opel = new Manufacturer("Opel", "Germany");
        Manufacturer opelManufacturer = manufacturerService.create(opel);
        Long id = opelManufacturer.getId();
        manufacturerService.delete(id);
        assertThrows(NoSuchElementException.class, () -> {
            manufacturerService.get(id);
        });
    }

    @Test
    void getAll_manufacturers_ok() {
        Manufacturer skoda = new Manufacturer("Skoda", "Czechia");
        Manufacturer mazda = new Manufacturer("Mazda", "Japan");
        Manufacturer skodaManufacturer = manufacturerService.create(skoda);
        Manufacturer mazdaManufacturer = manufacturerService.create(mazda);
        Long skodaId = skodaManufacturer.getId();
        Long mazdaId = mazdaManufacturer.getId();
        skoda.setId(skodaId);
        mazda.setId(mazdaId);
        List<Manufacturer> manufacturers = manufacturerService.getAll();
        assertTrue(manufacturers.contains(skoda) && manufacturers.contains(mazda));
    }

    @Test
    void getAll_allManufacturersDeleted_ok() {
        Manufacturer renault = new Manufacturer("Renault", "France");
        Manufacturer nissan = new Manufacturer("Nissan", "Japan");
        manufacturerService.create(renault);
        manufacturerService.create(nissan);
        List<Manufacturer> manufacturers = manufacturerService.getAll();
        manufacturers.forEach(m -> manufacturerService.delete(m.getId()));
        List<Manufacturer> actual = manufacturerService.getAll();
        List<Manufacturer> expected = new ArrayList<>();
        assertEquals(expected, actual);
    }

    @Test
    void update_manufacturer_ok() {
        Manufacturer lexus = new Manufacturer("Lexus", "USA");
        Manufacturer lexusManufacturer = manufacturerService.create(lexus);
        Long id = lexusManufacturer.getId();
        Manufacturer expected = new Manufacturer("Lexus", "Japan");
        expected.setId(id);
        Manufacturer actual = manufacturerService.update(expected);
        assertEquals(expected, actual);
    }

    @Test
    void update_manufacturerIdNotExisting_notOk() {
        Manufacturer manufacturer = new Manufacturer("Mazda", "Japan");
        Long id = -1L;
        manufacturer.setId(id);
        manufacturerService.update(manufacturer);
        assertThrows(NoSuchElementException.class, () -> {
            manufacturerService.get(id);
        });
    }

    @Test
    void update_manufacturerNullName_notOk() {
        Manufacturer citroen = new Manufacturer("Citroen", "France");
        Manufacturer citroenManufacturer = manufacturerService.create(citroen);
        Long id = citroenManufacturer.getId();
        Manufacturer manufacturer = new Manufacturer(null, "France");
        manufacturer.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            manufacturerService.update(manufacturer);
        });
    }

    @Test
    void update_manufacturerNullCountry_notOk() {
        Manufacturer ford = new Manufacturer("Ford", "USA");
        Manufacturer fordManufacturer = manufacturerService.create(ford);
        Long id = fordManufacturer.getId();
        Manufacturer manufacturer = new Manufacturer("GM", null);
        manufacturer.setId(id);
        assertThrows(DataProcessingException.class, () -> {
            manufacturerService.update(manufacturer);
        });
    }

    @Test
    void delete_manufacturer_ok() {
        Manufacturer bmw = new Manufacturer("BMW", "Germany");
        Manufacturer bmwManufacturer = manufacturerService.create(bmw);
        Long id = bmwManufacturer.getId();
        boolean isDeleted = manufacturerService.delete(id);
        assertTrue(isDeleted);
    }

    @Test
    void delete_manufacturerIdNotExisting_notOk() {
        Long id = -1L;
        boolean isDeleted = manufacturerService.delete(id);
        assertFalse(isDeleted);
    }

    @Test
    void delete_manufacturerIsDeleted_ok() {
        Manufacturer dodge = new Manufacturer("Dodge", "USA");
        Manufacturer dodgeManufacturer = manufacturerService.create(dodge);
        Long id = dodgeManufacturer.getId();
        manufacturerService.delete(id);
        assertTrue(manufacturerService.delete(id));
    }
}