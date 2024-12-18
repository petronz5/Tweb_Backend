package tweb.titancommerce.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Borsello {
    private int id;
    private int user_id;
    private String metodo_pagamento;
    private double importo;
    private String image_payment;

    public Borsello() {}

    public Borsello(int id, int user_id, String metodo_pagamento, double importo, String image_payment) {
        this.id = id;
        this.user_id = user_id;
        this.metodo_pagamento = metodo_pagamento;
        this.importo = importo;
        this.image_payment = image_payment;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMetodo_pagamento() {
        return metodo_pagamento;
    }

    public void setMetodo_pagamento(String metodo_pagamento) {
        this.metodo_pagamento = metodo_pagamento;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public String getImage_payment() {
        return image_payment;
    }

    public void setImage_payment(String image_payment) {
        this.image_payment = image_payment;
    }

    // Metodo per salvare un nuovo metodo di pagamento
    public boolean savePayment(Connection conn) throws SQLException {
        String sql = "INSERT INTO borsello (user_id, metodo_pagamento, importo, image_payment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, this.user_id);
            statement.setString(2, this.metodo_pagamento);
            statement.setDouble(3, this.importo);
            statement.setString(4, this.image_payment);
            return statement.executeUpdate() > 0;
        }
    }


    // Metodo statico per ottenere i metodi di pagamento per un utente
    public static List<Borsello> getPaymentMethodsByUserId(int userId, Connection conn) throws SQLException {
        List<Borsello> methods = new ArrayList<>();
        String sql = "SELECT * FROM borsello WHERE user_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String metodoPagamento = resultSet.getString("metodo_pagamento");
                double importo = resultSet.getDouble("importo");
                String imagePayment = resultSet.getString("image_payment"); // Prende il campo image_payment

                // Aggiunta del nuovo campo al costruttore
                methods.add(new Borsello(id, userId, metodoPagamento, importo, imagePayment));
            }
        }
        return methods;
    }


    public boolean updateImporto(Connection conn) throws SQLException {
        String sql = "UPDATE borsello SET importo = ? WHERE user_id = ? AND metodo_pagamento = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, this.importo);
            statement.setInt(2, this.user_id);
            statement.setString(3, this.metodo_pagamento);
            return statement.executeUpdate() > 0;
        }
    }
}
