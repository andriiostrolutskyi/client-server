package client_server;

import client_server.database.DB;
import client_server.entities.Product;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class DBTest extends TestCase {
    private DB db;

    public void setup() throws SQLException, ClassNotFoundException, InterruptedException {
        db = new DB();
        db.init();

    }
    @Test
    public void testLookUpAllProducts() throws SQLException, InterruptedException, ClassNotFoundException {
        setup();
        List<Product> products = db.lookUpAllProducts();
        assertNotNull(products);
        assertEquals(11, products.size());
    }

    @Test
    public void testLookUpProductsByCategory() throws SQLException, InterruptedException, ClassNotFoundException {
        setup();
        List<Product> products = db.lookUpProductsByCategory("Bakery");
        assertNotNull(products);
        assertEquals(2, products.size());
    }

    @Test
    public void testGetPriceOfAllProducts() throws SQLException, InterruptedException, ClassNotFoundException {
        setup();
        int price = db.getPriceOfAllProducts();
        assertEquals(45, price);
    }

    @Test
    public void testGetPriceOfProductsByCategory() throws SQLException, InterruptedException, ClassNotFoundException {
        setup();
        int price = db.getPriceOfProductsByCategory("Snacks");
        assertEquals(4, price);
    }

    @Test
    public void testGetNumberOfAllProducts() throws SQLException, InterruptedException, ClassNotFoundException {
        setup();
        int num = db.getNumberOfAllProducts();
        assertEquals(575, num);
    }
}
