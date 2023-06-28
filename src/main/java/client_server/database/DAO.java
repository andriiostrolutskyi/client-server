package client_server.database;

import client_server.entities.Category;
import client_server.entities.Product;

import java.sql.SQLException;
import java.util.List;

public interface DAO {

    void addCategory(Category category) throws SQLException;
    void addProduct(Product product) throws SQLException;
    void editCategory(Category category) throws SQLException;
    void editProduct(Product product) throws SQLException;
    void deleteCategory(String name) throws SQLException;
    void deleteProduct(String name) throws SQLException;
    Product searchProduct(String name) throws SQLException;
    List<Product> lookUpAllProducts() throws SQLException;
    List<Product> lookUpProductsByCategory(String name) throws SQLException;
    int getPriceOfAllProducts() throws SQLException;
    int getPriceOfProductsByCategory(String name) throws SQLException;
    int getNumberOfAllProducts() throws SQLException;
    void addNumberOfProduct(String name, int number) throws SQLException;
    void subtractNumberOfProduct(String name, int number) throws SQLException;
    void setPrice(String name, int price) throws SQLException;
}
