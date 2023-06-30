package client_server.database;

import client_server.entities.Category;
import client_server.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB implements DAO {

    private Connection connection;

    public void init() throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/zlagoda", "root", "root_naukma"
        );
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void addCategory(Category category) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO category VALUES (?,?)");
        statement.setString(1, category.getName());
        statement.setString(2, category.getCharacteristics());
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void addProduct(Product product) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO product (product_name, characteristics, manufacturer, quantity, price, category_name) VALUES (?,?,?,?,?,?)");
        statement.setString(1, product.getName());
        statement.setString(2, product.getCharacteristics());
        statement.setString(3, product.getManufacturer());
        statement.setInt(4, product.getQuantity());
        statement.setInt(5, product.getPrice());
        statement.setString(6, product.getCategory().getName());
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void editCategory(Category category) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE category SET characteristics = ? WHERE category_name = ?");
        statement.setString(1, category.getCharacteristics());
        statement.setString(2, category.getName());
        int result = statement.executeUpdate();
        statement.close();
    }


    @Override
    public void editProduct(Product product) throws SQLException {
        String query = "UPDATE product SET characteristics = ?, manufacturer = ?, quantity = ?, price = ?, category_name = ? WHERE product_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, product.getCharacteristics());
        statement.setString(2, product.getManufacturer());
        statement.setInt(3, product.getQuantity());
        statement.setInt(4, product.getPrice());
        statement.setString(5, product.getCategory().getName());
        statement.setString(6, product.getName());
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void deleteCategory(String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM category WHERE category_name = ?");
        statement.setString(1, name);
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void deleteProduct(String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM product WHERE product_name = ?");
        statement.setString(1, name);
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public Product searchProduct(String name) throws SQLException {
        Product product = new Product();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM product WHERE product_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            product.setName(resultSet.getString("product_name"));
            product.setCharacteristics(resultSet.getString("characteristics"));
            product.setManufacturer(resultSet.getString("manufacturer"));
            product.setQuantity(resultSet.getInt("quantity"));
            product.setPrice(resultSet.getInt("price"));
            product.setCategory(searchCategory(resultSet.getString("category_name")));
        }
        return product;
    }

    public List<Product> searchProductByName(String name) throws SQLException {
        List<Product> products = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM product WHERE product_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Product product = new Product();
            product.setName(resultSet.getString("product_name"));
            product.setCharacteristics(resultSet.getString("characteristics"));
            product.setManufacturer(resultSet.getString("manufacturer"));
            product.setQuantity(resultSet.getInt("quantity"));
            product.setPrice(resultSet.getInt("price"));
            product.setCategory(searchCategory(resultSet.getString("category_name")));
            products.add(product);
        }
        return products;
    }

    @Override
    public List<Product> lookUpAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM product");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Product product = new Product();
            product.setName(resultSet.getString("product_name"));
            product.setCharacteristics(resultSet.getString("characteristics"));
            product.setManufacturer(resultSet.getString("manufacturer"));
            product.setQuantity(resultSet.getInt("quantity"));
            product.setPrice(resultSet.getInt("price"));
            product.setCategory(searchCategory(resultSet.getString("category_name")));
            products.add(product);
        }
        return products;
    }

    @Override
    public List<Category> lookUpAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM category");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Category category = new Category();
            category.setName(resultSet.getString("category_name"));
            category.setCharacteristics(resultSet.getString("characteristics"));
            categories.add(category);
        }
        return categories;
    }

    @Override
    public List<Product> lookUpProductsByCategory(String name) throws SQLException {
        List<Product> products = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM product WHERE category_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Product product = new Product();
            product.setName(resultSet.getString("product_name"));
            product.setCharacteristics(resultSet.getString("characteristics"));
            product.setManufacturer(resultSet.getString("manufacturer"));
            product.setQuantity(resultSet.getInt("quantity"));
            product.setPrice(resultSet.getInt("price"));
            product.setCategory(searchCategory(resultSet.getString("category_name")));
            products.add(product);
        }
        return products;
    }

    @Override
    public int getPriceOfAllProducts() throws SQLException {
        int total = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT SUM(quantity*price) AS total FROM product");
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            total = resultSet.getInt("total");
        }
        return total;
    }

    @Override
    public int getPriceOfProductsByCategory(String name) throws SQLException {
        int total = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT SUM(quantity*price) AS total FROM product WHERE category_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            total = resultSet.getInt("total");
        }
        return total;
    }

    @Override
    public int getPriceOfProductsByName(String name) throws SQLException {
        int total = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT quantity*price AS total FROM product WHERE product_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            total = resultSet.getInt("total");
        }
        return total;
    }

    @Override
    public int getNumberOfAllProducts() throws SQLException {
        int num = 0;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT SUM(quantity) AS num FROM product");
        if (resultSet.next()) {
            num = resultSet.getInt("num");
        }
        return num;
    }

    @Override
    public int getNumberOfProductsByCategory(String name) throws SQLException {
        int num = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT SUM(quantity) AS num FROM product WHERE category_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            num = resultSet.getInt("num");
        }
        return num;
    }

    @Override
    public void addNumberOfProduct(String name, int number) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE product SET quantity = quantity + ? WHERE product_name = ?");
        statement.setInt(1, number);
        statement.setString(2, name);
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void subtractNumberOfProduct(String name, int number) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE product SET quantity = quantity - ? WHERE product_name = ?");
        statement.setInt(1, number);
        statement.setString(2, name);
        int result = statement.executeUpdate();
        statement.close();
    }

    @Override
    public void setPrice(String name, int price) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE product SET price = ? WHERE product_name = ?");
        statement.setInt(1, price);
        statement.setString(2, name);
        int result = statement.executeUpdate();
        statement.close();
    }

    public Category searchCategory(String name) throws SQLException {

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM category WHERE category_name = ?");
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Category category = new Category();
            category.setName(resultSet.getString("category_name"));
            category.setCharacteristics(resultSet.getString("characteristics"));
            return category;
        }
        return null;
    }

}
