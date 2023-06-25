package client_server;
public class Processor {

    public static String process(Message message) {
        Object[] requestInfo = extractValues(message.getUsefulInfo());
        switch ((int) requestInfo[0]) {
            case 1:
                System.out.println("Number of products in storage = " + Storage.getNumberOfProducts());
                return "OK";

            case 2:
                Storage.subtractProducts((int) requestInfo[1]);
                System.out.println("Number of products in storage after subtracting = " + Storage.getNumberOfProducts());
                return "OK";

            case 3:
                Storage.addProducts((int) requestInfo[1]);
                System.out.println("Number of products in storage after adding = " + Storage.getNumberOfProducts());
                return "OK";

            case 4:
                ProductGroup productGroup = new ProductGroup((String) requestInfo[1]);
                System.out.println("Created " + productGroup.getProductGroupName() + " product group");
                return "OK";

            case 5:
                Product.setPrice((int) requestInfo[1]);
                System.out.println("Set price to be " + Product.getPrice());
                return "OK";

            default:
                return "Request has not been processed";
        }
    }

    public static Object[] extractValues(String input) {
        String[] parts = input.split("/");

        if (parts.length != 2) {
            return null;
        }

        try {
            Object beforeSlash;
            if (isNumeric(parts[0])) {
                beforeSlash = Integer.parseInt(parts[0]);
            } else {
                beforeSlash = parts[0];
            }

            Object afterSlash;
            if (isNumeric(parts[1])) {
                afterSlash = Integer.parseInt(parts[1]);
            } else {
                afterSlash = parts[1];
            }

            return new Object[]{beforeSlash, afterSlash};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }
}
