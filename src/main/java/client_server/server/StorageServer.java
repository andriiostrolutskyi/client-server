package client_server.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import client_server.database.DB;
import client_server.entities.Category;
import client_server.entities.Product;
import com.sun.net.httpserver.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class StorageServer {
    static final int nThreads = 10;
    static final String KEYSTORE_PATH = "mykeystore.jks";
    static final String KEYSTORE_PASSWORD = "andrii";

    public static void main(String[] args) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fileInputStream = new FileInputStream(KEYSTORE_PATH);
            keyStore.load(fileInputStream, KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8888), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            Executor executor = Executors.newFixedThreadPool(nThreads);

            server.createContext("/home", new HomeHandler());
            server.createContext("/home/add", new AddHandler());
            server.createContext("/home/subtract", new SubtractHandler());

            server.createContext("/category", new CategoryHandler());
            server.createContext("/category/add", new AddCategoryHandler());
            server.createContext("/category/addCategory", new PostAddCategoryHandler());
            server.createContext("/category/edit", new EditCategoryHandler());
            server.createContext("/category/editCategory", new PostEditCategoryHandler());
            server.createContext("/category/delete", new PostDeleteCategoryHandler());

            server.createContext("/product", new ProductHandler());
            server.createContext("/product/searchByCategory", new SearchByCategoryHandler());
            server.createContext("/product/searchByName", new SearchByNameHandler());
            server.createContext("/product/add", new AddProductHandler());
            server.createContext("/product/addProduct", new PostAddProductHandler());
            server.createContext("/product/edit", new EditProductHandler());
            server.createContext("/product/editProduct", new PostEditProductHandler());
            server.createContext("/product/delete", new PostDeleteProductHandler());

            server.setExecutor(executor);
            server.start();
            System.out.println("Server started.");
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                 UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /*Home-------------------------------------------------*/

    static class HomeHandler implements HttpHandler {
        private List<Product> products;
        private DB db;

        public void handle(HttpExchange exchange) throws IOException {
            db = new DB();
            try {
                db.init();
                products = db.lookUpAllProducts();
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }

            String script = "<script>\n" +
                    "function addProduct() {\n" +
                    "   var selectedProduct = document.getElementById('productDropdown').value;\n" +
                    "   var quantity = document.getElementById('quantityInput').value;\n" +
                    "   if (selectedProduct && quantity) {\n" +
                    "       var url = '/home/add?product=' + encodeURIComponent(selectedProduct) + '&quantity=' + encodeURIComponent(quantity);\n" +
                    "       makeRequest(url);\n" +
                    "   }\n" +
                    "}\n" +
                    "function subtractProduct() {\n" +
                    "   var selectedProduct = document.getElementById('productDropdown').value;\n" +
                    "   var quantity = document.getElementById('quantityInput').value;\n" +
                    "   if (selectedProduct && quantity) {\n" +
                    "       var url = '/home/subtract?product=' + encodeURIComponent(selectedProduct) + '&quantity=' + encodeURIComponent(quantity);\n" +
                    "       makeRequest(url);\n" +
                    "   }\n" +
                    "}\n" +
                    "function makeRequest(url) {\n" +
                    "   var xhr = new XMLHttpRequest();\n" +
                    "   xhr.open('POST', url);\n" +
                    "   xhr.onload = function() {\n" +
                    "       if (xhr.status === 200) {\n" +
                    "           // Redirect back to the home page\n" +
                    "           window.location.href = '/home';\n" +
                    "       }\n" +
                    "   };\n" +
                    "   xhr.send();\n" +
                    "}\n" +
                    "</script>\n";

            StringBuilder htmlResponse = new StringBuilder();
            htmlResponse.append("<html><body>");
            htmlResponse.append("<h1>Storage</h1>");
            htmlResponse.append("<div style=\"display: flex;\">");
            htmlResponse.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
            htmlResponse.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
            htmlResponse.append("<h3><a href=\"/category\">Categories</a></h3>");
            htmlResponse.append("</div>");
            htmlResponse.append("<select id='productDropdown'>");

            for (Product product : products) {
                htmlResponse.append("<option value='").append(product.getName()).append("'>").append(product.getName()).append("</option>");
            }

            htmlResponse.append("</select>");
            htmlResponse.append("<br>");
            htmlResponse.append("<input type='number' id='quantityInput' placeholder='Enter quantity'>");
            htmlResponse.append("<br>");
            htmlResponse.append("<button onclick='addProduct()'>Add</button>");
            htmlResponse.append("<button onclick='subtractProduct()'>Subtract</button>");

            htmlResponse.insert(0, script);

            htmlResponse.append("</body></html>");

            String response = htmlResponse.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        }
    }
    static class AddHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParameters(query);
            String product = params.get("product");
            int quantity = Integer.parseInt(params.get("quantity"));

            DB db = new DB();
            try {
                db.init();
                db.addNumberOfProduct(product, quantity);
            } catch (ClassNotFoundException | InterruptedException | SQLException e) {
                throw new RuntimeException(e);
            }

            exchange.getResponseHeaders().set("Location", "/home");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }
    static class SubtractHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParameters(query);
            String product = params.get("product");
            int quantity = Integer.parseInt(params.get("quantity"));

            DB db = new DB();
            try {
                db.init();
                db.subtractNumberOfProduct(product, quantity);
            } catch (ClassNotFoundException | InterruptedException | SQLException e) {
                throw new RuntimeException(e);
            }

            exchange.getResponseHeaders().set("Location", "/home");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    private static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                params.put(key, value);
            }
        }
        return params;
    }

    /*Category--------------------------------------------------*/
    static class CategoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            List<Category> categories;

            try {
                DB db = new DB();
                db.init();
                categories = db.lookUpAllCategories();
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Internal Server Error".getBytes());
                response.close();
                return;
            }

            StringBuilder responseBuilder = buildTableOfCategories(categories);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class AddCategoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            StringBuilder responseBuilder = buildAddCategory();

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class PostAddCategoryHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String[] params = requestBody.split("&");
            String categoryName = "";
            String characteristics = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("category_name".equals(keyValue[0])) {
                        categoryName = URLDecoder.decode(keyValue[1]);
                    } else if ("characteristics".equals(keyValue[0])) {
                        characteristics = URLDecoder.decode(keyValue[1]);
                    }
                }
            }

            Category category = new Category(categoryName, characteristics);

            try {
                DB db = new DB();
                db.init();
                db.addCategory(category);
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/category");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    static class EditCategoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String categoryName = null;
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParams(requestBody);
                categoryName = params.get("category");
            }
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            StringBuilder responseBuilder = buildEditCategory(categoryName);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }

        private Map<String, String> parseParams(String requestBody) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }
            return params;
        }
    }

    static class PostEditCategoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String categoryName = URLDecoder.decode(extractCategoryNameFromRequestURI(exchange.getRequestURI().toString()));

            try {
                DB db = new DB();
                db.init();
                Category category = db.searchCategory(categoryName);

                if (category != null) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    String characteristics = "";

                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2 && "characteristics".equals(keyValue[0])) {
                            characteristics = URLDecoder.decode(keyValue[1]);
                            break;
                        }
                    }

                    category.setCharacteristics(characteristics);
                    db.editCategory(category);
                    db.close();

                    redirectToCategoriesPage(exchange);
                } else {
                    redirectToCategoriesPage(exchange);
                }
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                redirectToCategoriesPage(exchange);
            }

        }

        private String extractCategoryNameFromRequestURI(String requestURI) {
            String[] pathSegments = requestURI.split("/");
            if (pathSegments.length >= 3) {
                String a = pathSegments[2];
                String substringToRemove = "editCategory?name=";
                return a.replace(substringToRemove, "");
            }
            return "";
        }

        private void redirectToCategoriesPage(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/category");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    static class PostDeleteCategoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String categoryName = null;
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParams(requestBody);
                categoryName = URLDecoder.decode(params.get("category"));
            }

            try {
                DB db = new DB();
                db.init();
                db.deleteCategory(categoryName);
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/category");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }

        private Map<String, String> parseParams(String requestBody) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }
            return params;
        }
    }


    /*Category builders--------------------------------------------------*/

    private static StringBuilder buildTableOfCategories(List<Category> categories) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<table>");
        responseBuilder.append("<tr>");
        responseBuilder.append("<th>Name</th>");
        responseBuilder.append("<th>Characteristics</th>");
        responseBuilder.append("<th>");
        responseBuilder.append("<form action=\"/category/add\" method=\"post\"><button type=\"submit\">Add category</button></form>");
        responseBuilder.append("</th>");
        responseBuilder.append("</tr>");

        for (Category category : categories) {
            responseBuilder.append("<tr>");
            responseBuilder.append("<td>").append(category.getName()).append("</td>");
            responseBuilder.append("<td>").append(category.getCharacteristics()).append("</td>");
            responseBuilder.append("<td>");
            responseBuilder.append("<th>");
            responseBuilder.append("<form action=\"/category/edit\" method=\"post\"><button type=\"submit\" name=\"category\" value=\"" + category.getName() + "\">Edit</button></form>");
            responseBuilder.append("</th>");
            responseBuilder.append("<th>");
            responseBuilder.append("<form action=\"/category/delete\" method=\"post\"><button type=\"submit\" name=\"category\" value=\"" + category.getName() + "\">Delete</button></form>");
            responseBuilder.append("</th>");
            responseBuilder.append("</td>");
            responseBuilder.append("</tr>");
        }

        responseBuilder.append("</table>");
        responseBuilder.append("</body></html>");
        return responseBuilder;
    }

    private static StringBuilder buildAddCategory() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<h2>Add Category</h2>");
        responseBuilder.append("<form method=\"POST\" action=\"/category/addCategory\">");
        responseBuilder.append("<label for=\"category_name\">Category Name:</label>");
        responseBuilder.append("<input type=\"text\" id=\"category_name\" name=\"category_name\"><br><br>");
        responseBuilder.append("<label for=\"characteristics\">Characteristics:</label>");
        responseBuilder.append("<input type=\"text\" id=\"characteristics\" name=\"characteristics\"><br><br>");
        responseBuilder.append("<input type=\"submit\" value=\"Submit\">");
        responseBuilder.append("</form>");

        responseBuilder.append("</body></html>");
        return responseBuilder;
    }

    private static StringBuilder buildEditCategory(String categoryName) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<h2>Edit Category " + categoryName + "</h2>");
        responseBuilder.append("<form method=\"POST\" action=\"/category/editCategory?name=" + categoryName + "\">");
        responseBuilder.append("<input type=\"hidden\" name=\"category_name\" value=\"" + categoryName + "\">");
        responseBuilder.append("<label for=\"characteristics\">Characteristics:</label>");
        responseBuilder.append("<input type=\"text\" id=\"characteristics\" name=\"characteristics\"><br><br>");
        responseBuilder.append("<input type=\"submit\" value=\"Submit\">");
        responseBuilder.append("</form>");

        responseBuilder.append("</body></html>");
        return responseBuilder;
    }


    /*Product--------------------------------------------------*/
    static class ProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            List<Product> products;
            int price;
            int number;
            try {
                DB db = new DB();
                db.init();
                products = db.lookUpAllProducts();
                price = db.getPriceOfAllProducts();
                number = db.getNumberOfAllProducts();
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Internal Server Error".getBytes());
                response.close();
                return;
            }

            StringBuilder responseBuilder = buildTableOfProducts(products, price, number);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class SearchByCategoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String queryString = exchange.getRequestURI().getQuery();
            String[] queryParams = queryString.split("=");
            if (queryParams.length != 2 || !queryParams[0].equals("category")) {
                exchange.sendResponseHeaders(400, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Invalid category parameter".getBytes());
                response.close();
                return;
            }

            String categoryName = URLDecoder.decode(queryParams[1]);

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            List<Product> products;
            int price;
            int num;
            try {
                DB db = new DB();
                db.init();
                products = db.lookUpProductsByCategory(categoryName);
                price = db.getPriceOfProductsByCategory(categoryName);
                num = db.getNumberOfProductsByCategory(categoryName);
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Internal Server Error".getBytes());
                response.close();
                return;
            }

            StringBuilder responseBuilder = buildTableOfProducts(products, price, num);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class SearchByNameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String queryString = exchange.getRequestURI().getQuery();
            String[] queryParams = queryString.split("=");
            if (queryParams.length != 2 || !queryParams[0].equals("name")) {
                exchange.sendResponseHeaders(400, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Invalid product name".getBytes());
                response.close();
                return;
            }

            String name = URLDecoder.decode(queryParams[1]);

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            List<Product> products;
            int price;
            try {
                DB db = new DB();
                db.init();
                products = db.searchProductByName(name);
                price = db.getPriceOfProductsByName(name);
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream response = exchange.getResponseBody();
                response.write("Internal Server Error".getBytes());
                response.close();
                return;
            }

            StringBuilder responseBuilder = buildTableOfProducts(products, price, 0);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class AddProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            StringBuilder responseBuilder = buildAddProduct();

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }
    }

    static class PostAddProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String[] params = requestBody.split("&");
            String name = "";
            String characteristics = "";
            String manufacturer = "";
            int quantity = 0;
            int price = 0;
            String categoryName = "";

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("name".equals(keyValue[0])) {
                        name = URLDecoder.decode(keyValue[1]);
                    } else if ("characteristics".equals(keyValue[0])) {
                        characteristics = URLDecoder.decode(keyValue[1]);
                    } else if ("manufacturer".equals(keyValue[0])) {
                        manufacturer = URLDecoder.decode(keyValue[1]);
                    } else if ("quantity".equals(keyValue[0])) {
                        quantity = Integer.parseInt(keyValue[1]);
                    } else if ("price".equals(keyValue[0])) {
                        price = Integer.parseInt(keyValue[1]);
                    } else if ("category".equals(keyValue[0])) {
                        categoryName = URLDecoder.decode(keyValue[1]);
                    }
                }
            }

            try {
                DB db = new DB();
                db.init();
                Category category = db.searchCategory(categoryName);
                Product product = new Product(name, characteristics, manufacturer, quantity, price, category);
                db.addProduct(product);
                db.close();
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }

            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/product");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    static class EditProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String productName = null;
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParams(requestBody);
                productName = params.get("name");
            }
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            StringBuilder responseBuilder = buildEditProduct(productName);

            OutputStream response = exchange.getResponseBody();
            response.write(responseBuilder.toString().getBytes());
            response.close();
        }

        private Map<String, String> parseParams(String requestBody) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }
            return params;
        }
    }

    static class PostEditProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String productName = URLDecoder.decode(extractProductNameFromRequestURI(exchange.getRequestURI().toString()));

            try {
                DB db = new DB();
                db.init();
                Product product = db.searchProduct(productName);

                if (product != null) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    String characteristics = "";
                    String manufacturer = "";
                    int quantity = 0;
                    int price = 0;
                    Category category = null;

                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = keyValue[1];
                            switch (key) {
                                case "characteristics":
                                    characteristics = URLDecoder.decode(value);
                                    break;
                                case "manufacturer":
                                    manufacturer = URLDecoder.decode(value);
                                    break;
                                case "quantity":
                                    quantity = Integer.parseInt(value);
                                    break;
                                case "price":
                                    price = Integer.parseInt(value);
                                    break;
                                case "category":
                                    category = db.searchCategory(URLDecoder.decode(value));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    product.setCharacteristics(characteristics);
                    product.setManufacturer(manufacturer);
                    product.setQuantity(quantity);
                    product.setPrice(price);
                    product.setCategory(category);
                    db.editProduct(product);
                    db.close();

                    redirectToProductsPage(exchange);
                } else {
                    redirectToProductsPage(exchange);
                }
            } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                redirectToProductsPage(exchange);
            }
        }

        private String extractProductNameFromRequestURI(String requestURI) {
            String[] pathSegments = requestURI.split("/");
            if (pathSegments.length >= 3) {
                String productSegment = pathSegments[2];
                String substringToRemove = "editProduct?name=";
                return productSegment.replace(substringToRemove, "");
            }
            return "";
        }

        private void redirectToProductsPage(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/product");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    static class PostDeleteProductHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String productName = null;
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParams(requestBody);
                productName = URLDecoder.decode(params.get("name"));
            }

            try {
                DB db = new DB();
                db.init();
                db.deleteProduct(productName);
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Headers headers = exchange.getResponseHeaders();
            headers.set("Location", "/product");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }

        private Map<String, String> parseParams(String requestBody) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }
            return params;
        }
    }

    /*Product builders--------------------------------------------------*/
    private static StringBuilder buildTableOfProducts(List<Product> products, int price, int number) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<form action=\"/product/searchByName\" method=\"GET\">");
        responseBuilder.append("<input type=\"text\" name=\"name\" placeholder=\"Enter a name\">");
        responseBuilder.append("<input type=\"submit\" value=\"Search\">");
        responseBuilder.append("</form>");
        responseBuilder.append("<div style=\"width: 10px;\"></div>");
        responseBuilder.append("<form action=\"/product/searchByCategory\" method=\"GET\">");
        responseBuilder.append("<input type=\"text\" name=\"category\" placeholder=\"Enter a category\">");
        responseBuilder.append("<input type=\"submit\" value=\"Search\">");
        responseBuilder.append("</form>");
        responseBuilder.append("</div>");

        responseBuilder.append("<table>");
        responseBuilder.append("<tr>");
        responseBuilder.append("<th>Name</th>");
        responseBuilder.append("<th>Characteristics</th>");
        responseBuilder.append("<th>Manufacturer</th>");
        responseBuilder.append("<th>Quantity</th>");
        responseBuilder.append("<th>Price</th>");
        responseBuilder.append("<th>Category</th>");
        responseBuilder.append("<th>");
        responseBuilder.append("<form action=\"/product/add\" method=\"post\"><button type=\"submit\">Add product</button></form>");
        responseBuilder.append("</th>");
        responseBuilder.append("</tr>");

        for (Product product : products) {
            responseBuilder.append("<tr>");
            responseBuilder.append("<td>").append(product.getName()).append("</td>");
            responseBuilder.append("<td>").append(product.getCharacteristics()).append("</td>");
            responseBuilder.append("<td>").append(product.getManufacturer()).append("</td>");
            responseBuilder.append("<td>").append(product.getQuantity()).append("</td>");
            responseBuilder.append("<td>").append(product.getPrice()).append("</td>");
            responseBuilder.append("<td>").append(product.getCategory().getName()).append("</td>");
            responseBuilder.append("<td>");
            responseBuilder.append("<th>");
            responseBuilder.append("<form action=\"/product/edit\" method=\"post\"><button type=\"submit\" name=\"name\" value=\"" + product.getName() + "\">Edit</button></form>");
            responseBuilder.append("</th>");
            responseBuilder.append("<th>");
            responseBuilder.append("<form action=\"/product/delete\" method=\"post\"><button type=\"submit\" name=\"name\" value=\"" + product.getName() + "\">Delete</button></form>");
            responseBuilder.append("</th>");
            responseBuilder.append("</td>");
            responseBuilder.append("</tr>");
        }

        responseBuilder.append("</table>");


        responseBuilder.append("<p>Total price: ").append(price).append("</p>");

        if (!(number == 0)) {
            responseBuilder.append("<p>Number of products in the storage: ").append(number).append("</p>");
        }

        responseBuilder.append("</body></html>");
        return responseBuilder;
    }

    private static StringBuilder buildAddProduct() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<h2>Add Product</h2>");
        responseBuilder.append("<form method=\"POST\" action=\"/product/addProduct\">");
        responseBuilder.append("<label for=\"name\">Name:</label>");
        responseBuilder.append("<input type=\"text\" id=\"name\" name=\"name\"><br><br>");
        responseBuilder.append("<label for=\"characteristics\">Characteristics:</label>");
        responseBuilder.append("<input type=\"text\" id=\"characteristics\" name=\"characteristics\"><br><br>");
        responseBuilder.append("<label for=\"manufacturer\">Manufacturer:</label>");
        responseBuilder.append("<input type=\"text\" id=\"manufacturer\" name=\"manufacturer\"><br><br>");
        responseBuilder.append("<label for=\"quantity\">Quantity:</label>");
        responseBuilder.append("<input type=\"number\" id=\"quantity\" name=\"quantity\"><br><br>");
        responseBuilder.append("<label for=\"price\">Price:</label>");
        responseBuilder.append("<input type=\"number\" id=\"price\" name=\"price\"><br><br>");
        responseBuilder.append("<label for=\"category\">Category:</label>");
        responseBuilder.append("<input type=\"text\" id=\"category\" name=\"category\"><br><br>");
        responseBuilder.append("<input type=\"submit\" value=\"Submit\">");
        responseBuilder.append("</form>");

        responseBuilder.append("</body></html>");
        return responseBuilder;
    }

    private static StringBuilder buildEditProduct(String productName) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("<html><body>");
        responseBuilder.append("<h1>Storage</h1>");
        responseBuilder.append("<div style=\"display: flex;\">");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/home\">Home</a></h3>");
        responseBuilder.append("<h3 style=\"margin-right: 10px;\"><a href=\"/product\">Products</a></h3>");
        responseBuilder.append("<h3><a href=\"/category\">Categories</a></h3>");
        responseBuilder.append("</div>");

        responseBuilder.append("<h2>Edit Product " + productName + "</h2>");
        responseBuilder.append("<form method=\"POST\" action=\"/product/editProduct?name=" + productName + "\">");
        responseBuilder.append("<input type=\"hidden\" name=\"product_name\" value=\"" + productName + "\">");
        responseBuilder.append("<label for=\"characteristics\">Characteristics:</label>");
        responseBuilder.append("<input type=\"text\" id=\"characteristics\" name=\"characteristics\"><br><br>");
        responseBuilder.append("<label for=\"manufacturer\">Manufacturer:</label>");
        responseBuilder.append("<input type=\"text\" id=\"manufacturer\" name=\"manufacturer\"><br><br>");
        responseBuilder.append("<label for=\"quantity\">Quantity:</label>");
        responseBuilder.append("<input type=\"number\" id=\"quantity\" name=\"quantity\"><br><br>");
        responseBuilder.append("<label for=\"price\">Price:</label>");
        responseBuilder.append("<input type=\"number\" id=\"price\" name=\"price\"><br><br>");
        responseBuilder.append("<label for=\"category\">Category:</label>");
        responseBuilder.append("<input type=\"text\" id=\"category\" name=\"category\"><br><br>");
        responseBuilder.append("<input type=\"submit\" value=\"Submit\">");
        responseBuilder.append("</form>");

        responseBuilder.append("</body></html>");
        return responseBuilder;
    }
}