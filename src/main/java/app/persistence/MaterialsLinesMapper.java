package app.persistence;

import app.entities.Material;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialsLinesMapper
{
    private final ConnectionPool connectionPool;
    private final MaterialMapper materialMapper;

    public MaterialsLinesMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialMapper = new MaterialMapper(connectionPool);
    }

    public void createMaterialLine(int orderId, MaterialsLine line) throws DatabaseException
    {
        String sql = """
                INSERT INTO materials_lines
                (order_id, material_id, material_name, unit_type, quantity, unit_price, line_price)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING line_id
                """;

        Material material = line.getMaterial();
        if (material == null)
        {
            throw new DatabaseException("Material må ikke være null ved oprettelse af MaterialsLine");
        }

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, material.getId());
            ps.setString(3, material.getName());
            ps.setString(4, material.getUnitType());
            ps.setInt(5, line.getQuantity());
            ps.setDouble(6, material.getPrice());
            ps.setDouble(7, line.getLinePrice());

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    line.setLineId(rs.getInt("line_id"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af materials_line: " + e.getMessage());
        }
    }

    public List<MaterialsLine> getMaterialLinesByOrderId(int orderId) throws DatabaseException {
        List<MaterialsLine> materialLines = new ArrayList<>();
        String sql = """
            SELECT ml.line_id, ml.quantity, ml.unit_price, ml.line_price,
                   m.id, m.name, m.description, m.unit, m.unit_type,
                   m.material_length, m.material_width, m.material_height, m.price
            FROM materials_lines ml
            LEFT JOIN materials m ON ml.material_id = m.id
            WHERE ml.order_id = ?
            ORDER BY ml.line_id
            """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Material material = new Material(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("unit"),
                            rs.getString("unit_type"),
                            rs.getDouble("material_length"),
                            rs.getDouble("material_width"),
                            rs.getDouble("material_height"),
                            rs.getDouble("price")
                    );

                    MaterialsLine line = new MaterialsLine();
                    line.setLineId(rs.getInt("line_id"));
                    line.setMaterial(material);
                    line.setQuantity(rs.getInt("quantity"));
                    line.setUnitPrice(rs.getDouble("unit_price")); // Use stored unit_price
                    line.setLinePrice(rs.getDouble("line_price"));

                    materialLines.add(line);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af materialelinjer: " + e.getMessage());
        }
        return materialLines;
    }



    public void updateMaterialLinePrice(int lineId, double unitPrice, int quantity) throws DatabaseException
    {
        String sql = """
            UPDATE materials_lines
            SET unit_price = ?, line_price = ?
            WHERE line_id = ?
            """;

        double linePrice = unitPrice * quantity;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setDouble(1, unitPrice);
            ps.setDouble(2, linePrice);
            ps.setInt(3, lineId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af pris: " + e.getMessage());
        }
    }


    public boolean updateMaterialLineName(int orderId, MaterialsLine line, String newMaterialName) throws DatabaseException
    {
        String sql = """
                UPDATE materials_lines
                SET material_name=?
                WHERE order_id=? AND line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, newMaterialName);
            ps.setInt(2, orderId);
            ps.setInt(3, line.getLineId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1)
            {

                return true;
            } else
            {
                throw new DatabaseException("Ingen material line fundet med id: " + line.getLineId());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materials_lines: " + e.getMessage());
        }
    }

    public String getMaterialLineName(int orderId, MaterialsLine line) throws DatabaseException
    {
        String result = "";
        String sql = """
                SELECT material_name
                FROM materials_lines WHERE  order_id=? AND line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, line.getLineId());
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    result = rs.getString("material_name");
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af materials_lines: " + e.getMessage());
        }
        return result;
    }

    public boolean deleteMaterialLine(MaterialsLine line) throws DatabaseException
    {
        boolean result = false;
        String sql = """
                DELETE FROM materials_lines
                WHERE line_id=?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, line.getLineId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage() + "Fejl ved sletning af ordrelinje med id: " + line.getLineId());
        }
        return result;
    }

    public boolean deleteAllMaterialLinesByOrderId(int orderId) throws DatabaseException
    {
        String sql = """
                DELETE FROM materials_lines
                WHERE order_id = ?;
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected >= 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af materiale linjer på ordre id: " + orderId + ": " + e.getMessage());
        }
        return false;
    }
}