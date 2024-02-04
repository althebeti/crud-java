package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Person;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonController implements HttpHandler {

    private final Connection connection;

    public PersonController(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            handlePostRequest(exchange);
        } else if ("GET".equalsIgnoreCase(method)) {
            handleGetRequest(exchange);
        } else if ("PUT".equalsIgnoreCase(method)) {
            handlePutRequest(exchange);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            handleDeleteRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, 0); // Method Not Allowed
            exchange.close();
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        String inputLine;
        StringBuilder requestData = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            requestData.append(inputLine);
        }
        reader.close();

        String name = requestData.toString().trim();
        name = name.replaceAll("[%=]", " ");
        name = name.replaceAll("[0-9](?=[^ ])", "");


        try {
            // Create a new person
            int generatedId = createPerson(connection, name);

            // Respond with the generated id
            String response = "Person created with id: " + generatedId;
            exchange.sendResponseHeaders(201, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0); // Internal Server Error
        } finally {
            exchange.close();
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            // Retrieve all persons
            List<Person> persons = getAllPersons(connection);
            StringBuilder response = new StringBuilder();
            for (Person p : persons) {
                response.append(p).append("\n");
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0); // Internal Server Error
            exchange.close();
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        String inputLine;
        StringBuilder requestData = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            requestData.append(inputLine);
        }
        reader.close();

        String requestBodyString = requestData.toString().trim();
        try {
            // Parse the JSON request body to update a person's name
            JSONObject jsonBody = new JSONObject(requestBodyString);
            int id = jsonBody.getInt("id");
            String updatedName = jsonBody.getString("name");

            // Update the person's name in the database
            boolean success = updatePerson(connection, id, updatedName);

            if (success) {
                exchange.sendResponseHeaders(200, 0); // OK
            } else {
                exchange.sendResponseHeaders(404, 0); // Not Found
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the SQL exception for debugging
            exchange.sendResponseHeaders(500, 0); // Internal Server Error
        } catch (JSONException e) {
            e.printStackTrace(); // Log the JSON parsing exception for debugging
            exchange.sendResponseHeaders(400, 0); // Bad Request
        } finally {
            exchange.close();
        }
    }


    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Extract the ID from the request URL
        String[] pathParts = path.split("/");
        if (pathParts.length != 3) {
            exchange.sendResponseHeaders(400, 0); // Bad Request
            exchange.close();
            return;
        }

        int id = Integer.parseInt(pathParts[2]);

        try {
            // Delete the person from the database
            boolean success = deletePerson(connection, id);

            if (success) {
                exchange.sendResponseHeaders(204, 0); // No Content (successfully deleted)
            } else {
                exchange.sendResponseHeaders(404, 0); // Not Found
            }
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0); // Internal Server Error
        } finally {
            exchange.close();
        }
    }

    private static int createPerson(Connection connection, String name) throws SQLException {
        String insertSQL = "INSERT INTO persons (name) VALUES (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1); // Return the auto-generated ID
        }
        preparedStatement.close();
        return -1; // Return -1 in case of failure
    }

    private static List<Person> getAllPersons(Connection connection) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String selectSQL = "SELECT * FROM persons";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSQL);
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Person person = new Person(id, name);
            persons.add(person);
        }
        resultSet.close();
        statement.close();
        return persons;
    }

    private static boolean updatePerson(Connection connection, int id, String updatedName) throws SQLException {
        String updateSQL = "UPDATE persons SET name = ? WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);
        preparedStatement.setString(1, updatedName);
        preparedStatement.setInt(2, id);
        int rowsUpdated = preparedStatement.executeUpdate();
        preparedStatement.close();
        return rowsUpdated > 0;
    }

    private static boolean deletePerson(Connection connection, int id) throws SQLException {
        String deleteSQL = "DELETE FROM persons WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
        preparedStatement.setInt(1, id);
        int rowsDeleted = preparedStatement.executeUpdate();
        preparedStatement.close();
        return rowsDeleted > 0;
    }
}
